package com.roguegame.presentation;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.roguegame.domain.DungeonLevel;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap;
import com.roguegame.domain.GameMap.TileType;
//import domain.LeaderboardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.roguegame.domain.DungeonLevel.HEIGHT;
import static com.roguegame.domain.DungeonLevel.WIDTH;

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

    private static int playerX = 0;
    private static int playerY = 0;

    private static DungeonLevel currentLevel = null;

    // Инвентарь (временный хардкод)
    private static List<String> foodItems = List.of("Хлеб", "Яблоко", "Мясо");
    private static List<String> elixirItems = List.of("Эликсир силы", "Эликсир ловкости", "Эликсир здоровья");
    private static List<String> weaponItems = List.of("Меч", "Топор", "Кинжал");
    private static List<String> scrollItems = List.of("Свиток силы", "Свиток ловкости", "Свиток здоровья");

    private static boolean showingMenu = false;
    private static String currentMenuType = "";
    private static List<String> currentMenuItems = new ArrayList<>();
    //private static LeaderboardService leaderboardService = new FileLeaderboardService(); // Реализация разработчика А

    // НОВЫЕ ПОЛЯ
    //private static final int LEVEL_WIDTH = DungeonLevel.WIDTH; // Ширина уровня
    //private static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT; // Высота уровня
    public static final int VIEW_RADIUS = 7; // Радиус видимости

    // Исследованные клетки (для тумана войны)
    private static boolean[][] explored = new boolean[WIDTH][HEIGHT];

    // Основные методы

    // Обработка нажатия клавиши
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

    // Обработка нажатия клавиши в меню
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
        } /*else if (key.getKeyType() == KeyType.Escape) {
            //currentState = GameState.MENU_SCREEN;
        }*/
    }

    // Обработка нажатия клавиш в игре
    private static void handleGameInput(KeyStroke key, Screen screen) {
        if (showingMenu) { // меню выбора предметов из рюкзака
            handleMenuInput(key, screen);
            return; // Не обрабатываем движение, пока меню открыто
        }
        switch (key.getKeyType()) {
            case Escape: // Если esc, выходим в меню
                currentState = GameState.MENU_SCREEN;
                return;
            case Character:
                char c = key.getCharacter();
                if (c == 'w' || c == 'W') movePlayer(0, -1);
                if (c == 's' || c == 'S') movePlayer(0, 1);
                if (c == 'a' || c == 'A') movePlayer(-1, 0);
                if (c == 'd' || c == 'D') movePlayer(1, 0);

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
        // Проверка на переход на следующий уровень
        if (playerX == currentLevel.getExitPosition().x &&
                playerY == currentLevel.getExitPosition().y) {
            ScreenManager.showMessage(screen, "You found the exit! Next level!");
            currentState = GameState.ENDGAME_SCREEN; // уточнить !!!
        }

        // Проверка на смерть (для теста)
        if (playerX == playerY /*8 && playerY == 8*/) {
            currentState = GameState.DEAD_SCREEN;
        }
    }

    // Обработка выбора предмета из рюкзака
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

    // Движение игрока
    private static void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        if (canMove(newX, newY)) {
            playerX = newX;
            playerY = newY;
            calculateFOV(); // Пересчитываем видимость
            updateExplored(); // Обновляем исследованные клетки
        }
    }

    private static boolean canMove(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return false;
        }

        GameMap.TileType tile = currentLevel.getTile(x, y);
        if (tile == GameMap.TileType.WALL) {
            return false;
        }

        if (tile == GameMap.TileType.DOOR) {
            Position pos = new Position(x, y);
            var door = currentLevel.getDoors().get(pos);
            if (door != null && door.locked) {
                // Пока нет системы ключей — просто запрещаем проход
                return false;
            }
        }

        return true;
    }

    // Обновляем исследованные клетки при движении
    private static void updateExplored() {
        // Отмечаем все клетки, которые видны прямо сейчас, как исследованные
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (currentFOV[x][y]) {
                    explored[x][y] = true;
                }
            }
        }
    }

    // Сброс исследованных клеток при новой игре
    private static void resetGame() {
        currentLevel = new DungeonLevel(); // Генерируем новый уровень
        Position startPos = currentLevel.getStartPosition();
        playerX = startPos.x;
        playerY = startPos.y;
        // Сброс массива исследованных клеток
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                explored[x][y] = false;
            }
        }
        explored[playerX][playerY] = true; // Стартовая позиция считается исследованной
        calculateFOV(); // Пересчитываем FOV сразу после старта
        showingMenu = false;
        currentMenuType = "";
        currentMenuItems.clear();
    }

    // Массив текущей видимости (что видно прямо сейчас) FOV = field of view
    private static boolean[][] currentFOV = new boolean[WIDTH][HEIGHT];

    // Максимальная дистанция видимости (можно настроить)
    private static final int MAX_FOV_DISTANCE = 15;

    // Рассчитываем FOV с помощью Bresenham
    public static void calculateFOV() {
        if (currentLevel == null) return;
        // Сбрасываем текущую видимость
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                currentFOV[x][y] = false;
            }
        }

        // Пробегаем по всем клеткам в радиусе MAX_FOV_DISTANCE
        for (int dx = -MAX_FOV_DISTANCE; dx <= MAX_FOV_DISTANCE; dx++) {
            for (int dy = -MAX_FOV_DISTANCE; dy <= MAX_FOV_DISTANCE; dy++) {
                if (dx == 0 && dy == 0) continue; // не считаем саму позицию игрока

                int targetX = playerX + dx;
                int targetY = playerY + dy;

                // Проверяем, в пределах ли карты
                if (targetX < 0 || targetX >= WIDTH || targetY < 0 || targetY >= HEIGHT) {
                    continue;
                }

                // Запускаем алгоритм Брезенхэма от игрока до этой клетки
                castRay(playerX, playerY, targetX, targetY);
            }
        }
    }

    // Алгоритм Брезенхэма — прорисовывает линию от (x0,y0) до (x1,y1), останавливаясь на стене
    private static void castRay(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            // Если вышли за границы — выходим
            if (x0 < 0 || x0 >= WIDTH || y0 < 0 || y0 >= HEIGHT) break;

            // Отмечаем эту клетку как видимую
            currentFOV[x0][y0] = true;

            // Если встретили стену — прекращаем луч
            if (currentLevel.getTile(x0, y0) == TileType.WALL) {
                break;
            }

            // Если достигли цели — выходим
            if (x0 == x1 && y0 == y1) break;

            // Шаг по алгоритму Брезенхэма
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    // Метод для проверки, видна ли клетка прямо сейчас
    public static boolean isCurrentlyVisible(int x, int y) {
        return currentFOV[x][y];
    }

    // Геттеры
    public static GameState getCurrentState() {
        return currentState;
    }

    public static int getPlayerX() {
        return playerX;
    }

    public static int getPlayerY() {
        return playerY;
    }

    public static boolean isShowingMenu() {
        return showingMenu;
    }

    public static String getCurrentMenuType() {
        return currentMenuType;
    }

    public static List<String> getCurrentMenuItems() {
        return currentMenuItems;
    }

    public static int getCurrentMenuLine() {
        return currentMenuLine;
    }

    public static DungeonLevel getCurrentLevel() {
        return currentLevel;
    }

    public static boolean isExplored(int x, int y) {
        return explored[x][y];
    }
}