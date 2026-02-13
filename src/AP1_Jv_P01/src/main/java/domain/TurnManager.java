package domain;

import java.util.concurrent.ThreadLocalRandom;

public class TurnManager {
    public enum Turn {
        PLAYER,
        ENEMY;

    }
    TurnManager(TurnManager.Turn currentTurn) {
        this.currentTurn = currentTurn;
        setTurnCalculated(true);
    }

    private Turn currentTurn;

    public boolean isTurnCalculated() {
        return turnCalculated;
    }
    public void setTurnCalculated(boolean turnCalculated) {
        this.turnCalculated = turnCalculated;
    }
    private boolean turnCalculated = false;


    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public void calculateTurn(Character player, Enemy monster) {
        int playerInitiateRoll = player.getAgility() + ThreadLocalRandom.current().nextInt(1, 20);
        int monsterInitiateRoll = monster.getAgility() + ThreadLocalRandom.current().nextInt(1, 20);
        if (playerInitiateRoll >= monsterInitiateRoll) {
            currentTurn = Turn.PLAYER;
        } else {
            currentTurn = Turn.ENEMY;
        }
    }

    public void nextTurn() {
        if (currentTurn.equals(Turn.PLAYER)) currentTurn = Turn.ENEMY;
        else currentTurn = Turn.PLAYER;
    }
    @Override
    public String toString() {
        return "Current turn: " + currentTurn;
    }
}

