package com.roguegame.domain;

import com.roguegame.domain.GameMap.Direction;
import com.roguegame.domain.TurnManager.Turn;


abstract class Entity {
    private int posX;
    private int posY;
    private int width;
    private int height;
    private int speed;
    private int agility;

    public Entity(int posX, int posY, int height, int width) {
        this.posX = posX;
        this.posY = posY;
        this.height = height;
        this.width = width;
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

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    //check whether an enemy is 1 tile away from character
    public boolean checkContact(Character player, Enemy enemy) {
        boolean isContact = ((player.getPosX() == enemy.getPosX() && Math.abs(player.getPosY() - enemy.getPosY()) == 1) || (player.getPosY() == enemy.getPosY() && Math.abs(player.getPosX() - enemy.getPosX()) == 1));
        if (!isContact) {
            isContact = (enemy.getType() == Enemy.Type.SNAKE_MAGE && ((Math.abs(player.getPosX() - enemy.getPosX()) == 1) && (Math.abs(player.getPosY() - enemy.getPosY()) == 1)));
        }
        return isContact;
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
    //check if character movement attempt results in fight
    public boolean checkPlayerAttack(Character player, Enemy enemy, Direction playerDirection) {
        boolean isPlayerAttacking = false;
        int newY = player.getPosY();
        int newX = player.getPosX();
        switch (playerDirection) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
            case DIAGONALLY_DOWN_LEFT: newY++; newX--; break;
            case DIAGONALLY_DOWN_RIGHT: newY++; newX++; break;
            case DIAGONALLY_UP_LEFT: newY--; newX--; break;
            case DIAGONALLY_UP_RIGHT: newY--; newX++; break;
            case STOP: break;
        }
        if (newY == enemy.getPosY() && newX == enemy.getPosX()) {
            isPlayerAttacking = true;
        }
        return isPlayerAttacking;
    }

    public void moveCharacter (Character player, Direction playerDirection, World world) {
        Combat combat = new Combat();
        TurnManager currentTurn = new TurnManager(Turn.PLAYER);
        int newPosY = player.getPosY();
        int newPosX = player.getPosX();
        switch (playerDirection) {
            case UP -> newPosY--;
            case DOWN -> newPosY++;
            case LEFT -> newPosX--;
            case RIGHT -> newPosX++;
        }
        Enemy enemy = world.getEnemyAtPosition(newPosX, newPosY);
        if (enemy != null) {
            combat.attack(player, enemy, currentTurn);
            return;
        }
        if (world.getLevel().isWalkable(newPosX, newPosY)) {
            player.setPosX(newPosX);
            player.setPosY(newPosY);
        }
    }


}
