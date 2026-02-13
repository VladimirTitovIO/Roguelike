package domain;

import domain.GameMap.Direction;
import java.util.Random;


public class Enemy extends Entity {
    private final Type enemyType;
    private int health;
    private int strength;
    private int hostility;
    private int treasureValue;
    private boolean alive = true;
    private boolean visible = true;
    private static final Random random = new Random();

    private static final int MAX_TRIES_TO_MOVE = 16;
    private static final int STAT_VERY_HIGH = 5;
    private static final int STAT_HIGH = 4;
    private static final int STAT_MEDIUM = 3;
    private static final int STAT_LOW = 2;
    private static final int HEALTH_VERY_HIGH = 22;
    private static final int HEALTH_HIGH = 17;
    private static final int HEALTH_MEDIUM = 14;
    private static final int HEALTH_LOW = 10;
    private static final int HOSTILITY_LOW = 3;
    private static final int HOSTILITY_MEDIUM = 5;
    private static final int HOSTILITY_HIGH = 7;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public enum Type {
        //Type(int health, int agility, int strength, int speed, int hostility, int treasure_value, height, width)
        ZOMBIE(HEALTH_HIGH, STAT_LOW, STAT_MEDIUM, STAT_MEDIUM - 1, HOSTILITY_MEDIUM, 15, 1, 1),
        VAMPIRE(HEALTH_HIGH, STAT_HIGH, STAT_MEDIUM, STAT_HIGH - 1, HOSTILITY_HIGH, 20, 1, 1),
        GHOST(HEALTH_LOW, STAT_HIGH, STAT_LOW, STAT_HIGH - 1, HOSTILITY_LOW, 10, 1, 1),
        OGRE(HEALTH_VERY_HIGH, STAT_LOW, STAT_VERY_HIGH, STAT_LOW - 1, HOSTILITY_MEDIUM, 25, 2, 2),
        SNAKE_MAGE(HEALTH_MEDIUM, STAT_VERY_HIGH, STAT_LOW, STAT_VERY_HIGH - 1, HOSTILITY_HIGH, 25, 1, 1);

        private final int health;
        private final int agility;
        private final int strength;
        private final int speed;
        private final int hostility;
        private final int treasureValue;
        private boolean ogreCooldown = false;
        public boolean firstVampireHit = true;
        public boolean playerAsleep = false;
        public static final double SLEEP_CHANCE = 0.15;
        private int height;
        private int width;

        public int getHealth() {
            return health;
        }

        public int getAgility() {
            return agility;
        }

        public int getStrength() {
            return strength;
        }

        public int getSpeed() {
            return speed;
        }

        public int getHostility() {
            return hostility;
        }

        public int getValue() {
            return treasureValue;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public boolean isOgreCooldown() {
            return ogreCooldown;
        }

        public void setOgreCooldown(boolean cooldown) {
            ogreCooldown = cooldown;
        }

        public boolean isFirstVampireHit() {
            return firstVampireHit;
        }

        public void setFirstVampireHit(boolean firstVampireHit) {
            this.firstVampireHit = firstVampireHit;
        }

        public boolean isPlayerAsleep() {
            return playerAsleep;
        }

        public void setPlayerAsleep(boolean playerAsleep) {
            this.playerAsleep = playerAsleep;
        }

        Type(int health, int agility, int strength, int speed, int hostility, int treasure_value, int height, int width) {
            this.health = health;
            this.agility = agility;
            this.strength = strength;
            this.speed = speed;
            this.hostility = hostility;
            this.treasureValue = treasure_value;
            this.height = height;
            this.width = width;
        }
    }


    public Enemy(Type enemyType, int posX, int posY) {
        super(posX, posY, enemyType.height, enemyType.width);
        this.enemyType = enemyType;
        health = enemyType.getHealth();
        setAgility(enemyType.getAgility());
        strength = enemyType.getStrength();
        hostility = enemyType.getHostility();
        setSpeed(enemyType.getSpeed());
        treasureValue = enemyType.getValue();
    }

    public void die() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    public Type getType() {
        return enemyType;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHostility() {
        return hostility;
    }

    public int getTreasureValue() {
        return treasureValue;
    }

    @Override
    public String toString() {
        return "Monster: {type: " + enemyType + ", health: " + health + ", strength: " + strength
                + ", agility: " + getAgility() + ", speed: " + getSpeed() + ", hostility: " + hostility
                + ", treasure value: " + treasureValue;
    }

    public void zombieMovementPattern(Enemy zombie, World world) {
        if (checkIfInSameRoom(zombie, world)) {
            for (int i = 0; i < MAX_TRIES_TO_MOVE; i++) {
                Direction movementDir = Direction.getRandomBasicDirection();
                if (world.tryToMove(zombie, movementDir)) return;
            }
        }
    }

    public void vampireMovementPattern(Enemy vampire, World world) {
        if (checkIfInSameRoom(vampire, world)) {
            for (int i = 0; i < MAX_TRIES_TO_MOVE; i++) {
                Direction movementDir = Direction.getRandomAllDirections();
                if (world.tryToMove(vampire, movementDir)) return;
            }
        }
    }

    public void ghostMovementPattern(Enemy ghost, World world) {
        if (checkIfInSameRoom(ghost, world)) {
            for (int i = 0; i < MAX_TRIES_TO_MOVE; i++) {
                Direction movementDir = Direction.getRandomAllDirections();
                ghost.setPosX(random.nextInt(world.getLevel().getRoomWidth(ghost.getPosX(), ghost.getPosY())));
                ghost.setPosY(random.nextInt(world.getLevel().getRoomHeight(ghost.getPosY(), ghost.getPosY())));
                if (world.tryToMove(ghost, movementDir)) return;
            }
        }
    }

    public void ogreMovementPattern(Enemy ogre, World world) {
        if (checkIfInSameRoom(ogre, world)) {
            for (int i = 0; i < MAX_TRIES_TO_MOVE; i++) {
                Direction movementDir = Direction.getRandomBasicDirection();
                if (world.tryToMove(ogre, movementDir)) return;
            }
        }
    }

    public void snakeMageMovementPattern(Enemy snakeMage, World world) {
        if (checkIfInSameRoom(snakeMage, world)) {
            for (int i = 0; i < MAX_TRIES_TO_MOVE; i++) {
                Direction movementDir = Direction.getRandomDiagonalDirections();
                if (world.tryToMove(snakeMage, movementDir)) return;
            }
        }
    }

    public boolean checkIfInSameRoom(Enemy e, World world) {
        int enemyRoom = world.getLevel().getRoomAt(e.getPosX(), e.getPosY());
        int playerRoom = world.getLevel().getRoomAt(world.getPlayer().getPosX(), world.getPlayer().getPosY());
        if (enemyRoom == -1 || enemyRoom != playerRoom) {
            world.tryToMove(e, Direction.STOP);
            return false;
        }
        return true;
    }

    public boolean isPlayerNear(Character player, Enemy monster) {
        int distance = Math.abs(player.getPosX() - monster.getPosX());
        distance += Math.abs(player.getPosY() - monster.getPosY());
        boolean playerNear = false;
        switch (monster.getHostility()) {
            case HOSTILITY_LOW:
                if (distance <= HOSTILITY_LOW) {
                    playerNear = true;
                }
                break;
            case HOSTILITY_MEDIUM:
                if (distance <= HOSTILITY_MEDIUM) {
                    playerNear = true;
                }
                break;
            case HOSTILITY_HIGH:
                if (distance <= HOSTILITY_HIGH) {
                    playerNear = true;
                }
                break;
        }
        return playerNear;
    }

    public boolean tryToFollowPlayer(Character player, Enemy monster, World world) {
        int dx = player.getPosX() - monster.getPosX();
        int dy = player.getPosY() - monster.getPosY();
        Combat c = new Combat();
        TurnManager enemyTurn = new TurnManager(TurnManager.Turn.ENEMY);
        int stepX = Integer.compare(dx, 0);
        int stepY = Integer.compare(dy, 0);

        if (monster.getType() == Type.ZOMBIE || monster.getType() == Type.OGRE) {
            //Manhattan distance check
            if (Math.abs(dx) + Math.abs(dy) == 1) {
                c.attack(player, monster, enemyTurn);
                return false;
            }
            Direction directionX = (stepX > 0) ? Direction.RIGHT : Direction.LEFT;
            Direction directionY = (stepY > 0) ? Direction.DOWN : Direction.UP;
            if (Math.abs(dx) >= Math.abs(dy)) {
                if (dx != 0 && world.tryToMove(monster, directionX)) return true;
                if (dy != 0 && world.tryToMove(monster, directionY)) return true;
            } else {
                if (dy != 0 && world.tryToMove(monster, directionY)) return true;
                if (dx != 0 && world.tryToMove(monster, directionX)) return true;
            }
        }

        if (monster.getType() == Type.VAMPIRE || monster.getType() == Type.GHOST) {
            if (Math.max(Math.abs(dx), Math.abs(dy)) == 1) {
                c.attack(player, monster, enemyTurn);
                return false;
            }
            if (stepX != 0 && stepY != 0) {
                Direction diagonal = Direction.calculateDiagonalDirection(stepX, stepY);
                if (world.tryToMove(monster, diagonal)) {
                    return true;
                }
            }
            if (Math.abs(dx) >= Math.abs(dy)) {
                if (stepX != 0 && world.tryToMove(monster, stepX > 0 ? Direction.RIGHT : Direction.LEFT)) {
                    return true;
                }
                if (stepY != 0 && world.tryToMove(monster, stepY > 0 ? Direction.DOWN : Direction.UP)) {
                    return true;
                }
            } else {
                if (stepY != 0 && world.tryToMove(monster, stepY > 0 ? Direction.DOWN : Direction.UP)) {
                    return true;
                }
                if (stepX != 0 && world.tryToMove(monster, stepX > 0 ? Direction.RIGHT : Direction.LEFT)) {
                    return true;
                }
            }
        }

        if (monster.getType() == Type.SNAKE_MAGE) {
            if (Math.max(Math.abs(dx), Math.abs(dy)) == 1) {
                c.attack(player, monster, enemyTurn);
                return false;
            }
            if (stepX != 0 && stepY != 0) {
                Direction diagonal = Direction.calculateDiagonalDirection(stepX, stepY);
                if (world.tryToMove(monster, diagonal)) {
                    return true;
                }
            }
        }

        return false;
    }
}
