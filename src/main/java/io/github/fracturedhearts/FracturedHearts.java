package io.github.fracturedhearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.ObjIntConsumer;

public final class FracturedHearts implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Fractured Hearts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("fractured-hearts.json");
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath("fractured_hearts", "maximum_health");
    private static Config currentConfig = new Config();
    private static HeartStore hearts;
    private static boolean uninstalled;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("fracturedhearts")
                    .then(Commands.literal("status").executes(context -> status(context.getSource())))
                    .then(Commands.literal("toggle")
                            .then(Commands.literal("sounds").executes(context -> toggle(context.getSource(), "sounds")))
                            .then(Commands.literal("messages").executes(context -> toggle(context.getSource(), "messages")))
                            .then(Commands.literal("particles").executes(context -> toggle(context.getSource(), "particles"))))
                    .then(Commands.literal("consumeapple")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> consumeApple(context.getSource())))
                    .then(Commands.literal("reload")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> reload(context.getSource())))
                    .then(Commands.literal("uninstall")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .executes(context -> uninstall(context.getSource())))
                    .then(Commands.literal("sethearts")
                            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                            .then(Commands.argument("player", EntityArgument.player())
                                    .then(Commands.argument("points", IntegerArgumentType.integer(1))
                                            .executes(context -> setHearts(context.getSource(),
                                                    EntityArgument.getPlayer(context, "player"),
                                                    IntegerArgumentType.getInteger(context, "points"))))));
            root.then(integerSetting("ceiling", (config, value) -> config.maxHealth = value));
            root.then(integerSetting("floor", (config, value) -> config.minimumHealth = value));
            root.then(integerSetting("gain", (config, value) -> config.healthPerApple = value));
            root.then(integerSetting("loss", (config, value) -> config.healthLostPerDeath = value));
            root.then(Commands.literal("permadeath")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("toggle").executes(context -> change(context.getSource(),
                            config -> config.permadeath = !config.permadeath, "permadeath")))
                    .then(Commands.literal("true").executes(context -> change(context.getSource(),
                            config -> config.permadeath = true, "permadeath")))
                    .then(Commands.literal("false").executes(context -> change(context.getSource(),
                            config -> config.permadeath = false, "permadeath"))));
            root.then(Commands.literal("heartsteal")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("toggle").executes(context -> change(context.getSource(),
                            config -> config.pvpHeartSteal = !config.pvpHeartSteal, "heartsteal")))
                    .then(Commands.literal("true").executes(context -> change(context.getSource(),
                            config -> config.pvpHeartSteal = true, "heartsteal")))
                    .then(Commands.literal("false").executes(context -> change(context.getSource(),
                            config -> config.pvpHeartSteal = false, "heartsteal"))));
            dispatcher.register(root);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                Config config = loadConfig(server);
                hearts = HeartStore.load(server);
                hearts.importAndDropScoreboards(server);
                try {
                    hearts.migrateOfflinePlayerData(server);
                } catch (IOException exception) {
                    LOGGER.warn("Could not finish offline player migration; unresolved players remain pending", exception);
                }
                runCommand(server, "schedule clear fractured_hearts:internals/loop");
                runCommand(server, "schedule clear fractured_hearts:pvp/cleanup_killer_tags");
                currentConfig = config;
                uninstalled = false;
                LOGGER.info("Loaded Fractured Hearts configuration and world heart data");
            } catch (IOException | JsonParseException | IllegalArgumentException exception) {
                hearts = null;
                LOGGER.error("Could not load or migrate Fractured Hearts data; legacy scoreboards were retained", exception);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> join(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (hearts != null && !uninstalled) {
                apply(newPlayer);
                HeartStore.PlayerData data = hearts.get(newPlayer);
                if (data != null && data.pendingLossNotice) {
                    data.pendingLossNotice = false;
                    feedback(newPlayer, data, false);
                    saveHearts();
                }
                if (data != null && data.permadead) disconnectPermadead(newPlayer);
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) died(player, damageSource);
        });
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> integerSetting(
            String name, ObjIntConsumer<Config> setter) {
        return Commands.literal(name).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("points", IntegerArgumentType.integer(1))
                .executes(context -> change(context.getSource(),
                        config -> setter.accept(config, IntegerArgumentType.getInteger(context, "points")), name)));
    }

    private static int reload(CommandSourceStack source) {
        try {
            Config loaded = loadConfig(source.getServer());
            currentConfig = loaded;
            syncOnlinePlayers(source.getServer());
            source.sendSuccess(() -> Component.literal("Fractured Hearts config reloaded."), true);
            return 1;
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Could not reload Fractured Hearts config: " + exception.getMessage()));
            return 0;
        }
    }

    private static int change(CommandSourceStack source, java.util.function.Consumer<Config> edit, String setting) {
        Config updated = new Config(currentConfig);
        edit.accept(updated);
        try {
            updated.validate();
            saveConfig(updated);
            currentConfig = updated;
            syncOnlinePlayers(source.getServer());
            source.sendSuccess(() -> Component.literal("Fractured Hearts " + setting + " saved to config."), true);
            return 1;
        } catch (IOException | IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Could not save Fractured Hearts config: " + exception.getMessage()));
            return 0;
        }
    }

    private static void join(ServerPlayer player) {
        if (hearts == null || uninstalled) return;
        try {
            HeartStore.PlayerData data = hearts.getOrCreate(player,
                    (int) Math.round(player.getAttributeValue(Attributes.MAX_HEALTH)));
            // Tags left by older datapack builds are no longer used by the mod.
            for (String tag : java.util.List.copyOf(player.entityTags())) {
                if (tag.equals("fractured_hearts.sounds_off")) data.soundsOff = true;
                if (tag.equals("fractured_hearts.messages_off")) data.messagesOff = true;
                if (tag.equals("fractured_hearts.particles_off")) data.particlesOff = true;
                if (tag.startsWith("fractured_hearts.")) player.removeTag(tag);
            }
            hearts.save();
            runPlayerCommand(player, "advancement revoke @s only fractured_hearts:health_gain");
            apply(player);
            if (data.previousGameMode != null) {
                if (player.gameMode() == GameType.SPECTATOR) {
                    player.setGameMode(GameType.byId(data.previousGameMode));
                }
                data.previousGameMode = null;
                hearts.save();
            }
            if (data.permadead) disconnectPermadead(player);
        } catch (IOException exception) {
            LOGGER.error("Could not load heart data for {}", player.getName().getString(), exception);
        }
    }

    private static void died(ServerPlayer victim, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (hearts == null || uninstalled) return;
        try {
            HeartStore.PlayerData data = hearts.getOrCreate(victim, currentConfig.maxHealth);
            if (data.excluded || data.permadead) return;
            int next = data.points - currentConfig.healthLostPerDeath;
            if (currentConfig.pvpHeartSteal && damageSource.getEntity() instanceof ServerPlayer killer && killer != victim) {
                HeartStore.PlayerData killerData = hearts.getOrCreate(killer, currentConfig.maxHealth);
                if (!killerData.excluded && !killerData.permadead)
                    killerData.points = Math.min(currentConfig.maxHealth,
                            killerData.points + currentConfig.healthLostPerDeath);
                apply(killer);
                if (!killerData.messagesOff)
                    killer.sendSystemMessage(Component.literal("You stole hearts from " + victim.getName().getString() + "."));
            }
            if (next < currentConfig.minimumHealth) {
                if (currentConfig.permadeath) {
                    data.permadead = true;
                }
            } else {
                data.points = next;
            }
            data.pendingLossNotice = true;
            hearts.save();
        } catch (IOException exception) {
            LOGGER.error("Could not save heart loss for {}", victim.getName().getString(), exception);
        }
    }

    private static void apply(ServerPlayer player) {
        if (hearts == null || uninstalled) return;
        HeartStore.PlayerData data = hearts.get(player);
        if (data == null) return;
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        if (data.legacyBase != null) {
            double base = attribute.getBaseValue();
            if (data.legacyScore != null && Math.abs(base - data.legacyScore) < 0.001) {
                attribute.setBaseValue(data.legacyBase);
                data.legacyBase = null;
                data.legacyScore = null;
                saveHearts();
            } else if (Math.abs(base - data.legacyBase) < 0.001) {
                data.legacyBase = null;
                data.legacyScore = null;
                saveHearts();
            } else {
                LOGGER.warn("Cannot safely restore legacy max-health base for {}", player.getName().getString());
            }
        }
        if (data.excluded) {
            attribute.removeModifier(HEALTH_MODIFIER_ID);
        } else {
            if (data.points > currentConfig.maxHealth) {
                data.points = currentConfig.maxHealth;
                saveHearts();
            }
            double amount = data.points - attribute.getBaseValue();
            AttributeModifier existing = attribute.getModifier(HEALTH_MODIFIER_ID);
            if (Math.abs(amount) < 0.001) {
                if (existing != null) attribute.removeModifier(HEALTH_MODIFIER_ID);
            } else if (existing == null || Math.abs(existing.amount() - amount) >= 0.001) {
                attribute.addOrUpdateTransientModifier(new AttributeModifier(
                        HEALTH_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void disconnectPermadead(ServerPlayer player) {
        player.connection.disconnect(Component.literal("Fractured Hearts permadeath: you have perished."));
    }

    private static void saveHearts() {
        try {
            hearts.save();
        } catch (IOException exception) {
            LOGGER.error("Could not save Fractured Hearts world data", exception);
        }
    }

    private static void runPlayerCommand(ServerPlayer player, String command) {
        player.level().getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER), command);
    }

    private static int status(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null || hearts == null) return 0;
        HeartStore.PlayerData data = hearts.get(player);
        if (data == null) return 0;
        source.sendSuccess(() -> Component.literal("Fractured Hearts: " + (data.excluded ? "excluded" :
                data.points + " health points (" + (data.points / 2.0) + " hearts)")), false);
        return 1;
    }

    private static int toggle(CommandSourceStack source, String option) {
        ServerPlayer player = source.getPlayer();
        if (player == null || hearts == null) return 0;
        HeartStore.PlayerData data = hearts.get(player);
        if (data == null) return 0;
        boolean enabled;
        switch (option) {
            case "sounds" -> enabled = !(data.soundsOff = !data.soundsOff);
            case "messages" -> enabled = !(data.messagesOff = !data.messagesOff);
            case "particles" -> enabled = !(data.particlesOff = !data.particlesOff);
            default -> { return 0; }
        }
        saveHearts();
        source.sendSuccess(() -> Component.literal(option + " " + (enabled ? "on" : "off")), false);
        return 1;
    }

    private static int consumeApple(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null || hearts == null || uninstalled) return 0;
        HeartStore.PlayerData data = hearts.get(player);
        if (data == null || data.excluded || data.permadead) return 0;
        if (data.points + currentConfig.healthPerApple > currentConfig.maxHealth) {
            player.sendSystemMessage(Component.literal("You are already too close to the maximum health to gain hearts."));
            return 0;
        }
        data.points += currentConfig.healthPerApple;
        apply(player);
        feedback(player, data, true);
        saveHearts();
        return 1;
    }

    private static void feedback(ServerPlayer player, HeartStore.PlayerData data, boolean gained) {
        if (!data.messagesOff) {
            String message = data.permadead ? "GAME OVER: you have perished." :
                    (gained ? "Recovered hearts. " : "Lost hearts. ")
                            + "Maximum health: " + (data.points / 2.0) + " hearts.";
            player.sendSystemMessage(Component.literal(message));
        }
        if (!data.soundsOff) {
            player.playSound(gained ? SoundEvents.CHORUS_FLOWER_GROW : SoundEvents.GLASS_BREAK, 1.0F, 1.0F);
        }
        if (!data.particlesOff) {
            player.level().sendParticles(player, gained ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.DAMAGE_INDICATOR,
                    false, false, player.getX(), player.getY() + 1, player.getZ(), 15,
                    0.5, 0.5, 0.5, 0.0);
        }
    }

    private static int setHearts(CommandSourceStack source, ServerPlayer player, int points) {
        if (hearts == null || points < currentConfig.minimumHealth || points > currentConfig.maxHealth) {
            source.sendFailure(Component.literal("Health points must be between the configured floor and ceiling."));
            return 0;
        }
        try {
            HeartStore.PlayerData data = hearts.getOrCreate(player, currentConfig.maxHealth);
            data.points = points;
            data.excluded = false;
            apply(player);
            hearts.save();
            source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + " to " + points + " health points."), true);
            return 1;
        } catch (IOException exception) {
            source.sendFailure(Component.literal("Could not save heart data: " + exception.getMessage()));
            return 0;
        }
    }

    private static int uninstall(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        if (hearts == null || uninstalled) {
            source.sendFailure(Component.literal("Fractured Hearts world data is not active."));
            return 0;
        }
        try {
            hearts.migrateOfflinePlayerData(server);
        } catch (IOException exception) {
            source.sendFailure(Component.literal("Could not finish offline migration: " + exception.getMessage()));
            return 0;
        }
        if (hearts.hasPendingLegacyPlayers() || hearts.hasOfflineCleanup(server)) {
            source.sendFailure(Component.literal("Uninstall stopped: some legacy saved health or game modes remain unresolved. Affected players must join or be repaired manually."));
            return 0;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            HeartStore.PlayerData data = hearts.get(player);
            if (data != null && data.legacyBase != null) {
                source.sendFailure(Component.literal("Uninstall stopped: unresolved legacy max-health base for "
                        + player.getName().getString()));
                return 0;
            }
        }
        try {
            hearts.delete();
        } catch (IOException exception) {
            source.sendFailure(Component.literal("Could not remove world heart data: " + exception.getMessage()));
            return 0;
        }
        uninstalled = true;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (attribute != null) attribute.removeModifier(HEALTH_MODIFIER_ID);
            player.setHealth(player.getMaxHealth());
            HeartStore.PlayerData data = hearts.get(player);
            if (data != null && data.previousGameMode != null && player.gameMode() == GameType.SPECTATOR) {
                player.setGameMode(GameType.byId(data.previousGameMode));
            }
            runPlayerCommand(player, "advancement revoke @s only fractured_hearts:health_gain");
        }
        source.sendSuccess(() -> Component.literal(
                "Fractured Hearts world data removed. Stop the server and remove the JAR."), true);
        return 1;
    }

    private static Config loadConfig(MinecraftServer server) throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            Config config = configFromScoreboard(server);
            try {
                config.validate();
            } catch (IllegalArgumentException exception) {
                LOGGER.warn("Existing Fractured Hearts scoreboard settings are invalid; using fork defaults", exception);
                config = new Config();
            }
            saveConfig(config);
            return config;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            Config config = GSON.fromJson(reader, Config.class);
            if (config == null) throw new JsonParseException("Config must be a JSON object");
            config.validate();
            return config;
        }
    }

    private static Config configFromScoreboard(MinecraftServer server) {
        Config config = new Config();
        Integer ceiling = scoreboardSetting(server, "fractured_hearts.ceiling");
        Integer floor = scoreboardSetting(server, "fractured_hearts.floor");
        Integer gain = scoreboardSetting(server, "fractured_hearts.gain_inc");
        Integer loss = scoreboardSetting(server, "fractured_hearts.loss_inc");
        Integer permadeath = scoreboardSetting(server, "fractured_hearts.permadeath");
        Integer heartSteal = scoreboardSetting(server, "fractured_hearts.heart_steal");
        if (ceiling != null) config.maxHealth = ceiling;
        if (floor != null) config.minimumHealth = floor;
        if (gain != null) config.healthPerApple = gain;
        if (loss != null) config.healthLostPerDeath = loss;
        if (permadeath != null) config.permadeath = permadeath == 1;
        if (heartSteal != null) config.pvpHeartSteal = heartSteal == 1;
        return config;
    }

    private static Integer scoreboardSetting(MinecraftServer server, String objectiveName) {
        Objective objective = server.getScoreboard().getObjective(objectiveName);
        if (objective == null) return null;
        for (var entry : server.getScoreboard().listPlayerScores(objective)) {
            if (entry.owner().equals("$fractured_hearts.fakeplayer")) return entry.value();
        }
        return null;
    }

    private static void saveConfig(Config config) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        Path temporary = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        }
        try {
            Files.move(temporary, CONFIG_PATH, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void syncOnlinePlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) apply(player);
    }

    private static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    public static final class Config {
        // Values are health points: 2 points = 1 heart.
        public int maxHealth = 20;
        public int minimumHealth = 2;
        public int healthPerApple = 2;
        public int healthLostPerDeath = 2;
        public boolean permadeath = false;
        public boolean pvpHeartSteal = false;

        public Config() { }

        public Config(Config other) {
            maxHealth = other.maxHealth;
            minimumHealth = other.minimumHealth;
            healthPerApple = other.healthPerApple;
            healthLostPerDeath = other.healthLostPerDeath;
            permadeath = other.permadeath;
            pvpHeartSteal = other.pvpHeartSteal;
        }

        private void validate() {
            if (minimumHealth < 1 || maxHealth < minimumHealth || maxHealth > 1024)
                throw new IllegalArgumentException("Require 1 <= minimumHealth <= maxHealth <= 1024");
            if (healthPerApple < 1 || healthPerApple > maxHealth)
                throw new IllegalArgumentException("healthPerApple must be between 1 and maxHealth");
            if (healthLostPerDeath < 1 || healthLostPerDeath > maxHealth)
                throw new IllegalArgumentException("healthLostPerDeath must be between 1 and maxHealth");
        }
    }
}
