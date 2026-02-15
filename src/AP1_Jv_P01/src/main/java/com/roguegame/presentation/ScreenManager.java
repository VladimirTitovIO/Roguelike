package com.roguegame.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.roguegame.domain.DungeonLevel;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap.TileType;
import com.roguegame.domain.ScoreEntry;

import java.io.IOException;
import java.util.List;

public class ScreenManager {

    public static final int LEVEL_WIDTH = DungeonLevel.WIDTH;
    public static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT;
    public static final int VIEW_RADIUS = Controller.VIEW_RADIUS;

    // Универсальный метод для отрисовки полноэкранных сообщений
    public static void renderFullscreenMessage(Screen screen, MessageType type) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);

        String[] artLines;
        TextColor primaryColor;

        switch (type) {
            case START:
                artLines = new String[]{
                        "R R R       O O       G G G     U     U    E E E       ",
                        "R     R   O     O   G       G   U     U    E           ",
                        "R     R   O     O   G           U     U    E E E       ",
                        "R R R     O     O   G   G G G   U     U    E           ",
                        "R    R    O     O   G       G   U     U    E           ",
                        "R     R     O O       G G G      U U U     E E E       "
                };
                primaryColor = TextColor.ANSI.GREEN;
                break;

            case VICTORY:
                artLines = new String[]{
                        "Y     Y     O O      U     U        W           W    I    N       N      !!!  ",
                        " Y   Y    O     O    U     U        W     W     W    I    N N     N      !!!  ",
                        "  Y Y    O       O   U     U        W    W W    W    I    N  N    N      !!!  ",
                        "   Y     O       O   U     U         W   W W   W     I    N   N   N      !!!  ",
                        "   Y      O     O    U     U          W W   W W      I    N    N  N           ",
                        "   Y        O O       U U U            W     W       I    N     N N      !!!  "
                };
                primaryColor = TextColor.ANSI.GREEN;
                break;

            case DEFEAT:
                artLines = new String[]{
                        "Y     Y     O O      U     U       D D      EEEE        A       D D      ",
                        " Y   Y    O     O    U     U       D    D   E          A A      D    D   ",
                        "  Y Y    O       O   U     U       D     D  EEEE      A   A     D     D  ",
                        "   Y     O       O   U     U       D     D  E        A A A A    D     D  ",
                        "   Y      O     O    U     U       D    D   E       A       A   D    D   ",
                        "   Y        O O       U U U        D D      EEEE   A         A  D D      "
                };
                primaryColor = TextColor.ANSI.RED;
                break;

            default:
                artLines = new String[]{};
                primaryColor = TextColor.ANSI.WHITE;
        }

        // Отрисовка арта
        graphics.setForegroundColor(primaryColor);
        int startX = 5;
        int startY = 5;
        for (int i = 0; i < artLines.length; i++) {
            graphics.putString(startX, startY + i, artLines[i]);
        }

        // Отрисовка сообщения
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        String message = getMessageForType(type);
        graphics.putString(startX, startY + artLines.length + 2, message);

        screen.refresh();
    }

    private static String getMessageForType(MessageType type) {
        switch (type) {
            case START: return "Press any key to continue...";
            case VICTORY: return "Congratulations! Press any key to continue...";
            case DEFEAT: return "Game Over. Press any key to continue...";
            default: return "Press any key to continue...";
        }
    }

    public enum MessageType {
        START, VICTORY, DEFEAT
    }

    public static void renderStartScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.START);
    }

    public static void renderVictoryScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.VICTORY);
    }

    public static void renderDefeatScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.DEFEAT);
    }

    public static void renderMenuScreen(Screen screen, int currentLine) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();

        graphics.putString(5, 5, "     GAME MENU     ");
        graphics.putString(5, 6, "+------------------+");
        graphics.putString(5, 7, "|                  |");
        graphics.putString(5, 8, "|    NEW GAME      |");
        graphics.putString(5, 9, "|    LOAD GAME     |");
        graphics.putString(5, 10, "|    SCOREBOARD    |");
        graphics.putString(5, 11, "|    EXIT GAME     |");
        graphics.putString(5, 12, "|                  |");
        graphics.putString(5, 13, "+------------------+");

        // Выделение текущей строки
        int menuY = 8 + currentLine;
        graphics.putString(6, menuY, "<<<");
        graphics.putString(21, menuY, ">>>");

        screen.refresh();
    }

    // Игровой экран

    public static void renderLevel(Screen screen, int playerX, int playerY, boolean showingMenu,
                                   String currentMenuType, List<String> currentMenuItems) throws IOException {
        TextGraphics graphics = screen.newTextGraphics();

        // Очистка экрана
        screen.clear();

        DungeonLevel level = Controller.getCurrentLevel();

        // Рисуем карту с учётом тумана войны
        for (int y = 0; y < LEVEL_HEIGHT; y++) {
            for (int x = 0; x < LEVEL_WIDTH; x++) {
                TileType tile = level.getTile(x, y);

                boolean isSeen = Controller.isExplored(x, y); // уже исследовано?
                boolean isVisibleNow = Controller.isCurrentlyVisible(x, y); // видно прямо сейчас?

                if (isVisibleNow) {
                    // Видим — рисуем
                    switch (tile) {
                        case WALL:
                            graphics.setForegroundColor(TextColor.ANSI.WHITE);
                            graphics.setCharacter(x, y, '#');
                            break;
                        case FLOOR:
                            graphics.setForegroundColor(TextColor.ANSI.GREEN);
                            graphics.setCharacter(x, y, '.');
                            break;
                        case CORRIDOR:
                            graphics.setForegroundColor(TextColor.ANSI.YELLOW);
                            graphics.setCharacter(x, y, '.');
                            break;
                        case DOOR:
                            Position pos = new Position(x, y);
                            var door = level.getDoors().get(pos);
                            if (door != null) {
                                if (door.locked) {
                                    graphics.setForegroundColor(getDoorColor(door.color));
                                    graphics.setCharacter(x, y, 'D'); // или '🔒'
                                } else {
                                    graphics.setForegroundColor(TextColor.ANSI.CYAN);
                                    graphics.setCharacter(x, y, '+');
                                }
                            } else {
                                graphics.setForegroundColor(TextColor.ANSI.CYAN);
                                graphics.setCharacter(x, y, '+');
                            }
                            break;
                        default:
                            graphics.setForegroundColor(TextColor.ANSI.WHITE);
                            graphics.setCharacter(x, y, '?');
                    }
                } else if (isSeen) {
                    // Исследовано, но не видно сейчас — рисуем только стены
                    if (tile == TileType.WALL) {
                        graphics.setForegroundColor(TextColor.ANSI.WHITE);
                        graphics.setCharacter(x, y, '#');
                    } else {
                        // Не стена — рисуем тёмно (как "затемнённое" пространство)
                        graphics.setForegroundColor(TextColor.ANSI.BLACK);
                        graphics.setCharacter(x, y, ' ');
                    }
                } else {
                    // Не исследовано — чёрный фон
                    graphics.setForegroundColor(TextColor.ANSI.BLACK);
                    graphics.setCharacter(x, y, ' ');
                }
            }
        }

        // Рисуем ключи (если они видны)
        for (var entry : level.getKeysOnGround().entrySet()) {
            Position pos = entry.getKey();
            if (Controller.isVisible(pos.x, pos.y)) {
                graphics.setForegroundColor(getKeyColor(entry.getValue()));
                graphics.setCharacter(pos.x, pos.y, 'K'); // или '🔑'
            }
        }

        // Рисуем игрока
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.setCharacter(playerX, playerY, '@');

        // Рисуем UI-панель
        drawUIPanel(graphics);

        // Если меню открыто — рисуем его поверх
        if (showingMenu) {
            drawMenu(screen, graphics, currentMenuType, currentMenuItems);
        }

        // Подсказка
        graphics.putString(1, 38, "WASD to move | J/K/H/E to use items | ESC to quit");
        screen.refresh();
    }

    private static TextColor getDoorColor(DungeonLevel.DoorColor color) {
        return switch (color) {
            case RED -> TextColor.ANSI.RED;
            case BLUE -> TextColor.ANSI.BLUE;
            case YELLOW -> TextColor.ANSI.YELLOW;
        };
    }

    private static TextColor getKeyColor(DungeonLevel.DoorColor color) {
        return switch (color) {
            case RED -> TextColor.ANSI.RED_BRIGHT;
            case BLUE -> TextColor.ANSI.BLUE_BRIGHT;
            case YELLOW -> TextColor.ANSI.YELLOW_BRIGHT;
        };
    }

    private static void drawUIPanel(TextGraphics graphics) {
        int x = LEVEL_WIDTH + 2;
        int y = 1;
        //int fieldSize = 20;
        graphics.putString(x, y++, "LVL: 1");
        graphics.putString(x, y++, "Gold: 88");
        graphics.putString(x, y++, "Health: 188.00/500");
        graphics.putString(x, y++, "Agility: 70");
        graphics.putString(x, y++, "Strength: 70");
        y++;

        // Инвентарь в UI
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(x, y++, "Backpack:");
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.putString(x, y++, "Food: 3");
        graphics.putString(x, y++, "Elixirs: 3");
        graphics.putString(x, y++, "Weapons: 3");
        graphics.putString(x, y, "Scrolls: 3");
    }

    private static void drawMenu(Screen screen, TextGraphics graphics, String currentMenuType, List<String> currentMenuItems) {
        int menuX = 1;
        int menuY = LEVEL_HEIGHT + 3;

        graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.putString(menuX, menuY, "=== " + currentMenuType.toUpperCase() + " ===");

        for (int i = 0; i < currentMenuItems.size(); i++) {
            graphics.putString(menuX, menuY + i + 1, (i + 1) + ". " + currentMenuItems.get(i));
        }

        graphics.putString(menuX, menuY + currentMenuItems.size() + 2, "Press 1-" + currentMenuItems.size() + " or ESC to cancel");
    }

    public static void showMessage(Screen screen, String message) {
        try {
            TextGraphics graphics = screen.newTextGraphics();
            graphics.setForegroundColor(TextColor.ANSI.YELLOW);
            graphics.putString(0, LEVEL_HEIGHT + 5, message);
            screen.refresh();
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void renderScoreboardScreen(Screen screen, List<ScoreEntry> scores) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();

        // Заголовок
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        int startX = 5;
        int startY = 5;
        graphics.putString(startX, startY, "=== SCOREBOARD ===");

        // Заголовки столбцов
        int fieldSize = 14;
        int y = startY + 2;
        graphics.putString(startX, y, String.format("%-" + fieldSize + "s", "Treasures"));
        graphics.putString(startX + fieldSize, y, String.format("%-" + fieldSize + "s", "Level"));
        graphics.putString(startX + 2 * fieldSize, y, String.format("%-" + fieldSize + "s", "Enemies"));
        graphics.putString(startX + 3 * fieldSize, y, String.format("%-" + fieldSize + "s", "Food"));
        graphics.putString(startX + 4 * fieldSize, y, String.format("%-" + fieldSize + "s", "Elixirs"));
        graphics.putString(startX + 5 * fieldSize, y, String.format("%-" + fieldSize + "s", "Scrolls"));
        graphics.putString(startX + 6 * fieldSize, y, String.format("%-" + fieldSize + "s", "Attacks"));
        graphics.putString(startX + 7 * fieldSize, y, String.format("%-" + fieldSize + "s", "Missed"));
        graphics.putString(startX + 8 * fieldSize, y, String.format("%-" + fieldSize + "s", "Moves"));

        // Разделительная линия
        y += 1;
        for (int i = 0; i < 9 * fieldSize; i++) {
            graphics.setCharacter(startX + i, y, '-');
        }

        // Данные
        y += 1;
        for (int i = 0; i < Math.min(scores.size(), 10); i++) { // Показываем топ-10
            ScoreEntry entry = scores.get(i);
            graphics.putString(startX, y + i, String.format("%-" + fieldSize + "d", entry.getTreasures()));
            graphics.putString(startX + fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getLevel()));
            graphics.putString(startX + 2 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getEnemiesKilled()));
            graphics.putString(startX + 3 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getFoodUsed()));
            graphics.putString(startX + 4 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getElixirsUsed()));
            graphics.putString(startX + 5 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getScrollsUsed()));
            graphics.putString(startX + 6 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getAttacksMade()));
            graphics.putString(startX + 7 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getAttacksMissed()));
            graphics.putString(startX + 8 * fieldSize, y + i, String.format("%-" + fieldSize + "d", entry.getMovesMade()));
        }

        // Подсказка
        y += 10;
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(startX, y, "Press ESCAPE to exit...");

        screen.refresh();
    }
}