package presentation;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.Terminal;

import java.awt.Font;
import javax.swing.JFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int ROOM_WIDTH = 10;
    private static final int ROOM_HEIGHT = 10;

    private static int playerX = 5;
    private static int playerY = 5;

    // Инвентарь (хардкод)
    private static List<String> foodItems = List.of("Хлеб", "Яблоко", "Мясо");
    private static List<String> elixirItems = List.of("Эликсир силы", "Эликсир ловкости", "Эликсир здоровья");
    private static List<String> weaponItems = List.of("Меч", "Топор", "Кинжал");
    private static List<String> scrollItems = List.of("Свиток силы", "Свиток ловкости", "Свиток здоровья");

    private static boolean showingMenu = false;
    private static String currentMenuType = ""; // "food", "elixir", "weapon", "scroll"
    private static List<String> currentMenuItems = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Начальный размер окна (100x35 символов)
            TerminalSize terminalSize = new TerminalSize(100, 35);

            // Кастомный шрифт
            Font font = new Font("Monospaced", Font.BOLD, 16);
            SwingTerminalFontConfiguration fontConfig =
                    SwingTerminalFontConfiguration.newInstance(font);

            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory()
                    .setInitialTerminalSize(terminalSize)
                    .setTerminalEmulatorFontConfiguration(fontConfig);

            SwingTerminalFrame terminal = (SwingTerminalFrame) terminalFactory.createTerminal();

            // Разрешаем изменение размера окна
            terminal.setResizable(true);

            // Разворачиваем на весь экран
            //terminal.setExtendedState(JFrame.MAXIMIZED_BOTH);

            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            while (true) {
                renderRoom(screen);
                screen.refresh();

                KeyStroke key = screen.readInput();
                if (key == null) continue;

                if (key.getKeyType() == KeyType.Escape) {
                    break;
                }

                if (showingMenu) {
                    handleMenuInput(key, screen);
                } else {
                    handleGameInput(key);
                }

                playerX = Math.max(0, Math.min(playerX, ROOM_WIDTH - 1));
                playerY = Math.max(0, Math.min(playerY, ROOM_HEIGHT - 1));

            }

            screen.close();
            terminal.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void renderRoom(Screen screen) {
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
        graphics.setForegroundColor(TextColor.ANSI.WHITE);
        drawUIPanel(graphics);

        // Если меню открыто — рисуем его поверх
        if (showingMenu) {
            drawMenu(screen, graphics);
        }

        // Подсказка
        graphics.putString(0, ROOM_HEIGHT + 1, "WASD to move | J/K/H/E to use items | ESC to quit");
    }

    private static void drawUIPanel(TextGraphics graphics) {
        int uiX = ROOM_WIDTH + 2;
        graphics.putString(uiX, 1, "HP: 50/100");
        graphics.putString(uiX, 2, "STR: 8");
        graphics.putString(uiX, 3, "AGI: 12");
        graphics.putString(uiX, 4, "LVL: 1");

        // Инвентарь в UI
        graphics.putString(uiX, 6, "Inventory:");
        graphics.putString(uiX, 7, "Food: " + foodItems.size());
        graphics.putString(uiX, 8, "Elixirs: " + elixirItems.size());
        graphics.putString(uiX, 9, "Weapons: " + weaponItems.size());
        graphics.putString(uiX, 10, "Scrolls: " + scrollItems.size());
    }

    private static void drawMenu(Screen screen, TextGraphics graphics) {
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

    private static void handleGameInput(KeyStroke key) {
        switch (key.getKeyType()) {
            case Character:
                char c = key.getCharacter();
                if (c == 'w' || c == 'W') playerY--;
                if (c == 's' || c == 'S') playerY++;
                if (c == 'a' || c == 'A') playerX--;
                if (c == 'd' || c == 'D') playerX++;

                if (c == 'j' || c == 'J') {
                    openMenu("food", foodItems);
                }
                if (c == 'k' || c == 'K') {
                    openMenu("elixir", elixirItems);
                }
                if (c == 'h' || c == 'H') {
                    openMenu("weapon", weaponItems);
                }
                if (c == 'e' || c == 'E') {
                    openMenu("scroll", scrollItems);
                }
                break;
        }
    }

    private static void handleMenuInput(KeyStroke key, Screen screen) throws IOException {
        if (key.getKeyType() == KeyType.Escape) {
            closeMenu();
            return;
        }

        if (key.getKeyType() == KeyType.Character) {
            char c = key.getCharacter();
            if (c >= '1' && c <= '9') {
                int index = c - '1'; // Преобразует символ цифры в индекс массива от 0
                if (index < currentMenuItems.size()) {
                    String selectedItem = currentMenuItems.get(index);
                    showMessage(screen, "Used: " + selectedItem);
                    // Здесь можно добавить логику использования предмета
                    closeMenu();
                }
            }
        }
    }

    private static void openMenu(String type, List<String> items) {
        currentMenuType = type;
        currentMenuItems = new ArrayList<>(items);
        showingMenu = true;
    }

    private static void closeMenu() {
        showingMenu = false;
        currentMenuType = "";
        currentMenuItems.clear();
    }

    private static void showMessage(Screen screen, String message) throws IOException {
        TextGraphics graphics = screen.newTextGraphics();
        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(0, ROOM_HEIGHT + 5, message);
        screen.refresh(); // обновление экрана
        try {
            Thread.sleep(1500); // Показать 1.5 секунды
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}