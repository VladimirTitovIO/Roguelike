package domain;
import java.util.*;
import java.util.Map;


public class MapTest {
    public static void main(String[] args) throws InterruptedException {
        DungeonLevel level = new DungeonLevel();
        Character player = new Character(0, 0);
        DungeonLevel.Position start = level.getStartPosition();
        DungeonLevel.Position exit = level.getExitPosition();
        LevelGenerator lg = new LevelGenerator(level);
        World w = new World(player, level, level.getKeysOnGround());
        List<Enemy> enemies = lg.generateLevelEnemies(w.getLevelNumber(), w.getStruggleCounter());
        enemies.add(new Enemy(Enemy.Type.SNAKE_MAGE, start.x + 2, start.y +1));
        List<Item> items = lg.generateLevelItems(w.getLevelNumber(), w.getStruggleCounter());
        w.setItems(items);
        w.setEnemies(enemies);
        player.setPosY(start.y);
        player.setPosX(start.x);

        //---------------------SIMPLE GAME LOOP-----------------------------------------------------------------------------------------------------------------------
        player.getBackpack().addItem(new Item(ItemTypes.Subtype.AXE, 0, 0));
        List<Entity> turnOrder = w.calculateTurn(player, enemies);
        while (w.getLevelNumber() < 20 && player.isAlive()) {
            printMap(level, player, w, enemies, exit);
            Scanner scanner = new Scanner(System.in);
            for (Entity e : turnOrder) {
                if (!e.isAlive() && e instanceof Enemy) {
                    enemies.remove(e);
                    continue;
                }
                if (e instanceof Character && !e.isPlayerAsleep()) {
                    System.out.println("Player's input: ");
                    String input = scanner.nextLine();
                    w.handlePlayerTurn(w, input, scanner, player);
                } else {
                    Enemy enemy = (Enemy) e;
                    if (enemy.getType() == Enemy.Type.GHOST && enemy.isVisible()) enemy.setVisible(false);
                    else enemy.setVisible(true);
                    if (enemy.isPlayerNear(player, enemy)) {
                        enemy.tryToFollowPlayer(player, enemy, w);
                    } else {
                        enemy.movementPattern(enemy, w);
                    }
                }
            }
            if (player.getPosX() == exit.x && player.getPosY() == exit.y) {
                level = new DungeonLevel();
                w.setLevel(level);
                items.clear();
                enemies.clear();
                start = level.getStartPosition();
                exit = level.getExitPosition();
                player.setPosY(start.y);
                player.setPosX(start.x);
                player.getKeys().clear();
                w.setLevelNumber(w.getLevelNumber() + 1);
                lg = new LevelGenerator(level);
                items = lg.generateLevelItems(w.getLevelNumber(), w.getStruggleCounter());
                w.setItems(items);
                enemies = lg.generateLevelEnemies(w.getLevelNumber(), w.getStruggleCounter());
                w.setEnemies(enemies);
                turnOrder = w.calculateTurn(player, enemies);
                w.scaleEnemiesStrength(w.getLevelNumber(), enemies);
            }
        }
        //---------------------SIMPLE GAME LOOP-----------------------------------------------------------------------------------------------------------------------
        System.out.println();
    }

    private static void printMap(DungeonLevel level, Character player, World w, List<Enemy> enemies,  DungeonLevel.Position exit) {
        Map<DungeonLevel.Position, DungeonLevel.Door> doors = level.getDoors();
        Map<DungeonLevel.Position, DungeonLevel.DoorColor> keys = level.getKeysOnGround();
        GameMap.Direction dir = GameMap.Direction.getRandomBasicDirection();

        boolean itemprinted = false;
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {

                for (Item i : w.getItems()) {
                    if ((y == i.getPosY() && x == i.getPosX()) && i.isItemOnGround()) {
                        if (i.getType() == ItemTypes.Type.WEAPON) {
                            System.out.print("1");
                            itemprinted = true;
                        }
                        if (i.getType() == ItemTypes.Type.ELIXIR) {
                            System.out.print("2");
                            itemprinted = true;
                        }
                        if (i.getType() == ItemTypes.Type.MEDKIT) {
                            System.out.print("3");
                            itemprinted = true;
                        }
                        if (i.getType() == ItemTypes.Type.FOOD) {
                            System.out.print("4");
                            itemprinted = true;
                        }
                        if (i.getType() == ItemTypes.Type.SCROLLS) {
                            System.out.print("5");
                            itemprinted = true;
                        }
                        if (i.getType() == ItemTypes.Type.TREASURE) {
                            System.out.print("6");
                            itemprinted = true;
                        }
                    }
                }
                Enemy enemyAtPosition = w.getEnemyAtPosition(x, y);
                DungeonLevel.Position p = new DungeonLevel.Position(x, y);
                // старт / выход
                if (itemprinted) {
                    itemprinted = false;
                    continue;
                }
                if (y == player.getPosY() && x == player.getPosX()) {
                    System.out.print('P');
                    continue;
                }
                if (enemyAtPosition != null) {
                    if (enemyAtPosition.getType() == Enemy.Type.OGRE) {
                        System.out.print("O");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.GHOST && enemyAtPosition.isVisible()) {
                        System.out.print("G");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.GHOST && !enemyAtPosition.isVisible()) {
                        System.out.print(".");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.ZOMBIE) {
                        System.out.print("Z");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.MIMIC) {
                        System.out.print("M");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.VAMPIRE) {
                        System.out.print("V");
                        continue;
                    }
                    else if (enemyAtPosition.getType() == Enemy.Type.SNAKE_MAGE) {
                        System.out.print("S");
                        continue;
                    }
                }
                if (p.equals(exit)) {
                    System.out.print('E');
                    continue;
                }

                // ключи (строчные)
                DungeonLevel.DoorColor key = keys.get(p);
                if (key != null) {
                    System.out.print(keyChar(key));
                    continue;
                }

                // двери: ЗАПЕРТАЯ = цветной буквой, ОТКРЫТАЯ = '+'
                DungeonLevel.Door door = doors.get(p);
                if (door != null) {
                    if (door.locked) {
                        System.out.print(lockedDoorChar(door.color)); // R/B/Y
                    } else {
                        System.out.print('+'); // открытая дверь
                    }
                    continue;
                }

                // обычные тайлы
                char c = switch (level.getTile(x, y)) {
                    case WALL -> '#';
                    case FLOOR -> '.';
                    case CORRIDOR -> ':';
                    case DOOR -> '+'; // fallback если tile=DOOR, но метаданных нет
                };

                System.out.print(c);
            }
            System.out.println();
        }
    }

    private static char lockedDoorChar(DungeonLevel.DoorColor color) {
        return switch (color) {
            case RED -> 'R';
            case BLUE -> 'B';
            case YELLOW -> 'Y';
        };
    }

    private static char keyChar(DungeonLevel.DoorColor color) {
        return switch (color) {
            case RED -> 'r';
            case BLUE -> 'b';
            case YELLOW -> 'y';
        };
    }

}
