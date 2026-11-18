package io.github.fracturedhearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** World-owned heart data. It is inert if the server starts without this mod. */
final class HeartStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] OLD_OBJECTIVES = {
            "fractured_hearts.score", "fractured_hearts.hearts", "fractured_hearts.death",
            "fractured_hearts.toggle", "fractured_hearts.ceiling", "fractured_hearts.floor",
            "fractured_hearts.gain_inc", "fractured_hearts.loss_inc", "fractured_hearts.permadeath",
            "fractured_hearts.heart_steal", "fractured_hearts.original_health",
            "fractured_hearts.original_gamemode", "fractured_hearts_toggle_sounds",
            "fractured_hearts_toggle_messages", "fractured_hearts_toggle_particles",
            "fractured_hearts_toggle_permadeath", "fractured_hearts_status"
    };

    static final class PlayerData {
        int points;
        boolean excluded;
        boolean soundsOff;
        boolean messagesOff;
        boolean particlesOff;
        boolean permadead;
        boolean pendingLossNotice;
        Double legacyBase;
        Integer legacyScore;

        PlayerData() { }

        PlayerData(int points) {
            this.points = points;
        }
    }

    private final Path path;
    private Map<String, PlayerData> players = new HashMap<>();
    private Map<String, PlayerData> pendingNames = new HashMap<>();

    private HeartStore(Path path) {
        this.path = path;
    }

    static HeartStore load(MinecraftServer server) throws IOException {
        Path path = server.getWorldPath(LevelResource.LEVEL_DATA_FILE).getParent()
                .resolve("data/fractured-hearts.json");
        HeartStore store = new HeartStore(path);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Saved saved = GSON.fromJson(reader, Saved.class);
                if (saved == null || saved.players == null || saved.pendingNames == null)
                    throw new IOException("Invalid Fractured Hearts world data");
                store.players = saved.players;
                store.pendingNames = saved.pendingNames;
            }
        }
        return store;
    }

    PlayerData get(ServerPlayer player) {
        return players.get(player.getUUID().toString());
    }

    PlayerData getOrCreate(ServerPlayer player, int defaultPoints) throws IOException {
        String key = player.getUUID().toString();
        PlayerData data = players.get(key);
        if (data != null) return data;
        data = pendingNames.remove(player.getName().getString().toLowerCase(Locale.ROOT));
        if (data == null) data = new PlayerData(defaultPoints);
        players.put(key, data);
        save();
        return data;
    }

    boolean hasPendingLegacyPlayers() {
        return pendingNames.values().stream().anyMatch(data -> data.legacyBase != null);
    }

    boolean hasOfflineCleanup(MinecraftServer server) {
        for (var entry : players.entrySet()) {
            if (entry.getValue().legacyBase == null) continue;
            boolean online = server.getPlayerList().getPlayers().stream()
                    .anyMatch(player -> player.getUUID().toString().equals(entry.getKey()));
            if (!online) return true;
        }
        return false;
    }

    void importAndDropScoreboards(MinecraftServer server) throws IOException {
        var scoreboard = server.getScoreboard();
        Objective health = scoreboard.getObjective("fractured_hearts.score");
        if (health != null) {
            for (var entry : scoreboard.listPlayerScores(health)) {
                String name = entry.owner();
                if (name.startsWith("$") || name.startsWith("#")) continue;
                String key = name.toLowerCase(Locale.ROOT);
                PlayerData data = pendingNames.computeIfAbsent(key, ignored -> new PlayerData(entry.value()));
                data.points = entry.value();
                data.excluded = entry.value() == 0;
                data.legacyScore = entry.value();
                // The original datapack wrote the score into the vanilla base.
                // Its expected base before installation was 20 health points.
                data.legacyBase = 20.0;
            }
            // Commit the migration before deleting the only copy of the old scores.
            save();
        }
        for (String name : OLD_OBJECTIVES) {
            Objective objective = scoreboard.getObjective(name);
            if (objective != null) scoreboard.removeObjective(objective);
        }
    }

    void save() throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            GSON.toJson(new Saved(players, pendingNames), writer);
        }
        try {
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    void delete() throws IOException {
        Files.deleteIfExists(path);
    }

    private record Saved(Map<String, PlayerData> players, Map<String, PlayerData> pendingNames) { }
}
