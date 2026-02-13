package com.roguegame.domain;

import java.util.ArrayList;
import java.util.List;
import com.roguegame.domain.GameMap.Direction;

public class World {
    private final List<Enemy> enemies;
    private final List<Item> items;
    private final Character player;
    private final DungeonLevel level;
    private int levelNumber;


    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Item> getItems() {
        return items;
    }

    public Character getPlayer() {
        return player;
    }

    public DungeonLevel getLevel() {
        return level;
    }

    public World(List<Enemy> enemies, List<Item> items, Character player, DungeonLevel level) {
        this.enemies = enemies;
        this.items = items;
        this.player = player;
        this.level = level;
        levelNumber = 0;
    }

    public Enemy getEnemyAtPosition(int x, int y) {
        for (Enemy e : enemies) {
            if (x >= e.getPosX() && x < e.getPosX() + e.getWidth()
                    && y >= e.getPosY() && y < e.getPosY() + e.getHeight()) {
                return e;
            }
        }
        return null;
    }

    public boolean isEnemyOccupied(int x, int y) {
        return getEnemyAtPosition(x, y) != null;
    }

    public int getEntityRoom(Entity e) {
        return level.getRoomAt(e.getPosX(), e.getPosY());
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
            case STOP: break;
        }
        for (int y = newPosY; y < newPosY + e.getHeight(); y++) {
            for (int x = newPosX; x < newPosX + e.getWidth(); x++) {
                if (!level.isWalkable(x, y) || isTileOccupiedFor(e, x, y)) {
                    return false;
                }
            }
        }
        e.moveCharacterByDirection(dir);
        tryToPickUp((Character) e);
        return true;
    }

    public void tryToPickUp(Character player){
        for (Item item : items) {
            if (item.isItemOnGround() && item.getPosX() == player.getPosX()
                    && item.getPosY() == player.getPosY()) {
                player.getBackpack().addItem(item);
                item.pickItemUpBy(player);
                item.setPosX(0);
                item.setPosY(0);
            }
        }
    }

    public boolean isTileOccupiedFor(Entity e, int x, int y) {
        boolean occupied = false;
        if (e != player) {
            if (player.getPosY() == y && player.getPosX() == x) {
                occupied = true;
            }
        }
        if (e == player) {
            for (Enemy enemy : enemies) {
                if (isEnemyOccupied(x, y))
                    occupied = true;
            }
        }
        return occupied;
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
}
