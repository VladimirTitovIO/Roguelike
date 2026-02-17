package com.roguegame.domain;
import com.roguegame.domain.GameMap.Direction;


public abstract class Entity {
    private int posX;
    private int posY;
    private int speed;
    private int agility;
    public boolean alive = true;
    public boolean playerAsleep = false;

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Entity(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBaseAgility() {
        return agility;
    }

    public int getAgility() {
        return agility;
    }

    protected void setSpeed(int speed) {
        this.speed = speed;
    }

    protected void setAgility(int agility) {
        this.agility =
                agility;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosX(int x) {
        posX = x;
    }

    public void setPosY(int y) {
        posY = y;
    }

    public boolean isPlayerAsleep() {
        return playerAsleep;
    }

    public void setPlayerAsleep(boolean playerAsleep) {
        this.playerAsleep = playerAsleep;
    }

    public void moveCharacterByDirection(Direction direction) {
        switch (direction) {
            case UP:
                posY--;
                break;
            case DOWN:
                posY++;
                break;
            case LEFT:
                posX--;
                break;
            case RIGHT:
                posX++;
                break;
            case DIAGONALLY_DOWN_RIGHT:
                posX++;
                posY++;
                break;
            case DIAGONALLY_DOWN_LEFT:
                posX--;
                posY++;
                break;
            case DIAGONALLY_UP_LEFT:
                posX--;
                posY--;
                break;
            case DIAGONALLY_UP_RIGHT:
                posX++;
                posY--;
                break;
            case STOP:
                break;
        }
    }
}