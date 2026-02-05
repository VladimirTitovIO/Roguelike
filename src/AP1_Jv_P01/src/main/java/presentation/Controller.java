package presentation;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
//import domain.LeaderboardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    public enum GameState {
        START_SCREEN,
        MENU_SCREEN,
        GAME_SCREEN,
        DEAD_SCREEN,
        ENDGAME_SCREEN,
        SCOREBOARD_SCREEN
    }
    private static GameState currentState = GameState.START_SCREEN;
    private static int currentMenuLine = 0;

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
    //private static LeaderboardService leaderboardService = new FileLeaderboardService(); // Реализация разработчика А

    public static void handleInput(KeyStroke key, Screen screen) throws IOException {
        // Обработка ESC для возврата в меню из любого состояния, кроме START_SCREEN
        if (key.getKeyType() == KeyType.Escape && currentState != GameState.START_SCREEN) {
            currentState = GameState.MENU_SCREEN;
            return; // Прерываем дальнейшую обработку
        }
        switch (currentState) {
            case START_SCREEN:
                currentState = GameState.MENU_SCREEN;
                break;
            case MENU_SCREEN:
                handleMenuNavigation(key, screen);
                break;
            case GAME_SCREEN:
                handleGameInput(key, screen);
                break;
            case SCOREBOARD_SCREEN:
                if (key.getKeyType() == KeyType.Escape) {
                    currentState = GameState.MENU_SCREEN;
                }
                break;
            case DEAD_SCREEN:
                currentState = GameState.MENU_SCREEN;
                break;
            case ENDGAME_SCREEN:
                currentState = GameState.MENU_SCREEN;
                break;
        }
    }

    private static void handleMenuNavigation(KeyStroke key, Screen screen) {
        if (key.getCharacter() == 'w' || key.getCharacter() == 'W') {
            currentMenuLine = Math.max(0, currentMenuLine - 1);
        } else if (key.getCharacter() == 's' || key.getCharacter() == 'S') {
            currentMenuLine = Math.min(3, currentMenuLine + 1);
        } else if (key.getKeyType() == KeyType.Enter) {
            if (currentMenuLine == 0) { // NEW GAME
                currentState = GameState.GAME_SCREEN;
                resetGame();                
            } else if (currentMenuLine == 1) { // LOAD GAME
                //currentState = GameState.
            } else if (currentMenuLine == 2) { // SCOREBOARD
                currentState = GameState.SCOREBOARD_SCREEN;
            } else if (currentMenuLine == 3) { // EXIT
                System.exit(0);
            }
        } else if (key.getKeyType() == KeyType.Escape) {
            //currentState = GameState.MENU_SCREEN;
        }
    }

    private static void handleGameInput(KeyStroke key, Screen screen) {
        if (showingMenu) {
            handleMenuInput(key, screen);
            return; // Не обрабатываем движение, пока меню открыто
        }
        switch (key.getKeyType()) {
            case Escape: // Если esc, выходим в меню
                currentState = GameState.MENU_SCREEN;
                return;
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
        // Хаки для теста
        if (playerX == 0 && playerY == 0) { // Умер
            currentState = GameState.DEAD_SCREEN;
        }
        if (playerX == ScreenManager.ROOM_WIDTH - 1 && playerY == ScreenManager.ROOM_HEIGHT - 1) { // Победил
            currentState = GameState.ENDGAME_SCREEN;
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

    private static void resetGame() {
        playerX = 5;
        playerY = 5;
        showingMenu = false;
        currentMenuType = "";
        currentMenuItems.clear();
    }

    // Геттеры
    public static GameState getCurrentState() { return currentState; }
    public static int getPlayerX() { return playerX; }
    public static int getPlayerY() { return playerY; }
    public static boolean isShowingMenu() { return showingMenu; }
    public static String getCurrentMenuType() { return currentMenuType; }
    public static List<String> getCurrentMenuItems() { return currentMenuItems; }
    public static int getCurrentMenuLine() { return currentMenuLine; }
}