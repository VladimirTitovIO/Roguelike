package com.roguegame.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private final DungeonLevel level;
    private final Random random = new Random();

    public DungeonLevel getLevel() {
        return level;
    }

    public LevelGenerator(DungeonLevel level) {
        this.level = level;
    }

    public List<Item> generateLevelItems(int level, double struggleCounter) {
        List<Item> itemsAtLevel = new ArrayList<>();
        List<ItemTypes.Subtype> pool = buildWeightedPool();
        int baseItems = 5 + level / 3; //9 rooms
//        int extraItems = (int) Math.round(level * struggleCounter);
        int extraItems = (int) Math.round(3 + struggleCounter * 6);
        int totalItems = baseItems + extraItems;
        for (int i = 0; i < totalItems; i++) {
            if (struggleCounter > 0.7) {
                itemsAtLevel.add(new Item(buildSubtypeByWeight2(ItemTypes.Subtype.HIGH_STRUGGLE, random), 0, 0));
            }
            else if (struggleCounter < 0.3) {
                itemsAtLevel.add(new Item(buildSubtypeByWeight2(ItemTypes.Subtype.LOW_STRUGGLE, random), 0, 0));
            }
            else {
                itemsAtLevel.add(new Item(buildSubtypeByWeight2(ItemTypes.Subtype.BALANCED_STRUGGLE, random), 0, 0));
            }
        }
        for (Item i : itemsAtLevel) {
            int room = random.nextInt(0, 9);
            i.setPosX(getLevel().getRandomRoomX(room));
            i.setPosY(getLevel().getRandomRoomY(room));
        }
        return itemsAtLevel;
    }

    public ItemTypes.Subtype buildSubtypeByWeight2(ItemTypes.Subtype[] subtype, Random random) {
        int totalWeight = 0;
        for (ItemTypes.Subtype s : subtype) {
            totalWeight += s.getWeight();
        }
        int roll = random.nextInt(totalWeight);
        int itemWeight = 0;
        for (ItemTypes.Subtype s : subtype) {
            itemWeight += s.getWeight();
            if (roll < itemWeight) {
                return s;
            }
        }
        return subtype[0];

    }

    private List<ItemTypes.Subtype> buildWeightedPool() {
        List<ItemTypes.Subtype> pool = new ArrayList<>();
        for (ItemTypes.Subtype s : ItemTypes.Subtype.values()) {
            if (s == ItemTypes.Subtype.FISTS) continue;
            for (int i = 0; i < s.getWeight(); i++) {
                pool.add(s);
            }
        }
        return pool;
    }

    public List<Enemy> generateLevelEnemies(int level, double struggleCounter, Character player) {
        List<Enemy> enemiesAtLevel = new ArrayList<>();
        int playerRoom = getLevel().getRoomAt(player.getPosX(), player.getPosY());
        int baseEnemies = 6;
        double ratio = 1.0 - struggleCounter;
        int extraEnemies = (int) Math.round(level * ratio * 0.5);
        int totalEnemies = baseEnemies + extraEnemies;
        Enemy.Type randomEnemy = null;
        for (int i = 0; i < totalEnemies; i++) {
            int room = random.nextInt(0, 9);
            while (room == playerRoom) room = random.nextInt(0, 9);
            if (level < 3) {
                randomEnemy = Enemy.Type.getEasyEnemy();
            }
            if (level >= 3 && struggleCounter >= 0.4) {
                randomEnemy = Enemy.Type.values()[random.nextInt(Enemy.Type.values().length)];
            } else {
                randomEnemy = Enemy.Type.getEasyEnemy();
            }
            if (randomEnemy != null) {
                enemiesAtLevel.add(new Enemy(randomEnemy,
                        getLevel().getRandomRoomX(room), getLevel().getRandomRoomY(room)));
            }
        }
        return enemiesAtLevel;
    }
}
