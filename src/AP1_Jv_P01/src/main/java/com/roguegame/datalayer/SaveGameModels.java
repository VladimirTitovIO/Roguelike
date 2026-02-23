package com.roguegame.datalayer;

import com.roguegame.domain.DungeonLevel;
import com.roguegame.domain.ItemTypes;

import java.util.*;

public final class SaveGameModels {
    private SaveGameModels() {}
    public int movesMade;
    public static final int VERSION = 1;

    public static final class SaveFile {
        public int version = VERSION;
        public long savedAt;
        public SaveWorld world;
    }

    public static final class SaveWorld {
        public int levelNumber;
        public double struggleCounter;

        public SaveLevel level;
        public SavePlayer player;

        public List<SaveEnemy> enemies = new ArrayList<>();
        public List<SaveItem> items = new ArrayList<>();
    }

    public static final class SaveLevel {
        public int width;
        public int height;

        // tiles[x][y] как в DungeonLevel
        public int[][] tilesOrdinal;

        public int startX, startY;
        public int exitX, exitY;

        public List<SaveDoor> doors = new ArrayList<>();
        public List<SaveKeyOnGround> keysOnGround = new ArrayList<>();
    }

    public static final class SaveDoor {
        public int x, y;
        public DungeonLevel.DoorColor color;
        public boolean locked;
    }

    public static final class SaveKeyOnGround {
        public int x, y;
        public DungeonLevel.DoorColor color;
    }

    public static final class SavePlayer {
        public int x, y;

        public int hp;
        public int maxHp;
        public int strength;
        public int agility;
        public int gold;

        public Set<DungeonLevel.DoorColor> keys = new HashSet<>();
        public List<ItemTypes.Subtype> backpackItems = new ArrayList<>();

        public int attacksLanded;
        public int attacksMissed;
        public int enemiesKilled;
        public ItemTypes.Subtype equippedWeapon;
        public int movesMade;
    }

    public static final class SaveEnemy {
        public String type; // Enemy.Type name()
        public int x, y;

        public int health;
        public int strength;
        public int hostility;
        public int treasureValue;

        public boolean visible;
        public boolean inCombat;
        public boolean mimicRevealed;
    }

    public static final class SaveItem {
        public ItemTypes.Subtype subtype;
        public int x, y;
    }
}