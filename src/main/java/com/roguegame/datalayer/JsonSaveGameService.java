package com.roguegame.datalayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class JsonSaveGameService {

    private static final String FILE_NAME = "savegame.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;

    public JsonSaveGameService() {
        // ТУ ЖЕ директорию, что и leaderboard.json (как мы делали ранее)
        Path root = Path.of(System.getProperty("user.dir"));
        Path dir = root.resolve("src").resolve("AP1_Jv_P01");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        this.filePath = dir.resolve(FILE_NAME);
    }

    public Path getFilePath() {
        return filePath;
    }

    public void save(SaveGameModels.SaveFile data) {
        data.savedAt = System.currentTimeMillis();
        try (Writer w = new FileWriter(filePath.toFile())) {
            gson.toJson(data, w);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save game to " + filePath, e);
        }
    }

    public Optional<SaveGameModels.SaveFile> load() {
        if (!Files.exists(filePath)) return Optional.empty();
        try (Reader r = new FileReader(filePath.toFile())) {
            SaveGameModels.SaveFile data = gson.fromJson(r, SaveGameModels.SaveFile.class);
            return Optional.ofNullable(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load game from " + filePath, e);
        }
    }

    public boolean hasSave() {
        return Files.exists(filePath);
    }

    public void clear() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {}
    }
}