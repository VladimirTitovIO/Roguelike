package com.roguegame.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.roguegame.domain.DungeonLevel;
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

                renderTile(g, tile, x, y, isSeen, isVisibleNow, controller.getLevel());
            }
        }
        renderKeys(g, controller);
    }

    private static void renderTile(TextGraphics g, TileType tile, int x, int y,
                                   boolean isSeen, boolean isVisibleNow, DungeonLevel level) {
        if (isVisibleNow) {
            drawVisibleTile(g, tile, x, y, level);
        } else if (isSeen) {
            drawExploredTile(g, tile, x, y);
        } else {
            drawUnexploredTile(g, x, y);
        }
    }

    private static void drawVisibleTile(TextGraphics g, TileType tile, int x, int y, DungeonLevel level) {
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
                renderDoor(g, x, y, level);
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
    public static void renderUIPanel(TextGraphics g) {
        int x = LEVEL_WIDTH + 2;
        int y = 1;

        g.putString(x, y++, "LVL: 1");
        g.putString(x, y++, "Gold: 88");
        g.putString(x, y++, "Health: 188.00/500");
        g.putString(x, y++, "Agility: 70");
        g.putString(x, y++, "Strength: 70");
        y++;

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        g.putString(x, y++, "Backpack:");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(x, y++, "Food: 3");
        g.putString(x, y++, "Elixirs: 3");
        g.putString(x, y++, "Weapons: 3");
        g.putString(x, y, "Scrolls: 3");
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
