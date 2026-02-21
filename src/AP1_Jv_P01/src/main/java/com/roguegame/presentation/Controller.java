package com.roguegame.presentation;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.swing.ScrollingSwingTerminal;
import com.roguegame.domain.*;
import com.roguegame.domain.Character;
import com.roguegame.domain.DungeonLevel.Position;
import com.roguegame.domain.GameMap.TileType;
//import domain.LeaderboardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.roguegame.domain.DungeonLevel.HEIGHT;
import static com.roguegame.domain.DungeonLevel.WIDTH;

public class Controller {
    public Controller() {
        world = new World();
        Character player = world.getPlayer();
        turnOrder = world.calculateTurn(world.getPlayer(), world.getEnemies());
        rebuildMaps();
        player.getBackpack().addItem(new Item(ItemTypes.Subtype.FOOD_SMALL, 0, 0));
        player.getBackpack().addItem(new Item(ItemTypes.Subtype.SWORD, 0, 0));
        player.getBackpack().addItem(new Item(ItemTypes.Subtype.MEDKIT_SMALL, 0, 0));

    }

    public List<Entity> getTurnOrder() {
        return turnOrder;
    }

    public int getFoodUsed() {
        return foodUsed;
    }

    public void setFoodUsed(int foodUsed) {
        this.foodUsed = foodUsed;
    }

    public int getElixirsUsed() {
        return elixirsUsed;
    }

    public void setElixirsUsed(int elixirsUsed) {
        this.elixirsUsed = elixirsUsed;
    }

    public int getScrollsUsed() {
        return scrollsUsed;
    }

    public void setScrollsUsed(int scrollsUsed) {
        this.scrollsUsed = scrollsUsed;
    }


    public int getMovesMade() {
        return movesMade;
    }

    public void setMovesMade(int movesMade) {
        this.movesMade = movesMade;
    }

    public int getMedkitsUsed() {
        return medkitsUsed;
    }

    public void setMedkitsUsed(int medkitsUsed) {
        this.medkitsUsed = medkitsUsed;
    }

    public enum GameState {
        START_SCREEN,
        MENU_SCREEN,
        GAME_SCREEN,
        DEAD_SCREEN,
        ENDGAME_SCREEN,
        SCOREBOARD_SCREEN
    }

    private int foodUsed = 0;
    private int elixirsUsed = 0;
    private int scrollsUsed = 0;
    private int medkitsUsed = 0;
    private int movesMade = 0;
    private List<Entity> turnOrder;
    private final World world;
    private GameState currentState = GameState.START_SCREEN;
    private int currentMenuLine = 0;
    private Map<Position, Enemy> enemyMap;
    private Map<Position, Item> itemMap;


    // Инвентарь (временный хардкод)
    private static boolean showingMenu = false;
    private static String currentMenuType = "";
    private List<Item> currentMenuItems = new ArrayList<>();
    //private static LeaderboardService leaderboardService = new FileLeaderboardService(); // Реализация разработчика А

    // НОВЫЕ ПОЛЯ
    //private static final int LEVEL_WIDTH = DungeonLevel.WIDTH; // Ширина уровня
    //private static final int LEVEL_HEIGHT = DungeonLevel.HEIGHT; // Высота уровня
    public static final int VIEW_RADIUS = 7; // Радиус видимости

    // Исследованные клетки (для тумана войны)
    private boolean[][] explored = new boolean[WIDTH][HEIGHT];

    public Character getPlayer() {
        return world.getPlayer();
    }

    public DungeonLevel getLevel() {
        return world.getLevel();
    }

    public World getWorld() {
        return world;
    }

    // Основные методы

    // Обработка нажатия клавиши
    public void handleInput(KeyStroke key, Screen screen) throws IOException {
        // Обработка ESC для возврата в меню из любого состояния, кроме START_SCREEN и меню выбора инвентаря
        if (key.getKeyType() == KeyType.Escape && currentState != GameState.START_SCREEN && !showingMenu) {
            currentState = GameState.MENU_SCREEN;
            return; // Прерываем дальнейшую обработку
        }
        switch (currentState) {
            case START_SCREEN, DEAD_SCREEN, ENDGAME_SCREEN:
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
        }
    }

    public void enemyTurns() {
        for (Entity e : getTurnOrder()) {
            if (e instanceof Enemy enemy && enemy.isAlive()) {
                enemy = (Enemy) e;
                if (enemy.getType() == Enemy.Type.GHOST && enemy.isInCombat() && !enemy.isVisible()) enemy.setVisible(true);
                else if (enemy.getType() == Enemy.Type.GHOST && !enemy.isInCombat() && enemy.isVisible()) enemy.setVisible(false);
                else enemy.setVisible(true);
                if (enemy.isPlayerNear(getPlayer(), enemy)) {
                    enemy.tryToFollowPlayer(getPlayer(), enemy, getWorld());
                } else {
                    enemy.setInCombat(false);
                    enemy.movementPattern(enemy, getWorld());
                }
            }
        }
        rebuildMaps();
    }

    // Обработка нажатия клавиши в меню
    private void handleMenuNavigation(KeyStroke key, Screen screen) {
        if (key.getKeyType() == KeyType.Character) {
            if (key.getCharacter() == 'w' || key.getCharacter() == 'W') {
                currentMenuLine = Math.max(0, currentMenuLine - 1);
            } else if (key.getCharacter() == 's' || key.getCharacter() == 'S') {
                currentMenuLine = Math.min(3, currentMenuLine + 1);
            }
        } else if (key.getKeyType() == KeyType.Enter) {
            if (currentMenuLine == 0) { // NEW GAME
                currentState = GameState.GAME_SCREEN;
                if (!world.getPlayer().isAlive()) {
                    world.initNewLevel();
                }
                resetGame();
            } else if (currentMenuLine == 1) { // LOAD GAME
                //currentState = GameState.
            } else if (currentMenuLine == 2) { // SCOREBOARD
                currentState = GameState.SCOREBOARD_SCREEN;
            } else if (currentMenuLine == 3) { // EXIT
                System.exit(0);
            }
        }
    }

    // Обработка нажатия клавиш в игре
    private void handleGameInput(KeyStroke key, Screen screen) {
        Character player = world.getPlayer();
        boolean moved = false;
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
                if (c == 'w' || c == 'W') moved = world.tryToMove(player, GameMap.Direction.UP);
                if (c == 's' || c == 'S') moved = world.tryToMove(player, GameMap.Direction.DOWN);
                if (c == 'a' || c == 'A') moved = world.tryToMove(player, GameMap.Direction.LEFT);
                if (c == 'd' || c == 'D') moved = world.tryToMove(player, GameMap.Direction.RIGHT);
                if (moved) {
                    calculateFOV();
                    updateExplored();
                    setMovesMade(getMovesMade() + 1);
                    turnOrder = world.calculateTurn(world.getPlayer(), world.getEnemies());
                    rebuildMaps();
                }

                if (c == 'j' || c == 'J') {
                    openMenu("food", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.FOOD)).toList());
                }
                if (c == 'k' || c == 'K') {
                    openMenu("elixirs", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.ELIXIR)).toList());
                }
                if (c == 'h' || c == 'H') {
                    openMenu("weapons", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.WEAPON)).toList());
                }
                if (c == 'e' || c == 'E') {
                    openMenu("scroll", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.SCROLLS)).toList());
                }
                if (c == 'l' || c == 'L') {
                    openMenu("medkit", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.MEDKIT)).toList());
                }
                if (c == 't' || c == 'T') {
                    openMenu("treasures", player.getBackpack().getItems().stream().
                            filter(e -> e.getType().equals(ItemTypes.Type.TREASURE)).toList());
                }
                break;
        }
        // Проверка на переход на следующий уровень
        if (world.getPlayer().getPosX() == world.getLevel().getExitPosition().x &&
                world.getPlayer().getPosY() == world.getLevel().getExitPosition().y) {
            ScreenManager.showMessage(screen, "You found the exit! Next level!");
//            currentState = GameState.ENDGAME_SCREEN; // уточнить !!!
            world.initNewLevel();
            world.setLevelNumber(world.getLevelNumber() + 1);
            resetGame();
        }

//        // Проверка на смерть (для теста)
//        if (world.getPlayer().getPosX() == world.getPlayer().getPosY() /*8 && playerY == 8*/) {
//            currentState = GameState.DEAD_SCREEN;
//        }
    }

    // Обработка выбора предмета из рюкзака
    private void handleMenuInput(KeyStroke key, Screen screen) {
        Character player = world.getPlayer();

        if (key.getKeyType() == KeyType.Escape) {
            closeMenu();
            return;
        }

        if (key.getKeyType() == KeyType.Character) {
            char c = key.getCharacter();
            if (c >= '1' && c <= '9') {
                int index = c - '1';
                if (index < currentMenuItems.size()) {
                    String selectedItem = String.valueOf(currentMenuItems.get(index));
                    ScreenManager.showMessage(screen, "Used: " + selectedItem);
                    Item oldWeapon = player.getWeapon();
                    if (currentMenuItems.get(index).getType() == ItemTypes.Type.WEAPON) {
                        Item currentWeapon = currentMenuItems.get(index);
                        if (oldWeapon.getSubtype() == ItemTypes.Subtype.FISTS)
                            player.getBackpack().useItem(currentMenuItems.get(index), player);
                        if (oldWeapon.getSubtype() != ItemTypes.Subtype.FISTS && oldWeapon != currentWeapon) {
                            if (world.dropItemNear(world.getPlayer(), oldWeapon)) {
                                player.getBackpack().useItem(currentMenuItems.get(index), player);
                            }
                        }
                    } else {
                        player.getBackpack().useItem(currentMenuItems.get(index), player);
                        if (currentMenuItems.get(index).getType() == ItemTypes.Type.FOOD)
                            setFoodUsed(getFoodUsed() + 1);
                        if (currentMenuItems.get(index).getType() == ItemTypes.Type.ELIXIR)
                            setElixirsUsed(getElixirsUsed() + 1);
                        if (currentMenuItems.get(index).getType() == ItemTypes.Type.SCROLLS)
                            setScrollsUsed(getScrollsUsed() + 1);
                        if (currentMenuItems.get(index).getType() == ItemTypes.Type.MEDKIT)
                            setMedkitsUsed(getMedkitsUsed() + 1);
                    }
                    closeMenu();
                }
            }
        }
    }

    private void openMenu(String type, List<Item> items) {
        currentMenuType = type;
        currentMenuItems = new ArrayList<>(items);
        showingMenu = true;
    }

    private void closeMenu() {
        showingMenu = false;
        currentMenuType = "";
        currentMenuItems.clear();
    }

    public void rebuildMaps() {
        enemyMap = new HashMap<>();
        for (Enemy e : world.getEnemies()) {
            enemyMap.put(new Position(e.getPosX(), e.getPosY()), e);
        }
        itemMap = new HashMap<>();
        for (Item i : world.getItems()) {
            itemMap.put(new Position(i.getPosX(), i.getPosY()), i);
        }
        setEnemyMap(enemyMap);
        setItemMap(itemMap);
    }

    public void setEnemyMap(Map<Position, Enemy> enemyMap) {
        this.enemyMap = enemyMap;
    }

    public void setItemMap(Map<Position, Item> itemMap) {
        this.itemMap = itemMap;
    }

    public Map<Position, Enemy> getEnemyMap() {
        return enemyMap;
    }

    public Map<Position, Item> getItemMap() {
        return itemMap;
    }

    // Обновляем исследованные клетки при движении
    private void updateExplored() {
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
    private void resetGame() {
        Character player = world.getPlayer();
//        world.setLevelNumber(1);
        turnOrder = world.calculateTurn(world.getPlayer(), world.getEnemies());
        // Сброс массива исследованных клеток
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                explored[x][y] = false;
            }
        }
        explored[player.getPosX()][player.getPosY()] = true; // Стартовая позиция считается исследованной
        calculateFOV(); // Пересчитываем FOV сразу после старта
        showingMenu = false;
        currentMenuType = "";
        currentMenuItems.clear();
    }

    // Массив текущей видимости (что видно прямо сейчас) FOV = field of view
    private final boolean[][] currentFOV = new boolean[WIDTH][HEIGHT];

    // Максимальная дистанция видимости (можно настроить)
    private static final int MAX_FOV_DISTANCE = 15;

    // Рассчитываем FOV с помощью Bresenham
    public void calculateFOV() {
        Character player = world.getPlayer();
        if (world.getLevel() == null) return;
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

                int targetX = player.getPosX() + dx;
                int targetY = player.getPosY() + dy;

                // Проверяем, в пределах ли карты
                if (targetX < 0 || targetX >= WIDTH || targetY < 0 || targetY >= HEIGHT) {
                    continue;
                }

                // Запускаем алгоритм Брезенхэма от игрока до этой клетки
                castRay(player.getPosX(), player.getPosY(), targetX, targetY);
            }
        }
    }

    // Алгоритм Брезенхэма — прорисовывает линию от (x0,y0) до (x1,y1), останавливаясь на стене
    private void castRay(int x0, int y0, int x1, int y1) {
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
            if (world.getLevel().getTile(x0, y0) == TileType.WALL) {
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
    public boolean isCurrentlyVisible(int x, int y) {
        return currentFOV[x][y];
    }

    // Геттеры
    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }

    public void setGameState(GameState state) {
        currentState = state;
    }

    public static boolean isShowingMenu() {
        return showingMenu;
    }

    public static String getCurrentMenuType() {
        return currentMenuType;
    }

    public List<Item> getCurrentMenuItems() {
        return currentMenuItems;
    }

    public int getCurrentMenuLine() {
        return currentMenuLine;
    }

    public boolean isExplored(int x, int y) {
        return explored[x][y];
    }
}