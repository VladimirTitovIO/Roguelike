package com.roguegame.datalayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class SaveRepository {
    private final Path savePath;

    public SaveRepository(Path savePath) {
        this.savePath = savePath;
    }

    public SaveModels.SaveFile loadOrEmpty() {
        if (!Files.exists(savePath)) {
            return new SaveModels.SaveFile();
        }
        try {
            String json = Files.readString(savePath, StandardCharsets.UTF_8);
            Object parsed = Json.parse(json);
            return SaveMapper.fromJsonRoot(parsed);
        } catch (Exception e) {
            // если файл битый — начинаем заново (но можно и пробрасывать)
            return new SaveModels.SaveFile();
        }
    }

    public void save(SaveModels.SaveFile file) throws IOException {
        file.savedAt = System.currentTimeMillis();
        String json = Json.stringify(SaveMapper.toJsonRoot(file));
        Files.createDirectories(savePath.getParent() == null ? Paths.get(".") : savePath.getParent());
        Files.writeString(savePath, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void saveSession(SaveModels.Session session) throws IOException {
        SaveModels.SaveFile file = loadOrEmpty();
        file.session = session;
        save(file);
    }

    public Optional<SaveModels.Session> loadLastSession() {
        SaveModels.SaveFile file = loadOrEmpty();
        return Optional.ofNullable(file.session);
    }

    public void addAttempt(SaveModels.Attempt attempt) throws IOException {
        SaveModels.SaveFile file = loadOrEmpty();
        file.attempts.add(attempt);

        // сортируем: лучшие сверху (по score), если равны — по levelReached
        file.attempts.sort((a, b) -> {
            int c = Integer.compare(b.score, a.score);
            if (c != 0) return c;
            return Integer.compare(b.levelReached, a.levelReached);
        });

        // можно ограничить топ-20
        if (file.attempts.size() > 20) {
            file.attempts = new ArrayList<>(file.attempts.subList(0, 20));
        }
        save(file);
    }

    public List<SaveModels.Attempt> loadLeaderboard() {
        return List.copyOf(loadOrEmpty().attempts);
    }

    public void clearSession() throws IOException {
        SaveModels.SaveFile file = loadOrEmpty();
        file.session = null;
        save(file);
    }
}
