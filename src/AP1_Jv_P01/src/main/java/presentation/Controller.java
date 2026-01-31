package presentation;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static int playerX = 5;
    private static int playerY = 5;

    // Инвентарь (хардкод)
    private static List<String> foodItems = List.of("Хлеб", "Яблоко", "Мясо");
    private static List<String> elixirItems = List.of("Эликсир силы", "Эликсир ловкости", "Эликсир здоровья");
    private static List<String> weaponItems = List.of("Меч", "Топор", "Кинжал");
    private static List<String> scrollItems = List.of("Свиток силы", "Свиток ловкости", "Свиток здоровья");

    private static boolean showingMenu = false;
    private static String currentMenuType = "";
    private static List<String> currentMenuItems = new ArrayList<>();

    public static void handleInput(KeyStroke key, Screen screen) throws IOException {
        if (key.getKeyType() == KeyType.Escape) {
            return; // Выход из игры handled in Presentation
        }

        if (showingMenu) {
            handleMenuInput(key, screen);
        } else {
            handleGameInput(key, screen);
        }

        // Ограничиваем игрока в пределах комнаты
        playerX = Math.max(0, Math.min(playerX, 9));
        playerY = Math.max(0, Math.min(playerY, 9));
    }

    private static void handleGameInput(KeyStroke key, Screen screen) {
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

    private static void handleMenuInput(KeyStroke key, Screen screen) {
        if (key.getKeyType() == KeyType.Escape) {
            closeMenu();
            return;
        }

        if (key.getKeyType() == KeyType.Character) {
            char c = key.getCharacter();
            if (c >= '1' && c <= '9') {
                int index = c - '1';
                if (index < currentMenuItems.size()) {
                    String selectedItem = currentMenuItems.get(index);
                    ScreenManager.showMessage(screen, "Used: " + selectedItem);
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

    // Геттеры для Screen
    public static int getPlayerX() { return playerX; }
    public static int getPlayerY() { return playerY; }
    public static boolean isShowingMenu() { return showingMenu; }
    public static String getCurrentMenuType() { return currentMenuType; }
    public static List<String> getCurrentMenuItems() { return currentMenuItems; }
}