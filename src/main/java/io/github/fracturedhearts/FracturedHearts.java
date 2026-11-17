package io.github.fracturedhearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private static Config currentConfig = new Config();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var root = Commands.literal("fracturedhearts")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.literal("reload").executes(context -> reload(context.getSource())))
                    .then(Commands.literal("uninstall").executes(context -> uninstall(context.getSource())))
                    .then(Commands.literal("markpermadeath").executes(context -> markPermadeath(context.getSource())));
            root.then(integerSetting("ceiling", (config, value) -> config.maxHealth = value));
            root.then(integerSetting("floor", (config, value) -> config.minimumHealth = value));
            root.then(integerSetting("gain", (config, value) -> config.healthPerApple = value));
            root.then(integerSetting("loss", (config, value) -> config.healthLostPerDeath = value));
            root.then(Commands.literal("permadeath")
                    .then(Commands.literal("toggle").executes(context -> change(context.getSource(),
                            config -> config.permadeath = !config.permadeath, "permadeath")))
                    .then(Commands.literal("true").executes(context -> change(context.getSource(),
                            config -> config.permadeath = true, "permadeath")))
                    .then(Commands.literal("false").executes(context -> change(context.getSource(),
                            config -> config.permadeath = false, "permadeath"))));
            root.then(Commands.literal("heartsteal")
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
                applyConfig(server, config);
                currentConfig = config;
                LOGGER.info("Loaded configuration from {}", CONFIG_PATH);
            } catch (IOException | JsonParseException | IllegalArgumentException exception) {
                LOGGER.error("Could not load Fractured Hearts configuration; keeping existing scoreboard settings", exception);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncOnlinePlayers(server));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> integerSetting(
            String name, ObjIntConsumer<Config> setter) {
        return Commands.literal(name).then(Commands.argument("points", IntegerArgumentType.integer(1))
                .executes(context -> change(context.getSource(),
                        config -> setter.accept(config, IntegerArgumentType.getInteger(context, "points")), name)));
    }

    private static int reload(CommandSourceStack source) {
        try {
            Config loaded = loadConfig(source.getServer());
            applyConfig(source.getServer(), loaded);
            currentConfig = loaded;
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
            applyConfig(source.getServer(), updated);
            currentConfig = updated;
            source.sendSuccess(() -> Component.literal("Fractured Hearts " + setting + " saved to config."), true);
            return 1;
        } catch (IOException | IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Could not save Fractured Hearts config: " + exception.getMessage()));
            return 0;
        }
    }

    private static int markPermadeath(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        var scoreboard = source.getServer().getScoreboard();
        Objective objective = scoreboard.getObjective("fractured_hearts.original_gamemode");
        if (objective != null && scoreboard.getPlayerScoreInfo(player, objective) == null) {
            scoreboard.getOrCreatePlayerScore(player, objective).set(player.gameMode().getId());
        }
        return 1;
    }

    private static int uninstall(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        var scoreboard = server.getScoreboard();
        Objective health = scoreboard.getObjective("fractured_hearts.score");
        Objective originals = scoreboard.getObjective("fractured_hearts.original_health");
        Objective gameModes = scoreboard.getObjective("fractured_hearts.original_gamemode");
        if (health == null || originals == null) {
            source.sendFailure(Component.literal("No complete Fractured Hearts baseline exists. Nothing was changed."));
            return 0;
        }

        java.util.List<ServerPlayer> tracked = new java.util.ArrayList<>();
        for (var entry : scoreboard.listPlayerScores(health)) {
            String name = entry.owner();
            ServerPlayer player = server.getPlayerList().getPlayerByName(name);
            if (player == null) {
                source.sendFailure(Component.literal("Uninstall stopped: " + name + " is offline. Have every tracked player join before uninstalling."));
                return 0;
            }
            ReadOnlyScoreInfo baseline = scoreboard.getPlayerScoreInfo(player, originals);
            if (baseline == null) {
                source.sendFailure(Component.literal("Uninstall stopped: no original health was recorded for " + name + ". Nothing was changed."));
                return 0;
            }
            if (Math.abs(player.getAttributeBaseValue(Attributes.MAX_HEALTH) - entry.value()) > 0.001) {
                source.sendFailure(Component.literal("Uninstall stopped: another change to " + name + "'s max health was detected. Nothing was changed."));
                return 0;
            }
            tracked.add(player);
        }

        runCommand(server, "schedule clear fractured_hearts:internals/loop");
        runCommand(server, "schedule clear fractured_hearts:pvp/cleanup_killer_tags");
        for (String advancement : java.util.List.of("health_gain", "health_loss", "initialization", "pvp_kill")) {
            runCommand(server, "advancement revoke @a only fractured_hearts:" + advancement);
        }
        for (String key : java.util.List.of("gain", "loss", "temp")) {
            runCommand(server, "data remove storage fractured_hearts:data " + key);
        }
        for (ServerPlayer player : tracked) {
            int baseline = scoreboard.getPlayerScoreInfo(player, originals).value();
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseline / 1000.0);
            if (gameModes != null) {
                ReadOnlyScoreInfo oldMode = scoreboard.getPlayerScoreInfo(player, gameModes);
                if (oldMode != null && player.gameMode() == GameType.SPECTATOR) {
                    player.setGameMode(GameType.byId(oldMode.value()));
                }
            }
            for (String tag : java.util.List.copyOf(player.entityTags())) {
                if (tag.startsWith("fractured_hearts.")) player.removeTag(tag);
            }
        }
        for (String objective : java.util.List.of(
                "fractured_hearts.score", "fractured_hearts.hearts", "fractured_hearts.death",
                "fractured_hearts.toggle", "fractured_hearts.ceiling", "fractured_hearts.floor",
                "fractured_hearts.gain_inc", "fractured_hearts.loss_inc", "fractured_hearts.permadeath",
                "fractured_hearts.heart_steal", "fractured_hearts.original_health",
                "fractured_hearts.original_gamemode", "fractured_hearts_toggle_sounds",
                "fractured_hearts_toggle_messages", "fractured_hearts_toggle_particles",
                "fractured_hearts_status")) {
            Objective existing = scoreboard.getObjective(objective);
            if (existing != null) scoreboard.removeObjective(existing);
        }
        source.sendSuccess(() -> Component.literal("Fractured Hearts cleaned up for " + tracked.size()
                + " players. Stop the server and remove the JAR before starting it again."), true);
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

    private static void applyConfig(MinecraftServer server, Config config) {
        setScore(server, "fractured_hearts.ceiling", config.maxHealth);
        setScore(server, "fractured_hearts.floor", config.minimumHealth);
        setScore(server, "fractured_hearts.gain_inc", config.healthPerApple);
        setScore(server, "fractured_hearts.loss_inc", config.healthLostPerDeath);
        setScore(server, "fractured_hearts.permadeath", config.permadeath ? 1 : 0);
        setScore(server, "fractured_hearts.heart_steal", config.pvpHeartSteal ? 1 : 0);
        syncOnlinePlayers(server);
    }

    private static void syncOnlinePlayers(MinecraftServer server) {
        runCommand(server, "execute as @a if score @s fractured_hearts.score > $fractured_hearts.fakeplayer fractured_hearts.ceiling run scoreboard players operation @s fractured_hearts.score = $fractured_hearts.fakeplayer fractured_hearts.ceiling");
        runCommand(server, "execute as @a if score @s fractured_hearts.score matches 1.. run function fractured_hearts:config/sync_health");
    }

    private static void setScore(MinecraftServer server, String objective, int value) {
        runCommand(server, "scoreboard players set $fractured_hearts.fakeplayer " + objective + " " + value);
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
