package com.roguegame.domain;

import java.util.*;

import com.roguegame.domain.GameMap.Direction;

public class World {
    private List<Enemy> enemies;
    private List<Item> items;
    private Character player;
    private DungeonLevel level;
    private int levelNumber;
    private Map<DungeonLevel.Position, DungeonLevel.DoorColor> keys;
    private double struggleCounter;

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Character getPlayer() {
        return player;
    }

    public DungeonLevel getLevel() {
        return level;
    }

    public void setLevel(DungeonLevel level) {
        this.level = level;
    }

    public Map<DungeonLevel.Position, DungeonLevel.DoorColor> getKeys() {
        return keys;
    }

    public double getStruggleCounter() {
        return struggleCounter;
    }

    public void setStruggleCounter(double struggleCounter) {
        this.struggleCounter = struggleCounter;
    }

    public World() {
        level = new DungeonLevel();
        keys = level.getKeysOnGround();
        levelNumber = 1;
        struggleCounter = 0.5;
        DungeonLevel.Position start = level.getStartPosition();
        player = new Character(start.x, start.y);
        if (!player.isAlive()) {
            player = new Character(start.x, start.y);
            player.setAlive(true);
        }
        LevelGenerator lg = new LevelGenerator(level);
        enemies = lg.generateLevelEnemies(levelNumber, struggleCounter, player);
        items = lg.generateLevelItems(levelNumber, struggleCounter);
    }

    public void initNewLevel() {
        level = new DungeonLevel();
        setLevel(level);
        if (!player.isAlive()) {
            player = new Character(level.getStartPosition().x, level.getStartPosition().y);
            player.setAlive(true);
            player.getBackpack().addItem(new Item(ItemTypes.Subtype.FOOD_SMALL, 0, 0));
            player.getBackpack().addItem(new Item(ItemTypes.Subtype.SWORD, 0, 0));
            player.getBackpack().addItem(new Item(ItemTypes.Subtype.MEDKIT_SMALL, 0, 0));
            setLevelNumber(1);
        }
        player.getKeys().clear();
        items.clear();
        enemies.clear();
        LevelGenerator lg = new LevelGenerator(level);
        setItems(lg.generateLevelItems(levelNumber, struggleCounter));
        setEnemies(lg.generateLevelEnemies(levelNumber, struggleCounter, player));
        scaleEnemiesStrength(levelNumber, enemies);
    }

    public Enemy getEnemyAtPosition(int x, int y) {
        for (Enemy e : getEnemies()) {
            if (x == e.getPosX() && y == e.getPosY()) {
                return e;
            }
        }
        return null;
    }

    public boolean isEnemyOccupied(int x, int y) {
        return getEnemyAtPosition(x, y) != null;
    }

    public boolean tryToMove(Entity e, Direction dir) {
        int newPosY = e.getPosY();
        int newPosX = e.getPosX();
        switch (dir) {
            case UP: newPosY--; break;
            case DOWN: newPosY++; break;
            case LEFT: newPosX--; break;
            case RIGHT: newPosX++; break;
            case DIAGONALLY_DOWN_LEFT: newPosY++; newPosX--; break;
            case DIAGONALLY_DOWN_RIGHT: newPosY++; newPosX++; break;
            case DIAGONALLY_UP_LEFT: newPosY--; newPosX--; break;
            case DIAGONALLY_UP_RIGHT: newPosY--; newPosX++; break;
            case STOP: return false;
        }
        if (e == player) {
            if (getLevel().getTile(newPosX, newPosY) == GameMap.TileType.DOOR) {
                if (!getLevel().tryOpenDoor(newPosX, newPosY, player.getKeys())) return false;
            }
            Enemy mimic = getMimicAtPosition(newPosX, newPosY);
            if (mimic != null && !mimic.isMimicRevealed()) {
                mimic.setMimicRevealed(true);
                return false;
            }
            Enemy other = getEnemyAtPosition(newPosX, newPosY);
            if (other != null) {
                handleCombat((Character) e, other);
                removeDead();
                return false;
            }
        }
        if (!level.isWalkable(newPosX, newPosY) || isTileOccupiedFor(e, newPosX, newPosY)) {
            return false;
        }
        e.moveCharacterByDirection(dir);
        if (e == player) {
            tryToPickUp((Character) e);
        }
        return true;
    }

    private void handleCombat(Character player, Enemy enemy) {
        Combat c = new Combat();
        TurnManager playerTurn = new TurnManager(TurnManager.Turn.PLAYER);
        c.attack(player, enemy, playerTurn);
        if (enemy.getHealth() <= 0) {
           enemy.setAlive(false);
           player.setEnemiesKilled(player.getEnemiesKilled() + 1);
        }
    }

    public boolean removeDead() {
        if (enemies.removeIf(e -> !e.isAlive())) return true;
        return false;
    }

    public boolean tryToTeleport(Enemy e, int x, int y) {
        if (!level.isWalkable(x, y) || isTileOccupiedFor(e, x, y)) {
            return false;
        }
        e.setPosX(x);
        e.setPosY(y);
        return true;
    }

    private Enemy getMimicAtPosition(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.getType() == Enemy.Type.MIMIC) {
                if (enemy.getPosX() == x && enemy.getPosY() == y) {
                    return enemy;
                }
            }
        }
        return null;
    }

    public void tryToPickUp(Character player){
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.isItemOnGround() && item.getPosX() == player.getPosX()
                    && item.getPosY() == player.getPosY()) {
                player.getBackpack().addItem(item);
                item.pickItemUpBy(player);
                it.remove();
            }
        }
        Optional<DungeonLevel.DoorColor> key = getLevel().pickupKeyAt(player.getPosX(), player.getPosY());
        key.ifPresent(color -> {
            player.addKey(color);
        });
    }

    public boolean isTileOccupiedFor(Entity e, int x, int y) {
        if (e instanceof Enemy) {
            if (player.getPosY() == y && player.getPosX() == x) {
                return true;
            }
            for (Enemy enemy : enemies) {
                if (enemy.getPosX() == x && enemy.getPosY() == y) {
                    return true;
                }
            }
        }
        if (e == player && isEnemyOccupied(x, y)) {
                return true;
        }
        return false;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public List<Entity> calculateTurn(Character player, List<Enemy> enemies) {
        List<Entity> turnOrder = new ArrayList<>();
        turnOrder.add(player);
        turnOrder.addAll(enemies);
        turnOrder.sort((a, b) -> Integer.compare(b.getSpeed() + b.getAgility(), a.getSpeed() + a.getAgility()));
        return turnOrder;
    }

    public boolean dropItemNear(Character player, Item weapon) {
        Direction[] directions = {
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        };
        for(Direction dir : directions) {
            int ny = player.getPosY();
            int nx = player.getPosX();
            switch (dir) {
                case UP -> ny--;
                case DOWN -> ny++;
                case LEFT -> nx--;
                case RIGHT -> nx++;
            }
            if (!getLevel().isWalkable(nx, ny)) continue;
            if (isTileBlocked(nx, ny)) continue;
            weapon.dropItemAt(nx, ny);
            items.add(weapon);
            return true;
        }
        return false;
    }

    private boolean isTileBlocked(int x, int y) {
        for (Item i : items) {
            if (i.getPosX() == x && i.getPosY() == y) {
                return true;
            }
        }
        for (Enemy e : enemies) {
            if (e.getPosX() == x && e.getPosY() == y) {
                return true;
            }
        }
        return false;
    }

    public void calculateStruggle() {
        double ratio = player.getHealth() / (double) player.getMaximumHealth();
        double difficulty = 1.0 - ratio;
        //not to drop instantly to easiest struggle
        double smoothing = 0.1;
        struggleCounter += (difficulty - ratio) * smoothing;
        struggleCounter = Math.max(0.0, Math.min(1.0, struggleCounter));
    }

    public void scaleEnemiesStrength(int level, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            int scaledStrength = (int) Math.round(e.getType().getStrength() * (level * 0.35));
            e.setStrength(e.getStrength() + scaledStrength);
        }
    }
}