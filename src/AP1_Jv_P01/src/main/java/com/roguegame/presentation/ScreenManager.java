package com.roguegame.presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.roguegame.domain.*;
import com.roguegame.domain.Character;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap.TileType;

import java.io.IOException;
import java.util.List;

public class ScreenManager {

    /* --------------------------  константы  -------------------------- */
    public static final int LEVEL_WIDTH = DungeonLevel.WIDTH;
    public static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT;
    public static final int VIEW_RADIUS = Controller.VIEW_RADIUS;


    /* ----------------------  публичный high-level API  --------------- */
    // полноэкранные сообщения (старт/победа/поражение)
    public static void renderFullscreenMessage(Screen screen, MessageType type) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);

        MessageDesc desc = buildMessageDesc(type);

        // рисуем ASCII-арт, центрированный в верхней части экрана
        drawAsciiArtCentered(graphics, desc.art(), desc.color(), 0, 0,
                screen.getTerminalSize().getColumns(), 24);

        // подсказка
        drawStringCentered(TextColor.ANSI.WHITE, graphics, desc.prompt(), 0,
                screen.getTerminalSize().getRows() - 15, screen.getTerminalSize().getColumns());

        screen.refresh();
    }

    // стартовый экран
    public static void renderStartScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.START);
    }

    // экран победы
    public static void renderVictoryScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.VICTORY);
    }

    // экран поражения
    public static void renderDefeatScreen(Screen screen) throws IOException {
        renderFullscreenMessage(screen, MessageType.DEFEAT);
    }

    // игровое меню (выбор пункта стрелками)
    public static void renderMenuScreen(Screen screen, int currentLine) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);

        // заголовок
        drawStringCentered(TextColor.ANSI.GREEN, graphics, "GAME MENU", 0, 5,
                screen.getTerminalSize().getColumns());

        // рамка
        int boxX = (screen.getTerminalSize().getColumns() - " <<<SCOREBOARD>>> ".length()) / 2;
        int boxY = 6;
        int boxW = "<<<SCOREBOARD>>>".length()+2;
        int boxH = 8;
        drawBox(graphics, boxX, boxY, boxW, boxH,
                TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);

        // пункты меню
        String[] items = {"NEW GAME", "LOAD GAME", "SCOREBOARD", "EXIT GAME"};
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        for (int i = 0; i < items.length; i++) {
            graphics.putString(boxX + 4, boxY + 2 + i, items[i]);
        }

        // курсор
        graphics.setForegroundColor(TextColor.ANSI.GREEN);
        graphics.putString(boxX + 1, boxY + 2 + currentLine, ">>>");
        graphics.putString(boxX + boxW - 4, boxY + 2 + currentLine, "<<<");

        screen.refresh();
    }

    // основной игровой экран
    public static void renderLevel(Screen screen, Controller controller, boolean showingMenu,
                                   String currentMenuType, List<Item> currentMenuItems) throws IOException {
        TextGraphics graphics = screen.newTextGraphics();
        screen.clear();

        DungeonLevel level = controller.getLevel();
        Character player = controller.getPlayer();

        renderMap(graphics, level, controller);          // карта
        renderPlayer(graphics, player.getPosX(), player.getPosY()); // игрок
        renderUIPanel(graphics, controller);             // правая панель статистики

        if (showingMenu) {
            drawMenu(screen, graphics, currentMenuType, currentMenuItems);
        }

        // подсказка внизу
        drawStringCentered(TextColor.ANSI.WHITE, graphics,
                "WASD to move | J/K/H/E to use items | ESC to quit", 0,
                screen.getTerminalSize().getRows() - 3, screen.getTerminalSize().getColumns());
        screen.refresh();
    }

    // таблица рекордов
    public static void renderScoreboardScreen(Screen screen, List<ScoreEntry> scores) throws IOException {
        screen.clear();
        TextGraphics g = screen.newTextGraphics();

        int startX = 5;
        int startY = 5;

        // заголовок
        drawStringCentered(TextColor.ANSI.GREEN, g, "=== SCOREBOARD ===", 0, startY,
                screen.getTerminalSize().getColumns());

        // подготовим данные для универсальной drawTable
        String[] headers = {"Treasures", "Level", "Enemies", "Food",
                "Elixirs", "Scrolls", "Attacks", "Missed", "Moves"};
        List<String[]> rows = scores.stream()
                .limit(10)
                .map(se -> new String[]{
                        String.valueOf(se.getTreasures()),
                        String.valueOf(se.getLevel()),
                        String.valueOf(se.getEnemiesKilled()),
                        String.valueOf(se.getFoodUsed()),
                        String.valueOf(se.getElixirsUsed()),
                        String.valueOf(se.getScrollsUsed()),
                        String.valueOf(se.getAttacksMade()),
                        String.valueOf(se.getAttacksMissed()),
                        String.valueOf(se.getMovesMade())
                })
                .toList();

        drawTable(g, startX, startY + 2, headers, rows, 14);

        // подсказка
        drawStringCentered(TextColor.ANSI.WHITE, g, "Press ESCAPE to exit...", 0, startY + 25,
                screen.getTerminalSize().getColumns());

        screen.refresh();
    }

    // короткое всплывающее сообщение
    public static void showMessage(Screen screen, String message) {
        try {
            TextGraphics g = screen.newTextGraphics();
            g.setForegroundColor(TextColor.ANSI.YELLOW);
            g.putString(0, LEVEL_HEIGHT + 5, message);
            screen.refresh();
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* --------------------  приватные low-level рисовалки  -------------------- */

    // Рамка
    private static void drawBox(TextGraphics graphics, int boxX, int boxY, int width, int height,
                                TextColor fg, TextColor bg) {
        graphics.setBackgroundColor(bg);
        graphics.setForegroundColor(fg);
        graphics.putString(boxX, boxY, "┌" + "─".repeat(width - 2) + "┐");
        graphics.putString(boxX, boxY + height - 1, "└" + "─".repeat(width - 2) + "┘");
        for (int y = boxY + 1; y < boxY + height - 1; y++) {
            graphics.setCharacter(boxX, y, '│');
            graphics.setCharacter(boxX + width - 1, y, '│');
        }
    }

    // ASCII-арт, центрированный в заданном прямоугольнике
    private static void drawAsciiArtCentered(TextGraphics g,
                                             String[] lines,
                                             TextColor color,
                                             int areaX, int areaY,
                                             int areaW, int areaH) {

        g.setForegroundColor(color);

        // верхняя строка - выравниваем по ширине области
        String first = lines[0].trim();
        int startX = areaX + (areaW - first.length()) / 2;
        int startY = areaY + (areaH - lines.length) / 2;

        // рисуем все строки, начиная с одного и того же X
        for (int i = 0; i < lines.length; i++) {
            g.putString(startX, startY + i, lines[i]);
        }
    }


    // Одна строка по центру
    private static void drawStringCentered(TextColor.ANSI color, TextGraphics g, String text,
                                           int areaX, int y, int areaW) {
        g.setForegroundColor(color);
        g.putString(areaX + (areaW - text.length()) / 2, y, text);
    }

    // Таблица
    private static void drawTable(TextGraphics g, int x, int y,
                                  String[] headers, List<String[]> rows, int columnWidth) {
        TextColor headerColor = TextColor.ANSI.WHITE;
        TextColor lineColor = TextColor.ANSI.WHITE_BRIGHT;

        // заголовки
        g.setForegroundColor(headerColor);
        int shift = 0;
        for (String h : headers) {
            g.putString(x + shift, y, String.format("%-" + columnWidth + "s", h));
            shift += columnWidth;
        }
        // разделитель
        g.setForegroundColor(lineColor);
        g.putString(x, y + 1, "─".repeat(headers.length * columnWidth));

        // данные
        g.setForegroundColor(TextColor.ANSI.WHITE);
        for (int r = 0; r < rows.size(); r++) {
            shift = 0;
            for (int c = 0; c < headers.length; c++) {
                g.putString(x + shift, y + 2 + r,
                        String.format("%-" + columnWidth + "s", rows.get(r)[c]));
                shift += columnWidth;
            }
        }
    }

    /* ---------------  внутренняя логика уровня и UI  --------------- */

    private static void renderMap(TextGraphics g, DungeonLevel level, Controller controller) {
        for (int y = 0; y < LEVEL_HEIGHT; y++) {
            for (int x = 0; x < LEVEL_WIDTH; x++) {
                TileType tile = level.getTile(x, y);
                boolean isSeen = Controller.isExplored(x, y);
                boolean isVisibleNow = Controller.isCurrentlyVisible(x, y);

                renderTile(g, tile, x, y, isSeen, isVisibleNow, level);
            }
        }
        renderKeys(g, level, controller);
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

    private static void renderKeys(TextGraphics g, DungeonLevel level, Controller controller) {
        level.getKeysOnGround().forEach((pos, color) -> {
            if (controller.isVisible(pos.x, pos.y)) {
                g.setForegroundColor(getKeyColor(color));
                g.setCharacter(pos.x, pos.y, 'K');
            }
        });
    }

    private static void renderPlayer(TextGraphics g, int playerX, int playerY) {
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.setCharacter(playerX, playerY, '@');
    }

    private static void renderUIPanel(TextGraphics g, Controller controller) {
        Character player = controller.getPlayer();
        int x = LEVEL_WIDTH + 2;
        int y = 1;

        g.putString(x, y++, "Level:  " + controller.getWorld().getLevelNumber());
        g.putString(x, y++, "Gold: " + player.getGold());
        g.putString(x, y++, "Health: " + player.getHealth() + "/" + player.getMaximumHealth());
        g.putString(x, y++, "Agility: " + player.getAgility());
        g.putString(x, y++, "Strength: " + player.getBaseStrength() + "(" +
                player.getWeapon().getSubtype() + ")");
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

    /*private static void renderControlsHint(TextGraphics g) {
        g.putString(1, 38, "WASD to move | J/K/H/E to use items | ESC to quit");
    }*/

    private static void drawMenu(Screen screen, TextGraphics g,
                                 String currentMenuType, List<Item> currentMenuItems) {
        int menuX = 1;
        int menuY = LEVEL_HEIGHT + 3;

        g.setBackgroundColor(TextColor.ANSI.BLACK);
        g.setForegroundColor(TextColor.ANSI.WHITE);
        g.putString(menuX, menuY, "=== " + currentMenuType.toUpperCase() + " ===");

        for (int i = 0; i < currentMenuItems.size(); i++) {
            g.putString(menuX, menuY + i + 1, (i + 1) + ". " + currentMenuItems.get(i));
        }
        g.putString(menuX, menuY + currentMenuItems.size() + 2,
                "Press 1-" + currentMenuItems.size() + " or ESC to cancel");
    }

    /* ----------------------  вспомогательное  ---------------------- */

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

    // описываем арт-набор для каждого типа сообщений
    private record MessageDesc(String[] art, TextColor color, String prompt) {    }

    private static MessageDesc buildMessageDesc(MessageType type) {
        return switch (type) {
            case START -> new MessageDesc(START_ART, TextColor.ANSI.GREEN,
                    "Press any key to continue...");
            case VICTORY -> new MessageDesc(VICTORY_ART, TextColor.ANSI.GREEN,
                    "Congratulations! Press any key to continue...");
            case DEFEAT -> new MessageDesc(DEFEAT_ART, TextColor.ANSI.RED,
                    "You have fallen... Press any key to continue...");
        };
    }

    /* ----------------------  ASCII-арт  ---------------------- */
    private static final String[] START_ART = {
            "R R R       O O       G G G     U     U    E E E",
            "R     R   O     O   G       G   U     U    E    ",
            "R     R   O     O   G           U     U    E E E",
            "R R R     O     O   G   G G G   U     U    E    ",
            "R    R    O     O   G       G   U     U    E    ",
            "R     R     O O       G G G      U U U     E E E"
    };

    private static final String[] VICTORY_ART = {
            "Y     Y     O O      U     U        W           W    I    N       N      !!!",
            " Y   Y    O     O    U     U        W     W     W    I    N N     N      !!!",
            "  Y Y    O       O   U     U        W    W W    W    I    N  N    N      !!!",
            "   Y     O       O   U     U         W   W W   W     I    N   N   N      !!!",
            "   Y      O     O    U     U          W W   W W      I    N    N  N         ",
            "   Y        O O       U U U            W     W       I    N     N N      !!!"
    };

    private static final String[] DEFEAT_ART = {
            "Y     Y     O O      U     U       D D      E E E        A       D D    ",
            " Y   Y    O     O    U     U       D    D   E           A A      D    D ",
            "  Y Y    O       O   U     U       D     D  E E E      A   A     D     D",
            "   Y     O       O   U     U       D     D  E         A A A A    D     D",
            "   Y      O     O    U     U       D    D   E        A       A   D    D ",
            "   Y        O O       U U U        D D      E E E   A         A  D D    "
    };

    /* ----------------------  типы сообщений  ---------------------- */
    public enum MessageType {START, VICTORY, DEFEAT}
}
