package com.roguegame.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.roguegame.domain.*;
import com.roguegame.domain.Character;
import com.roguegame.domain.DungeonLevel.DoorColor;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap.TileType;

/**
 * Отвечает за отрисовку игрового уровня
 */
public class GameRenderer {

    public static final int LEVEL_WIDTH = DungeonLevel.WIDTH;
    public static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT;
    public static final int VIEW_RADIUS = Controller.VIEW_RADIUS;

    /**
     * Отрисовывает игровую карту с учётом видимости и освещённости.
     */
    public static void renderMap(TextGraphics g, Controller controller) {
        for (int y = 0; y < LEVEL_HEIGHT; y++) {
            for (int x = 0; x < LEVEL_WIDTH; x++) {
                TileType tile = controller.getLevel().getTile(x, y);
                boolean isSeen = controller.isExplored(x, y);
                boolean isVisibleNow = controller.isCurrentlyVisible(x, y);

                renderTile(g, tile, x, y, isSeen, isVisibleNow, controller);
            }
        }
        renderKeys(g, controller);
    }

    private static void renderTile(TextGraphics g, TileType tile, int x, int y,
                                   boolean isSeen, boolean isVisibleNow, Controller controller) {
        if (isVisibleNow) {
            drawVisibleTile(g, tile, x, y, controller);
        } else if (isSeen) {
            drawExploredTile(g, tile, x, y);
        } else {
            drawUnexploredTile(g, x, y);
        }
    }

    private static void drawVisibleTile(TextGraphics g, TileType tile, int x, int y, Controller controller) {
        DungeonLevel.Position exit = controller.getLevel().getExitPosition();
        if (exit.x == x && exit.y == y) {
            g.setForegroundColor(TextColor.ANSI.CYAN);
            g.setCharacter(x, y, 'E');
            return;
        }
        for (Enemy e : controller.getWorld().getEnemies()) {
            if (e.getPosX() == x && e.getPosY() == y) {
                g.setForegroundColor(TextColor.ANSI.RED);
                g.setCharacter(x, y, 'O');
                return;
            }
        }
        for (Item i : controller.getWorld().getItems()) {
            if (i.getPosX() == x && i.getPosY() == y) {
                g.setForegroundColor(TextColor.ANSI.MAGENTA);
                g.setCharacter(x, y, 'I');
                return;
            }
        }
        switch (tile) {
            case WALL:
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setCharacter(x, y, '#');
                break;
            case FLOOR:
                g.setForegroundColor(TextColor.ANSI.GREEN);
                g.setCharacter(x, y, '.');
                break;
            case CORRIDOR:
                g.setForegroundColor(TextColor.ANSI.YELLOW);
                g.setCharacter(x, y, '.');
                break;
            case DOOR:
                renderDoor(g, x, y, controller.getLevel());
                break;
            default:
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setCharacter(x, y, '?');
        }
    }

    private static void renderDoor(TextGraphics g, int x, int y, DungeonLevel level) {
        Position pos = new Position(x, y);
        var door = level.getDoors().get(pos);
        if (door != null && door.locked) {
            g.setForegroundColor(getDoorColor(door.color));
            g.setCharacter(x, y, 'D');
        } else {
            g.setForegroundColor(TextColor.ANSI.CYAN);
            g.setCharacter(x, y, '+');
        }
    }

    private static void drawExploredTile(TextGraphics g, TileType tile, int x, int y) {
        if (tile == TileType.WALL) {
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.setCharacter(x, y, '#');
        } else {
            g.setForegroundColor(TextColor.ANSI.BLACK);
            g.setCharacter(x, y, ' ');
        }
    }

    private static void drawUnexploredTile(TextGraphics g, int x, int y) {
        g.setForegroundColor(TextColor.ANSI.BLACK);
        g.setCharacter(x, y, ' ');
    }

    private static void renderKeys(TextGraphics g, Controller controller) {
        controller.getLevel().getKeysOnGround().forEach((pos, color) -> {
            if (controller.isCurrentlyVisible(pos.x, pos.y)) {
                g.setForegroundColor(getKeyColor(color));
                g.setCharacter(pos.x, pos.y, 'K');
            }
        });
    }

    /**
     * Отрисовывает игрока на карте.
     */
    public static void renderPlayer(TextGraphics g, int playerX, int playerY) {
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setCharacter(playerX, playerY, '@');
    }

    /**
     * Отрисовывает панель статистики справа от карты.
     */
    public static void renderUIPanel(TextGraphics g, Controller controller) {
        int x = LEVEL_WIDTH + 2;
        int y = 1;

        Character player = controller.getPlayer();

        g.putString(x, y++, "LVL: " + controller.getWorld().getLevelNumber());
        g.putString(x, y++, "Gold: " + player.getGold());
        g.putString(x, y++, "Health: " + player.getHealth() + "/" + player.getMaximumHealth());
        g.putString(x, y++, "Agility: " + player.getAgility());
        g.putString(x, y++, "Strength: " + player.getStrength() + "(" + player.getWeapon().getSubtype() + " " + player.getWeaponStrength() +")");
        y++;

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        g.putString(x, y++, "Backpack:");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(x, y++, "Food: " + player.getBackpack().getCount(ItemTypes.Type.FOOD));
        g.putString(x, y++, "Elixirs: " + player.getBackpack().getCount(ItemTypes.Type.ELIXIR));
        g.putString(x, y++, "Weapons: " + player.getBackpack().getCount(ItemTypes.Type.WEAPON));
        g.putString(x, y, "Scrolls: " + player.getBackpack().getCount(ItemTypes.Type.SCROLLS));
        g.putString(x, y, "Medkits: " + player.getBackpack().getCount(ItemTypes.Type.MEDKIT));
    }

    // --- Вспомогательные методы ---

    private static TextColor getDoorColor(DoorColor color) {
        return switch (color) {
            case RED -> TextColor.ANSI.RED;
            case BLUE -> TextColor.ANSI.BLUE;
            case YELLOW -> TextColor.ANSI.YELLOW;
        };
    }

    private static TextColor getKeyColor(DoorColor color) {
        return switch (color) {
            case RED -> TextColor.ANSI.RED_BRIGHT;
            case BLUE -> TextColor.ANSI.BLUE_BRIGHT;
            case YELLOW -> TextColor.ANSI.YELLOW_BRIGHT;
        };
    }
}
