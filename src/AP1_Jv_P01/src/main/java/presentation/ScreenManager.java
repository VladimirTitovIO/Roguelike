package presentation;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import domain.ScoreEntry;

import java.io.IOException;
import java.util.List;

public class ScreenManager {

    public static final int ROOM_WIDTH = 10;
    public static final int ROOM_HEIGHT = 10;

    // Экраны

    public static void renderStartScreen(Screen screen) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        //graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.setForegroundColor(TextColor.ANSI.GREEN);

        String[] deathArt = {
                "R R R       O O       G G G     U     U    E E E       ",
                "R     R   O     O   G       G   U     U    E           ",
                "R     R   O     O   G           U     U    E E E       ",
                "R R R     O     O   G   G G G   U     U    E           ",
                "R    R    O     O   G       G   U     U    E           ",
                "R     R     O O       G G G      U U U     E E E       "
        };

        int startX = 5;
        int startY = 5;
        for (int i = 0; i < deathArt.length; i++) {
            graphics.putString(startX, startY + i, deathArt[i]);
        }
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.putString(startX, startY + deathArt.length + 2, "Press any key to continue...");
        screen.refresh();
    }

    public static void menuScreen(Screen screen, int currentLine) throws IOException {
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

    public static void deadScreen(Screen screen) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();

        String[] deathArt = {
                "Y     Y     O O      U     U       D D      EEEE        A       D D      ",
                " Y   Y    O     O    U     U       D    D   E          A A      D    D   ",
                "  Y Y    O       O   U     U       D     D  EEEE      A   A     D     D  ",
                "   Y     O       O   U     U       D     D  E        A A A A    D     D  ",
                "   Y      O     O    U     U       D    D   E       A       A   D    D   ",
                "   Y        O O       U U U        D D      EEEE   A         A  D D      "
        };

        int startY = 5;
        for (int i = 0; i < deathArt.length; i++) {
            graphics.putString(5, startY + i, deathArt[i]);
        }

        graphics.putString(5, startY + deathArt.length + 2, "Press any key to continue...");

        screen.refresh();
        //screen.readInput();
    }

    public static void endgameScreen(Screen screen) throws IOException {
        screen.clear();
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setForegroundColor(TextColor.ANSI.GREEN);

        String[] winArt = {
                "Y     Y     O O      U     U        W           W    I    N       N      !!!  ",
                " Y   Y    O     O    U     U        W     W     W    I    N N     N      !!!  ",
                "  Y Y    O       O   U     U        W    W W    W    I    N  N    N      !!!  ",
                "   Y     O       O   U     U         W   W W   W     I    N   N   N      !!!  ",
                "   Y      O     O    U     U          W W   W W      I    N    N  N           ",
                "   Y        O O       U U U            W     W       I    N     N N      !!!  "
        };

        int startY = 5;
        for (int i = 0; i < winArt.length; i++) {
            graphics.putString(5, startY + i, winArt[i]);
        }
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.putString(5, startY + winArt.length + 2, "Press any key to continue...");

        screen.refresh();
        //screen.readInput();
    }

    // Игровой экран

    public static void renderRoom(Screen screen, int playerX, int playerY, boolean showingMenu,
                                  String currentMenuType, List<String> currentMenuItems) {
        TextGraphics graphics = screen.newTextGraphics();

        // Очистка экрана
        screen.clear();

        // Рисуем стены (рамка)
        for (int x = 0; x < ROOM_WIDTH; x++) {
            graphics.setCharacter(x, 0, '#');
            graphics.setCharacter(x, ROOM_HEIGHT - 1, '#');
        }
        for (int y = 0; y < ROOM_HEIGHT; y++) {
            graphics.setCharacter(0, y, '#');
            graphics.setCharacter(ROOM_WIDTH - 1, y, '#');
        }

        // Рисуем пол
        for (int y = 1; y < ROOM_HEIGHT - 1; y++) {
            for (int x = 1; x < ROOM_WIDTH - 1; x++) {
                graphics.setCharacter(x, y, '.');
            }
        }

        // Рисуем игрока
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.setCharacter(playerX, playerY, '@');

        // Рисуем UI-панель справа
        drawUIPanel(graphics);

        // Если меню открыто — рисуем его поверх
        if (showingMenu) {
            drawMenu(screen, graphics, currentMenuType, currentMenuItems);
        }

        // Подсказка
        graphics.putString(1, 38, "WASD to move | J/K/H/E to use items | ESC to quit");
    }

    private static void drawUIPanel(TextGraphics graphics) {
        int X = 120;
        int Y = 1;
        //int fieldSize = 20;
        graphics.putString(X, Y++, "LVL: 1");
        graphics.putString(X, Y++, "Gold: 88");
        graphics.putString(X, Y++, "Health: 188.00/500");
        graphics.putString(X, Y++, "Agility: 70");
        graphics.putString(X, Y++, "Strength: 70");
        Y++;

        // Инвентарь в UI
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(X, Y++, "Backpack:");
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        graphics.putString(X, Y++, "Food: 3");
        graphics.putString(X, Y++, "Elixirs: 3");
        graphics.putString(X, Y++, "Weapons: 3");
        graphics.putString(X, Y, "Scrolls: 3");
    }

    private static void drawMenu(Screen screen, TextGraphics graphics, String currentMenuType, List<String> currentMenuItems) {
        int menuX = 1;
        int menuY = ROOM_HEIGHT + 3;

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
            graphics.putString(0, ROOM_HEIGHT + 5, message);
            screen.refresh();
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scoreboardScreen(Screen screen, List<ScoreEntry> scores) throws IOException {
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