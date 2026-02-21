package com.roguegame.datalayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.roguegame.domain.LeaderboardService;
import com.roguegame.domain.ScoreEntry;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Leaderboard хранится в JSON файле: массив объектов ScoreEntry.
 * Путь: ~/.roguegame/leaderboard.json
 */
public class JsonLeaderboardService implements LeaderboardService {

    private static final String FILE_NAME = "leaderboard.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Type listType = new TypeToken<List<ScoreEntry>>(){}.getType();
    private final Path filePath;

    public JsonLeaderboardService() {
        Path dir = Paths.get(System.getProperty("user.home"), ".roguegame");
        this.filePath = dir.resolve(FILE_NAME);
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
    }

    @Override
    public List<ScoreEntry> loadLeaderboard() {
        try {
            if (!Files.exists(filePath)) return new ArrayList<>();
            String json = Files.readString(filePath, StandardCharsets.UTF_8).trim();
            if (json.isEmpty()) return new ArrayList<>();
            List<ScoreEntry> list = gson.fromJson(json, listType);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveScore(ScoreEntry score) {
        if (score == null) return;

        List<ScoreEntry> list = loadLeaderboard();
        list.add(score);

        try {
            Files.writeString(filePath, gson.toJson(list, listType), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void clear() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    public Path getFilePath() {
        return filePath;
    }
}
