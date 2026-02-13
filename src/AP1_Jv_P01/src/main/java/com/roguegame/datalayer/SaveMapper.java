package com.roguegame.datalayer;

import java.util.*;

public final class SaveMapper {
    private SaveMapper() {}


    @SuppressWarnings("unchecked")
    public static SaveModels.SaveFile fromJsonRoot(Object root) {
        if (!(root instanceof Map<?, ?> mm)) return new SaveModels.SaveFile();
        Map<String, Object> m = (Map<String, Object>) mm;

        SaveModels.SaveFile file = new SaveModels.SaveFile();
        file.version = intVal(m.get("version"), SaveModels.VERSION);
        file.savedAt = longVal(m.get("savedAt"), 0L);

        Object sess = m.get("session");
        if (sess instanceof Map<?, ?> s) {
            file.session = fromSession((Map<String, Object>) s);
        }

        Object attempts = m.get("attempts");
        if (attempts instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> a) {
                    file.attempts.add(fromAttempt((Map<String, Object>) a));
                }
            }
        }
        return file;
    }

    public static Object toJsonRoot(SaveModels.SaveFile file) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", file.version);
        root.put("savedAt", file.savedAt);

        root.put("session", file.session == null ? null : toSessionMap(file.session));

        List<Object> attempts = new ArrayList<>();
        for (SaveModels.Attempt a : file.attempts) {
            attempts.add(toAttemptMap(a));
        }
        root.put("attempts", attempts);

        return root;
    }

    // ---------- Session ----------

    private static SaveModels.Session fromSession(Map<String, Object> m) {
        SaveModels.Session s = new SaveModels.Session();
        s.levelNumber = intVal(m.get("levelNumber"), 1);
        s.score = intVal(m.get("score"), 0);

        s.player = fromPlayer(asMap(m.get("player")));
        s.dungeon = fromDungeon(asMap(m.get("dungeon")));

        Map<String, Object> pp = asMap(m.get("playerPos"));
        s.playerPos = new SaveModels.Pos(intVal(pp.get("x"), 1), intVal(pp.get("y"), 1));
        return s;
    }

    private static Map<String, Object> toSessionMap(SaveModels.Session s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("levelNumber", s.levelNumber);
        m.put("score", s.score);
        m.put("player", toPlayerMap(s.player));
        m.put("dungeon", toDungeonMap(s.dungeon));
        m.put("playerPos", Map.of("x", s.playerPos.x, "y", s.playerPos.y));
        return m;
    }

    // ---------- Player ----------

    private static SaveModels.Player fromPlayer(Map<String, Object> m) {
        SaveModels.Player p = new SaveModels.Player();
        p.maxHp = intVal(m.get("maxHp"), 10);
        p.hp = intVal(m.get("hp"), 10);
        p.agility = intVal(m.get("agility"), 3);
        p.baseStrength = intVal(m.get("baseStrength"), 3);
        p.exp = intVal(m.get("exp"), 0);
        p.gold = intVal(m.get("gold"), 0);
        p.weaponSubtype = strVal(m.get("weaponSubtype"), "FISTS");

        Object items = m.get("backpackItems");
        if (items instanceof List<?> list) {
            for (Object o : list) p.backpackItems.add(String.valueOf(o));
        }
        return p;
    }

    private static Map<String, Object> toPlayerMap(SaveModels.Player p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("maxHp", p.maxHp);
        m.put("hp", p.hp);
        m.put("agility", p.agility);
        m.put("baseStrength", p.baseStrength);
        m.put("exp", p.exp);
        m.put("gold", p.gold);
        m.put("weaponSubtype", p.weaponSubtype);
        m.put("backpackItems", new ArrayList<>(p.backpackItems));
        return m;
    }

    // ---------- Dungeon ----------

    private static SaveModels.Dungeon fromDungeon(Map<String, Object> m) {
        SaveModels.Dungeon d = new SaveModels.Dungeon();
        d.width = intVal(m.get("width"), 78);
        d.height = intVal(m.get("height"), 21);

        Object rows = m.get("rows");
        if (rows instanceof List<?> list) {
            for (Object o : list) d.rows.add(String.valueOf(o));
        }

        Map<String, Object> st = asMap(m.get("start"));
        d.start = new SaveModels.Pos(intVal(st.get("x"), 1), intVal(st.get("y"), 1));

        Map<String, Object> ex = asMap(m.get("exit"));
        d.exit = new SaveModels.Pos(intVal(ex.get("x"), 1), intVal(ex.get("y"), 1));

        Object doors = m.get("doors");
        if (doors instanceof List<?> list) {
            for (Object o : list) {
                Map<String, Object> dm = asMap(o);
                SaveModels.Door dd = new SaveModels.Door();
                Map<String, Object> pos = asMap(dm.get("pos"));
                dd.pos = new SaveModels.Pos(intVal(pos.get("x"), 1), intVal(pos.get("y"), 1));
                dd.color = strVal(dm.get("color"), "RED");
                dd.locked = boolVal(dm.get("locked"), false);
                d.doors.add(dd);
            }
        }

        Object keys = m.get("keys");
        if (keys instanceof List<?> list) {
            for (Object o : list) {
                Map<String, Object> km = asMap(o);
                SaveModels.Key kk = new SaveModels.Key();
                Map<String, Object> pos = asMap(km.get("pos"));
                kk.pos = new SaveModels.Pos(intVal(pos.get("x"), 1), intVal(pos.get("y"), 1));
                kk.color = strVal(km.get("color"), "RED");
                d.keys.add(kk);
            }
        }

        return d;
    }

    private static Map<String, Object> toDungeonMap(SaveModels.Dungeon d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("width", d.width);
        m.put("height", d.height);
        m.put("rows", new ArrayList<>(d.rows));
        m.put("start", Map.of("x", d.start.x, "y", d.start.y));
        m.put("exit", Map.of("x", d.exit.x, "y", d.exit.y));

        List<Object> doors = new ArrayList<>();
        for (SaveModels.Door dd : d.doors) {
            doors.add(Map.of(
                    "pos", Map.of("x", dd.pos.x, "y", dd.pos.y),
                    "color", dd.color,
                    "locked", dd.locked
            ));
        }
        m.put("doors", doors);

        List<Object> keys = new ArrayList<>();
        for (SaveModels.Key kk : d.keys) {
            keys.add(Map.of(
                    "pos", Map.of("x", kk.pos.x, "y", kk.pos.y),
                    "color", kk.color
            ));
        }
        m.put("keys", keys);

        return m;
    }

    // ---------- Attempt ----------

    private static SaveModels.Attempt fromAttempt(Map<String, Object> m) {
        SaveModels.Attempt a = new SaveModels.Attempt();
        a.finishedAt = longVal(m.get("finishedAt"), 0L);
        a.score = intVal(m.get("score"), 0);
        a.levelReached = intVal(m.get("levelReached"), 1);
        a.success = boolVal(m.get("success"), false);
        return a;
    }

    private static Map<String, Object> toAttemptMap(SaveModels.Attempt a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("finishedAt", a.finishedAt);
        m.put("score", a.score);
        m.put("levelReached", a.levelReached);
        m.put("success", a.success);
        return m;
    }

    // ---------- helpers ----------

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> mm) return (Map<String, Object>) mm;
        return new LinkedHashMap<>();
    }

    private static int intVal(Object o, int def) {
        if (o instanceof Number n) return n.intValue();
        return def;
    }

    private static long longVal(Object o, long def) {
        if (o instanceof Number n) return n.longValue();
        return def;
    }

    private static boolean boolVal(Object o, boolean def) {
        if (o instanceof Boolean b) return b;
        return def;
    }

    private static String strVal(Object o, String def) {
        return o == null ? def : String.valueOf(o);
    }
}
