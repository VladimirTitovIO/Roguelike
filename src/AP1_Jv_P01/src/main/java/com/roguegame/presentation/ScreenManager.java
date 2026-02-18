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
import java.util.stream.Collectors;

/**
 * Отрисовка всех экранов: старт, меню, победа, поражение, рекорды, игра.
 * НЕ отрисовывает игровой процесс — делегирует это GameRenderer.
 */


public class ScreenManager {

    /* --------------------------  константы  -------------------------- */
    public static final int LEVEL_WIDTH = DungeonLevel.WIDTH;
    public static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT;
    public static final int TERMINAL_WIDTH = Presentation.TERMINAL_WIDTH;
    public static final int TERMINAL_HEIGHT = Presentation.TERMINAL_HEIGHT;


    /* ----------------------  публичный high-level API  --------------- */
    // полноэкранные сообщения (старт/победа/поражение)
    public static void renderFullscreenMessage(Screen screen, MessageType type) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setBackgroundColor(TextColor.ANSI.BLACK);

        MessageDesc desc = buildMessageDesc(type);

        // рисуем ASCII-арт, центрированный в верхней части экрана
        drawAsciiArtCentered(graphics, desc.art(), desc.color(), 0, 0, TERMINAL_WIDTH, 24);


        // подсказка
        drawStringCentered(TextColor.ANSI.WHITE, graphics, desc.prompt(), 0, TERMINAL_HEIGHT - 15, TERMINAL_WIDTH);


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
                TERMINAL_WIDTH);

        // рамка
        int boxX = (TERMINAL_WIDTH - " <<<SCOREBOARD>>> ".length()) / 2;
        int boxY = 6;
        int boxW = "<<<SCOREBOARD>>>".length() + 2;
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

        Character player = controller.getPlayer();


        GameRenderer.renderMap(graphics, controller);
        GameRenderer.renderPlayer(graphics, player.getPosX(), player.getPosY());
        GameRenderer.renderUIPanel(graphics, controller);

        if (showingMenu) {
            drawMenu(screen, graphics, currentMenuType, currentMenuItems);
        }

        // подсказка внизу
        drawStringCentered(TextColor.ANSI.WHITE, graphics,
                "WASD to move | J/K/H/E to use items | ESC to quit", 0,
                TERMINAL_HEIGHT - 3, TERMINAL_WIDTH);
        screen.refresh();
    }

    // таблица рекордов
    public static void renderScoreboardScreen(Screen screen, List<ScoreEntry> scores) throws IOException {
        screen.clear();
        TextGraphics g = screen.newTextGraphics();

        int startX = 5;
        int startY = 5;

        // заголовок
        drawStringCentered(TextColor.ANSI.GREEN, g, "=== SCOREBOARD ===", 0, startY, TERMINAL_WIDTH);

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
        drawStringCentered(TextColor.ANSI.WHITE, g, "Press ESCAPE to exit", 0, startY + 25, TERMINAL_WIDTH);

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

    // описываем арт-набор для каждого типа сообщений
    private record MessageDesc(String[] art, TextColor color, String prompt) {
    }

    private static MessageDesc buildMessageDesc(MessageType type) {
        return switch (type) {
            case START -> new MessageDesc(START_ART, TextColor.ANSI.GREEN,
                    "Press any key to continue");
            case VICTORY -> new MessageDesc(VICTORY_ART, TextColor.ANSI.GREEN,
                    "Congratulations! Press any key to continue");
            case DEFEAT -> new MessageDesc(DEFEAT_ART, TextColor.ANSI.RED,
                    "You have fallen. Press any key to continue");
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
