package com.roguegame.datalayer;

import com.roguegame.domain.World;

import java.util.Optional;

public class SaveGameService {

    private final JsonSaveGameService repo = new JsonSaveGameService();

    public void save(World world, int movesMade) {
        SaveGameModels.SaveFile file = SaveGameMapper.toSaveFile(world, movesMade);
        repo.save(file);
    }

    public Optional<Integer> loadInto(World world) {
        return repo.load().map(file -> SaveGameMapper.applyToWorld(world, file));
    }

    public boolean hasSave() {
        return repo.hasSave();
    }

    public void clear() {
        repo.clear();
    }
}