package com.roguegame.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public interface GameMap {
    int getWidth();
    int getHeight();
    TileType getTile(int x, int y);
    boolean isWalkable(int x, int y);
    Random random = new Random();
    enum TileType {
        FLOOR,
        WALL,
        DOOR,
        CORRIDOR,
        EXIT
    }
    enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        DIAGONALLY_UP_LEFT,
        DIAGONALLY_UP_RIGHT,
        DIAGONALLY_DOWN_LEFT,
        DIAGONALLY_DOWN_RIGHT,
        STOP;

        private static final Direction[] BASIC = {
                UP, DOWN, LEFT, RIGHT
        };
        private static final Direction[] DIAGONAL = {
                DIAGONALLY_UP_LEFT,
                DIAGONALLY_UP_RIGHT,
                DIAGONALLY_DOWN_LEFT,
                DIAGONALLY_DOWN_RIGHT
        };

        public static Direction getRandomBasicDirection() {
            return BASIC[random.nextInt(BASIC.length)];
        }

        public static Direction getRandomAllDirections() {
            List<Direction> values = Arrays.asList(Direction.values());
            return values.get(random.nextInt(values.size()));
        }

        public static Direction getRandomDiagonalDirections() {
            return DIAGONAL[random.nextInt(DIAGONAL.length)];
        }

        public static Direction calculateDiagonalDirection(int x, int y) {
            if (x > 0 && y > 0) {
                return DIAGONALLY_DOWN_RIGHT;
            }
            else if (x > 0 && y < 0) {
                return DIAGONALLY_UP_RIGHT;
            }
            else if (x < 0 && y > 0) {
                return DIAGONALLY_DOWN_LEFT;
            }
            else {
                return DIAGONALLY_UP_LEFT;
            }
        }
    }
}
