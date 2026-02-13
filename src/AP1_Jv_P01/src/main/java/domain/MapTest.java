package domain;
import java.util.*;
import java.util.Map;


public class MapTest {
    public static void main(String[] args) {
        DungeonLevel level = new DungeonLevel();
        Character player = new Character(0, 0);

        System.out.println("Карта сгенерирована. Размер: "
                + level.getWidth() + " × " + level.getHeight());
        System.out.println();

        System.out.println("Легенда:");
        System.out.println("# = WALL, . = FLOOR, : = CORRIDOR");
        System.out.println("R/B/Y = ЗАПЕРТАЯ дверь (нужен ключ)");
        System.out.println("+     = ОТКРЫТАЯ дверь (ключ не нужен)");
        System.out.println("r/b/y = ключ");
        System.out.println("S = старт, E = выход");
        System.out.println();

        printMap(level, player);

        DungeonLevel.Position start = level.getStartPosition();
        DungeonLevel.Position exit = level.getExitPosition();

        System.out.println();
        System.out.println("Стартовая позиция: (" + start.x + ", " + start.y + ")");
        System.out.println("Выходная позиция:   (" + exit.x + ", " + exit.y + ")");

        // Дополнительно: список ключей на карте (удобно проверять)
        System.out.println("\nКлючи на карте:");
        for (Map.Entry<DungeonLevel.Position, DungeonLevel.DoorColor> e : level.getKeysOnGround().entrySet()) {
            System.out.println(" - " + e.getValue() + " key at " + e.getKey());
        }


    }

    private static void printMap(DungeonLevel level, Character player) {
        Map<DungeonLevel.Position, DungeonLevel.Door> doors = level.getDoors();
        Map<DungeonLevel.Position, DungeonLevel.DoorColor> keys = level.getKeysOnGround();
        GameMap.Direction dir = GameMap.Direction.getRandomBasicDirection();
        DungeonLevel.Position start = level.getStartPosition();
        List<Enemy> enemies = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        World w = new World(enemies, items, player, level);
        player.setPosX(start.x);
        player.setPosY(start.y);
        DungeonLevel.Position exit = level.getExitPosition();


        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {

                DungeonLevel.Position p = new DungeonLevel.Position(x, y);
                // старт / выход
                if (p.equals(start)) {
                    System.out.print('S');
                    continue;
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
