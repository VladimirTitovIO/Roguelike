package com.roguegame.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.roguegame.domain.*;
import com.roguegame.domain.Character;
import com.roguegame.domain.DungeonLevel.DoorColor;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap.TileType;

import java.util.Map;

/**
 * Отвечает за отрисовку игрового уровня
 */
public class GameRenderer {

    public static final int LEVEL_WIDTH = DungeonLevel.WIDTH;
    public static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT;

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
            drawExploredTile(g, tile, x, y, controller);
        } else {
            drawUnexploredTile(g, x, y);
        }
    }

    private static void drawVisibleTile(TextGraphics g, TileType tile,
                                        int x, int y, Controller controller) {
        if (isExitHere(controller, x, y)) {
            drawExit(g, x, y);
            return;
        }

        Map<Position, Item> itemMap = controller.getItemMap();
        Map<Position, Enemy> enemyMap = controller.getEnemyMap();
        Position currentPosition = new Position(x, y);
        Enemy enemy = enemyMap.get(currentPosition);
        if (enemy != null) {
            drawEnemy(g, enemy);
            return;
        }
        Item item = itemMap.get(currentPosition);
        if (item != null) {
            drawItem(g, item);
            return;
        }

        drawTile(g, tile, x, y, controller);
    }

    // выход
    private static boolean isExitHere(Controller controller, int x, int y) {
        Position exit = controller.getLevel().getExitPosition();
        return exit.x == x && exit.y == y;
    }

    private static void drawExit(TextGraphics g, int x, int y) {
        g.setForegroundColor(TextColor.ANSI.CYAN);
        g.setCharacter(x, y, 'E');
    }

    private static void drawEnemy(TextGraphics g, Enemy e) {
        TextColor color = switch (e.getType()) {
            case OGRE -> TextColor.ANSI.YELLOW;
            case ZOMBIE -> TextColor.ANSI.GREEN;
            case VAMPIRE -> TextColor.ANSI.RED;
            default -> TextColor.ANSI.WHITE;
        };
        char ch = switch (e.getType()) {
            case OGRE -> 'O';
            case ZOMBIE -> 'z';
            case VAMPIRE -> 'v';
            case GHOST -> 'g';
            case SNAKE_MAGE -> 's';
            case MIMIC -> 'm';
        };
        g.setForegroundColor(color);
        g.setCharacter(e.getPosX(), e.getPosY(), ch);
    }

    private static void drawItem(TextGraphics g, Item item) {
        int x = item.getPosX();
        int y = item.getPosY();
        switch (item.getType()) {
            case WEAPON -> {
                g.setForegroundColor(TextColor.ANSI.BLUE_BRIGHT);
                g.setCharacter(x, y, 'w');
            }
            case MEDKIT -> {
                g.setForegroundColor(TextColor.ANSI.CYAN);
                g.setCharacter(x, y, 'M');
            }
            case ELIXIR -> {
                g.setForegroundColor(TextColor.ANSI.GREEN);
                g.setCharacter(x, y, 'e');
            }
            case FOOD -> {
                g.setForegroundColor(TextColor.ANSI.RED);
                g.setCharacter(x, y, 'f');
            }
            case TREASURE -> {
                g.setForegroundColor(TextColor.ANSI.YELLOW);
                g.setCharacter(x, y, '$');
            }
            case SCROLLS -> {
                g.setForegroundColor(TextColor.ANSI.BLUE_BRIGHT);
                g.setCharacter(x, y, 'S');
            }
        }
    }

    // обычный тайл
    private static void drawTile(TextGraphics g, TileType tile,
                                 int x, int y, Controller controller) {
        switch (tile) {
            case WALL -> {
                g.setForegroundColor(TextColor.ANSI.WHITE);
                g.setCharacter(x, y, '#');
            }
            case FLOOR, CORRIDOR -> {
                g.setForegroundColor(TextColor.ANSI.BLACK);
                g.setCharacter(x, y, ' ');
            }
            case DOOR -> renderDoor(g, x, y, controller.getLevel());
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

    private static void drawExploredTile(TextGraphics g, TileType tile, int x, int y, Controller controller) {
        if (tile == TileType.WALL) {
            g.setForegroundColor(TextColor.ANSI.WHITE);
            g.setCharacter(x, y, '#');
        } else if (tile == TileType.FLOOR || tile == TileType.CORRIDOR) {
            g.setForegroundColor(TextColor.ANSI.GREEN);
            g.setCharacter(x, y, '.');
        } else if (tile == TileType.DOOR) {
            renderDoor(g, x, y, controller.getLevel());
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
        g.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
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
        g.putString(x, y++, "Strength: " + player.getStrength() + "(" + player.getWeapon().getSubtype() + ")");
        y++;

        g.setForegroundColor(TextColor.ANSI.YELLOW);
        g.putString(x, y++, "Backpack:");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(x, y++, "Food: " + player.getBackpack().getCount(ItemTypes.Type.FOOD));
        g.putString(x, y++, "Elixirs: " + player.getBackpack().getCount(ItemTypes.Type.ELIXIR));
        g.putString(x, y++, "Weapons: " + player.getBackpack().getCount(ItemTypes.Type.WEAPON));
        g.putString(x, y++, "Scrolls: " + player.getBackpack().getCount(ItemTypes.Type.SCROLLS));
        g.putString(x, y++, "Medkits: " + player.getBackpack().getCount(ItemTypes.Type.MEDKIT));
        g.putString(x, y, "Treasures: " + player.getBackpack().getCount(ItemTypes.Type.TREASURE));
    }

    public static void renderPlayerCombatLog(TextGraphics g, Controller controller) {
        int x = LEVEL_WIDTH + 2;
        int y = 15;
        g.setForegroundColor(TextColor.ANSI.GREEN);
        g.putString(x, y++, "=====COMBAT LOG=====");
        g.setForegroundColor(TextColor.ANSI.WHITE);
        if (controller.getPlayer().getCombatResult() != null) {
            g.putString(x, y++, controller.getPlayer().getCombatResult().toString());
        }
    }

    public static void renderEnemyCombatLog(TextGraphics g, Controller controller) {
        int x = LEVEL_WIDTH + 2;
        int y = 17;
        g.setForegroundColor(TextColor.ANSI.GREEN);
        g.setForegroundColor(TextColor.ANSI.WHITE);
        for (Enemy e : controller.getWorld().getEnemies()) {
            if (e.getCombatResult() != null) {
                g.putString(x, y++, e.getCombatResult().toString());
            }
        }
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
