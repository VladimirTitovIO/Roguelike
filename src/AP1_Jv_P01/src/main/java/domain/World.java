package domain;

import java.nio.file.FileSystem;
import java.util.*;

import domain.GameMap.Direction;

public class World {
    private List<Enemy> enemies;
    private List<Item> items;
    private final Character player;
    private DungeonLevel level;
    private int levelNumber;
    private Map<DungeonLevel.Position, DungeonLevel.DoorColor> keys;
    private double struggleCounter;

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Character getPlayer() {
        return player;
    }

    public DungeonLevel getLevel() {
        return level;
    }

    public void setLevel(DungeonLevel level) {
        this.level = level;
    }

    public Map<DungeonLevel.Position, DungeonLevel.DoorColor> getKeys() {
        return keys;
    }

    public double getStruggleCounter() {
        return struggleCounter;
    }

    public void setStruggleCounter(double struggleCounter) {
        this.struggleCounter = struggleCounter;
    }

    public World(Character player, DungeonLevel level,  Map<DungeonLevel.Position, DungeonLevel.DoorColor> keys) {
        enemies = getEnemies();
        items = getItems();
        this.player = player;
        this.level = level;
        levelNumber = 1;
        this.keys = keys;
        struggleCounter = 0.5;
    }

    public Enemy getEnemyAtPosition(int x, int y) {
        for (Enemy e : getEnemies()) {
            if (x == e.getPosX() && y == e.getPosY()) {
                return e;
            }
        }
        return null;
    }

    public boolean isEnemyOccupied(int x, int y) {
        return getEnemyAtPosition(x, y) != null;
    }

    public boolean tryToMove(Entity e, Direction dir) {
        int newPosY = e.getPosY();
        int newPosX = e.getPosX();
        switch (dir) {
            case UP: newPosY--; break;
            case DOWN: newPosY++; break;
            case LEFT: newPosX--; break;
            case RIGHT: newPosX++; break;
            case DIAGONALLY_DOWN_LEFT: newPosY++; newPosX--; break;
            case DIAGONALLY_DOWN_RIGHT: newPosY++; newPosX++; break;
            case DIAGONALLY_UP_LEFT: newPosY--; newPosX--; break;
            case DIAGONALLY_UP_RIGHT: newPosY--; newPosX++; break;
            case STOP: return false;
        }
        if (e == player) {
            if (getLevel().getTile(newPosX, newPosY) == GameMap.TileType.DOOR) {
                if (!getLevel().tryOpenDoor(newPosX, newPosY, player.getKeys())) return false;
            }
            Enemy mimic = getMimicAtPosition(newPosX, newPosY);
            if (mimic != null && !mimic.isMimicRevealed()) {
                mimic.setMimicRevealed(true);
                return false;
            }
            Enemy other = getEnemyAtPosition(newPosX, newPosY);
            if (other != null) {
                handleCombat((Character) e, other);
                return false;
            }

        }
        if (!level.isWalkable(newPosX, newPosY) || isTileOccupiedFor(e, newPosX, newPosY)) {
            return false;
        }
        e.moveCharacterByDirection(dir);
        if (e == player) {
            tryToPickUp((Character) e);
        }
        return true;
    }

    private void handleCombat(Character player, Enemy enemy) {
        Combat c = new Combat();
        TurnManager playerTurn = new TurnManager(TurnManager.Turn.PLAYER);
        c.attack(player, enemy, playerTurn);
        if (enemy.getHealth() <= 0) {
           enemy.setAlive(false);
        }
    }

    public boolean tryToTeleport(Enemy e, int x, int y) {
        if (!level.isWalkable(x, y) || isTileOccupiedFor(e, x, y)) {
            return false;
        }
        e.setPosX(x);
        e.setPosY(y);
        return true;
    }

    private Enemy getMimicAtPosition(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.getType() == Enemy.Type.MIMIC) {
                if (enemy.getPosX() == x && enemy.getPosY() == y) {
                    return enemy;
                }
            }
        }
        return null;
    }

    public void tryToPickUp(Character player){
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.isItemOnGround() && item.getPosX() == player.getPosX()
                    && item.getPosY() == player.getPosY()) {
                player.getBackpack().addItem(item);
                item.pickItemUpBy(player);
                it.remove();
            }
        }
        Optional<DungeonLevel.DoorColor> key = getLevel().pickupKeyAt(player.getPosX(), player.getPosY());
        key.ifPresent(color -> {
            player.addKey(color);
        });
    }

    public boolean isTileOccupiedFor(Entity e, int x, int y) {
        if (e instanceof Enemy) {
            if (player.getPosY() == y && player.getPosX() == x) {
                return true;
            }
            for (Enemy enemy : enemies) {
                if (enemy.getPosX() == x && enemy.getPosY() == y) {
                    return true;
                }
            }
        }
        if (e == player && isEnemyOccupied(x, y)) {
                return true;
        }
        return false;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public List<Entity> calculateTurn(Character player, List<Enemy> enemies) {
        List<Entity> turnOrder = new ArrayList<>();
        turnOrder.add(player);
        turnOrder.addAll(enemies);
        turnOrder.sort((a, b) -> Integer.compare(b.getSpeed() + b.getAgility(), a.getSpeed() + a.getAgility()));
        return turnOrder;
    }

    public void handlePlayerTurn(World w, String input, Scanner scanner, Character player) {
        if (Objects.equals(input, "w")) {
            w.tryToMove(player, GameMap.Direction.UP);
        } else if (Objects.equals(input, "d")) {
            w.tryToMove(player, GameMap.Direction.RIGHT);
        } else if (Objects.equals(input, "a")) {
            w.tryToMove(player, GameMap.Direction.LEFT);
        } else if (Objects.equals(input, "s")) {
            w.tryToMove(player, GameMap.Direction.DOWN);
        } else if (Objects.equals(input, "h")) {
            List<Item> weapons = player.getBackpack().getItems().stream().filter(i -> i.getType() == ItemTypes.Type.WEAPON).toList();
            int index = ListWeapons(weapons, scanner);
            if (index != -1) {
                Item oldWeapon = player.getWeapon();
                Item currentWeapon = weapons.get(index);
                if (oldWeapon.getSubtype() == ItemTypes.Subtype.FISTS) player.getBackpack().useItem(weapons.get(index), player);
                if (oldWeapon.getSubtype() != ItemTypes.Subtype.FISTS && oldWeapon != currentWeapon) {
                    if (dropItemNear(player, oldWeapon)) {
                        player.getBackpack().useItem(weapons.get(index), player);
                    }
                }
                System.out.println("Player weapon = " + player.getWeapon());
            }
        } else if (Objects.equals(input, "j")) {
            List<Item> elixirs = player.getBackpack().getItems().stream().filter(i -> i.getType() == ItemTypes.Type.ELIXIR).toList();
            int index = ListElixirs(elixirs, scanner);
            if (index != -1) {
                player.getBackpack().useItem(elixirs.get(index), player);

            }
        } else if (Objects.equals(input, "k")) {
            List<Item> foods = player.getBackpack().getItems().stream().filter(i -> i.getType() == ItemTypes.Type.FOOD).toList();
            int index = ListFoods(foods, scanner);
            if (index != -1) {
                player.getBackpack().useItem(foods.get(index), player);
            }
        } else if (Objects.equals(input, "l")) {
            List<Item> medkits = player.getBackpack().getItems().stream().filter(i -> i.getType() == ItemTypes.Type.MEDKIT).toList();
            int index = ListMedkits(medkits, scanner);
            if (index != -1) {
                player.getBackpack().useItem(medkits.get(index), player);
            }
        } else if (Objects.equals(input, "t")) {
            List<Item> scrolls = player.getBackpack().getItems().stream().filter(i -> i.getType() == ItemTypes.Type.SCROLLS).toList();
            int index = ListScrolls(scrolls, scanner);
            if (index != -1) {
                player.getBackpack().useItem(scrolls.get(index), player);
            }
        } else if (Objects.equals(input, "u")) {
            for (DungeonLevel.DoorColor key : player.getKeys()) {
                System.out.println(key);
            }
        }
        calculateStruggle();
        player.tickBuffs();
    }

    private int ListWeapons(List<Item> weapons, Scanner scanner) {
        if (weapons.isEmpty()) {
            System.out.println("No weapons at the moment");
            return -1;
        }
        System.out.println("Weapons: ");
        for (int i = 0; i < weapons.size(); i++) {
            System.out.println(i + ": " + weapons.get(i));
        }
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            scanner.nextLine();
            return -1;
        }
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index < 0 || index >= weapons.size()) {
            System.out.println("Invalid index");
            return -1;
        }
        return index;
    }
    private int ListElixirs(List<Item> elixirs, Scanner scanner) {
        if (elixirs.isEmpty()) {
            System.out.println("No elixirs at the moment");
            return -1;
        }
        System.out.println("Elixirs");
        for (int i = 0; i < elixirs.size(); i++) {
            System.out.println(i + ": " + elixirs.get(i));
        }
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            scanner.nextLine();
            return -1;
        }
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index < 0 || index > elixirs.size()) {
            System.out.println("Invalid index");
            return -1;
        }
        return index;
    }
    private int ListFoods(List<Item> foods, Scanner scanner) {
        if (foods.isEmpty()) {
            System.out.println("No foods at the moment");
            return -1;
        }
        System.out.println("Foods");
        for (int i = 0; i < foods.size(); i++) {
            System.out.println(i + ": " + foods.get(i));
        }
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            scanner.nextLine();
            return -1;
        }
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index < 0 || index > foods.size()) {
            System.out.println("Invalid index");
            return -1;
        }
        return index;
    }
    private int ListMedkits(List<Item> medkits, Scanner scanner) {
        if (medkits.isEmpty()) {
            System.out.println("No medkits at the moment");
            return -1;
        }
        System.out.println("Medkits");
        for (int i = 0; i < medkits.size(); i++) {
            System.out.println(i + ": " + medkits.get(i));
        }
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            scanner.nextLine();
            return -1;
        }
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index < 0 || index > medkits.size()) {
            System.out.println("Invalid index");
            return -1;
        }
        return index;
    }
    private int ListScrolls(List<Item> scrolls, Scanner scanner) {
        if (scrolls.isEmpty()) {
            System.out.println("No scrolls at the moment");
            return -1;
        }
        System.out.println("Scrolls");
        for (int i = 0; i < scrolls.size(); i++) {
            System.out.println(i + ": " + scrolls.get(i));
        }
        if (!scanner.hasNextInt()) {
            System.out.println("Invalid input");
            scanner.nextLine();
            return -1;
        }
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index < 0 || index > scrolls.size()) {
            System.out.println("Invalid index");
            return -1;
        }
        return index;
    }

    private boolean dropItemNear(Character player, Item weapon) {
        Direction[] directions = {
                Direction.UP,
                Direction.DOWN,
                Direction.LEFT,
                Direction.RIGHT
        };
        for(Direction dir : directions) {
            int ny = player.getPosY();
            int nx = player.getPosX();
            switch (dir) {
                case UP -> ny--;
                case DOWN -> ny++;
                case LEFT -> nx--;
                case RIGHT -> nx++;
            }
            if (isTileBlocked(nx, ny)) continue;
            if (getLevel().isWalkable(nx, ny)) {
                weapon.dropItemAt(nx, ny);
                items.add(weapon);
                return true;
            }
        }
        System.out.println("No free tile to drop weapon");
        return false;
    }

    private boolean isTileBlocked(int x, int y) {
        for (Item i : items) {
            if (i.getPosX() == x && i.getPosY() == y) {
                return true;
            }
        }
        for (Enemy e : enemies) {
            if (e.getPosX() == x || e.getPosY() == y) {
                return true;
            }
        }
        return false;
    }

    public void calculateStruggle() {
        double ratio = player.getHealth() / (double) player.getMaximumHealth();
        double difficulty = 1.0 - ratio;
        //not to drop instantly to easiest struggle
        double smoothing = 0.1;
        struggleCounter += (difficulty - ratio) * smoothing;
        struggleCounter = Math.max(0.0, Math.min(1.0, struggleCounter));
    }

    public void scaleEnemiesStrength(int level, List<Enemy> enemies) {
        for (Enemy e : enemies) {
            int scaledStrength = (int) (e.getType().getStrength() * (1 + level * 0.25));
            e.setStrength(e.getStrength() + scaledStrength);
        }
    }
}