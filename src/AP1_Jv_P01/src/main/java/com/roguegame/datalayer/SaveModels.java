package com.roguegame.datalayer;

import java.util.*;

public final class SaveModels {
    private SaveModels() {}

    public static final int VERSION = 1;

    public static final class SaveFile {
        public int version = VERSION;
        public long savedAt;
        public Session session;              // последняя сессия
        public List<Attempt> attempts = new ArrayList<>(); // история попыток (leaderboard)
    }

    public static final class Session {
        public int levelNumber;
        public int score;
        public Player player;
        public Dungeon dungeon;
        public Pos playerPos;
    }

    public static final class Player {
        public int maxHp;
        public int hp;
        public int agility;
        public int baseStrength;
        public int exp;
        public int gold;
        public String weaponSubtype;         // ItemTypes.Subtype name
        public List<String> backpackItems = new ArrayList<>(); // list of ItemTypes.Subtype names
    }

    public static final class Dungeon {
        public int width;
        public int height;

        // каждая строка — ряд карты; символы:
        // # WALL, . FLOOR, : CORRIDOR, + DOOR
        public List<String> rows = new ArrayList<>();

        public Pos start;
        public Pos exit;

        public List<Door> doors = new ArrayList<>();
        public List<Key> keys = new ArrayList<>();
    }

    public static final class Door {
        public Pos pos;
        public String color;   // "RED"/"BLUE"/"YELLOW"
        public boolean locked;
    }

    public static final class Key {
        public Pos pos;
        public String color;
    }

    public static final class Pos {
        public int x;
        public int y;
        public Pos() {}
        public Pos(int x, int y) { this.x = x; this.y = y; }
    }

    public static final class Attempt {
        public long finishedAt;
        public int score;
        public int levelReached;
        public boolean success;
    }
}

