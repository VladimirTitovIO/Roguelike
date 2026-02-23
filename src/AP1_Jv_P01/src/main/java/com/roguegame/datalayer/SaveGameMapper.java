package com.roguegame.datalayer;

import com.roguegame.domain.World;
import com.roguegame.domain.DungeonLevel;
import com.roguegame.domain.GameMap;
import com.roguegame.domain.Enemy;
import com.roguegame.domain.Item;
import com.roguegame.domain.ItemTypes;
import com.roguegame.domain.Character;

import java.util.*;

public final class SaveGameMapper {
    private SaveGameMapper() {}

    // ---------------------------
    // SAVE: World -> SaveFile
    // ---------------------------
    public static SaveGameModels.SaveFile toSaveFile(World world, int movesMade) {
        SaveGameModels.SaveFile file = new SaveGameModels.SaveFile();
        file.world = new SaveGameModels.SaveWorld();

        file.world.levelNumber = world.getLevelNumber();
        file.world.struggleCounter = world.getStruggleCounter();

        // Level
        file.world.level = toSaveLevel(world.getLevel());

        // Player
        file.world.player = toSavePlayer(world.getPlayer(), movesMade);

        // Enemies
        for (Enemy e : world.getEnemies()) {
            file.world.enemies.add(toSaveEnemy(e));
        }

        // Items on map
        for (Item it : world.getItems()) {
            file.world.items.add(toSaveItem(it));
        }

        return file;
    }

    private static SaveGameModels.SaveLevel toSaveLevel(DungeonLevel level) {
        SaveGameModels.SaveLevel s = new SaveGameModels.SaveLevel();
        s.width = level.getWidth();
        s.height = level.getHeight();

        s.tilesOrdinal = new int[s.width][s.height];
        for (int x = 0; x < s.width; x++) {
            for (int y = 0; y < s.height; y++) {
                s.tilesOrdinal[x][y] = level.getTile(x, y).ordinal(); // GameMap.TileType
            }
        }

        DungeonLevel.Position start = level.getStartPosition();
        DungeonLevel.Position exit = level.getExitPosition();
        s.startX = start.x; s.startY = start.y;
        s.exitX  = exit.x;  s.exitY  = exit.y;

        // Doors
        for (var entry : level.getDoors().entrySet()) {
            DungeonLevel.Position p = entry.getKey();
            DungeonLevel.Door d = entry.getValue();
            SaveGameModels.SaveDoor sd = new SaveGameModels.SaveDoor();
            sd.x = p.x; sd.y = p.y;
            sd.color = d.color;
            sd.locked = d.locked;
            s.doors.add(sd);
        }

        // Keys on ground
        for (var entry : level.getKeysOnGround().entrySet()) {
            DungeonLevel.Position p = entry.getKey();
            DungeonLevel.DoorColor c = entry.getValue();
            SaveGameModels.SaveKeyOnGround sk = new SaveGameModels.SaveKeyOnGround();
            sk.x = p.x; sk.y = p.y;
            sk.color = c;
            s.keysOnGround.add(sk);
        }

        return s;
    }

    private static SaveGameModels.SavePlayer toSavePlayer(Character player, int movesMade) {
        SaveGameModels.SavePlayer p = new SaveGameModels.SavePlayer();

        p.x = player.getPosX();
        p.y = player.getPosY();

        p.hp = player.getHealth();
        p.maxHp = player.getMaximumHealth();
        p.strength = player.getStrength();
        p.agility = player.getAgility();
        p.gold = player.getGold();

        p.keys = new HashSet<>(player.getKeys());

        // Backpack: сохраняем все предметы как subtypes
        for (Item it : player.getBackpack().getItems()) {
            p.backpackItems.add(it.getSubtype());
        }

        // equipped weapon subtype (важно)
        if (player.getWeapon() != null) {
            p.equippedWeapon = player.getWeapon().getSubtype();
        } else {
            p.equippedWeapon = ItemTypes.Subtype.FISTS;
        }

        // combat stats
        p.attacksLanded = player.getAttacksLanded();
        p.attacksMissed = player.getAttacksMissed();
        p.enemiesKilled = player.getEnemiesKilled();
        p.movesMade = movesMade;

        return p;
    }

    private static SaveGameModels.SaveEnemy toSaveEnemy(Enemy e) {
        SaveGameModels.SaveEnemy se = new SaveGameModels.SaveEnemy();
        se.type = e.getType().name();
        se.x = e.getPosX();
        se.y = e.getPosY();
        se.health = e.getHealth();
        se.strength = e.getStrength();
        se.visible = e.isVisible();
        se.inCombat = e.isInCombat();
        se.mimicRevealed = e.isMimicRevealed();
        return se;
    }

    private static SaveGameModels.SaveItem toSaveItem(Item it) {
        SaveGameModels.SaveItem si = new SaveGameModels.SaveItem();
        si.subtype = it.getSubtype();
        si.x = it.getPosX();
        si.y = it.getPosY();
        return si;
    }

    // ---------------------------
    // LOAD: SaveFile -> apply to World
    // ---------------------------
    public static int applyToWorld(World world, SaveGameModels.SaveFile file) {
        if (file == null || file.world == null) throw new IllegalArgumentException("Empty save file");

        // levelNumber + struggle
        world.setLevelNumber(file.world.levelNumber);
        world.setStruggleCounter(file.world.struggleCounter);

        // Level restore
        DungeonLevel loadedLevel = fromSaveLevel(file.world.level);
        world.setLevel(loadedLevel); // важно: setLevel обновляет keys

        // Items + enemies lists
        world.setItems(fromSaveItems(file.world.items));
        world.setEnemies(fromSaveEnemies(file.world.enemies));

        // Player restore (мутация существующего игрока, потому что в World нет setPlayer)
        int movesMade = applyToPlayer(world.getPlayer(), file.world.player);

        return movesMade;
    }

    private static DungeonLevel fromSaveLevel(SaveGameModels.SaveLevel s) {
        DungeonLevel level = new DungeonLevel(); // создаст рандомный, но мы тут же перезатрем

        // tiles
        GameMap.TileType[][] tiles = new GameMap.TileType[s.width][s.height];
        GameMap.TileType[] values = GameMap.TileType.values();
        for (int x = 0; x < s.width; x++) {
            for (int y = 0; y < s.height; y++) {
                int ord = s.tilesOrdinal[x][y];
                tiles[x][y] = values[Math.max(0, Math.min(ord, values.length - 1))];
            }
        }

        DungeonLevel.Position start = new DungeonLevel.Position(s.startX, s.startY);
        DungeonLevel.Position exit  = new DungeonLevel.Position(s.exitX,  s.exitY);

        // doors map
        Map<DungeonLevel.Position, DungeonLevel.Door> doors = new HashMap<>();
        for (SaveGameModels.SaveDoor d : s.doors) {
            DungeonLevel.Position p = new DungeonLevel.Position(d.x, d.y);
            DungeonLevel.Door door = new DungeonLevel.Door(d.color, d.locked);
            doors.put(p, door);
        }

        // keys on ground
        Map<DungeonLevel.Position, DungeonLevel.DoorColor> keysOnGround = new HashMap<>();
        for (SaveGameModels.SaveKeyOnGround k : s.keysOnGround) {
            DungeonLevel.Position p = new DungeonLevel.Position(k.x, k.y);
            keysOnGround.put(p, k.color);
        }

        // твой новый метод в DungeonLevel
        level.loadFromSave(tiles, start, exit, doors, keysOnGround);
        return level;
    }

    private static List<Item> fromSaveItems(List<SaveGameModels.SaveItem> items) {
        List<Item> result = new ArrayList<>();
        if (items == null) return result;
        for (SaveGameModels.SaveItem si : items) {
            result.add(new Item(si.subtype, si.x, si.y));
        }
        return result;
    }

    private static List<Enemy> fromSaveEnemies(List<SaveGameModels.SaveEnemy> enemies) {
        List<Enemy> result = new ArrayList<>();
        if (enemies == null) return result;

        for (SaveGameModels.SaveEnemy se : enemies) {
            Enemy.Type type = Enemy.Type.valueOf(se.type);
            Enemy e = new Enemy(type, se.x, se.y);

            // восстановим важное состояние
            e.setHealth(se.health);
            e.setStrength(se.strength);
            e.setVisible(se.visible);
            e.setInCombat(se.inCombat);
            e.setMimicRevealed(se.mimicRevealed);

            result.add(e);
        }
        return result;
    }

    private static int applyToPlayer(Character player, SaveGameModels.SavePlayer s) {
        if (s == null) throw new IllegalArgumentException("No player in save");

        player.setPosX(s.x);
        player.setPosY(s.y);

        player.setMaximumHealth(s.maxHp);
        player.setHealth(s.hp);

        player.setStrength(s.strength);
        player.setAgility(s.agility);
        player.setGold(s.gold);

        // keys
        player.getKeys().clear();
        player.getKeys().addAll(s.keys);

        // backpack
        player.getBackpack().clear();
        for (ItemTypes.Subtype sub : s.backpackItems) {
            player.getBackpack().addItem(new Item(sub, 0, 0));
        }

        // weapon
        ItemTypes.Subtype weaponSub = (s.equippedWeapon != null) ? s.equippedWeapon : ItemTypes.Subtype.FISTS;
        player.setWeapon(new Item(weaponSub, 0, 0));

        // stats
        player.setAttacksLanded(s.attacksLanded);
        player.setAttacksMissed(s.attacksMissed);
        player.setEnemiesKilled(s.enemiesKilled);

        return s.movesMade;
    }
}