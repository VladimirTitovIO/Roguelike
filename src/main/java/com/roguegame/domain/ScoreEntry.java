package com.roguegame.domain;

public class ScoreEntry {
    private int treasures;
    private int level;
    private int enemiesKilled;
    private int foodUsed;
    private int elixirsUsed;
    private int scrollsUsed;
    private int attacksMade;
    private int attacksMissed;
    private int movesMade;

    // Конструктор
    public ScoreEntry(int treasures, int level, int enemiesKilled, int foodUsed,
                      int elixirsUsed, int scrollsUsed, int attacksMade,
                      int attacksMissed, int movesMade) {
        this.treasures = treasures;
        this.level = level;
        this.enemiesKilled = enemiesKilled;
        this.foodUsed = foodUsed;
        this.elixirsUsed = elixirsUsed;
        this.scrollsUsed = scrollsUsed;
        this.attacksMade = attacksMade;
        this.attacksMissed = attacksMissed;
        this.movesMade = movesMade;
    }

    // Геттеры
    public int getTreasures() { return treasures; }
    public int getLevel() { return level; }
    public int getEnemiesKilled() { return enemiesKilled; }
    public int getFoodUsed() { return foodUsed; }
    public int getElixirsUsed() { return elixirsUsed; }
    public int getScrollsUsed() { return scrollsUsed; }
    public int getAttacksMade() { return attacksMade; }
    public int getAttacksMissed() { return attacksMissed; }
    public int getMovesMade() { return movesMade; }

    public void setTreasures(int treasures) { this.treasures = treasures; }
    public void setLevel(int level) { this.level = level; }
    public void setEnemiesKilled(int enemiesKilled) { this.enemiesKilled = enemiesKilled; }
    public void setFoodUsed(int foodUsed) { this.foodUsed = foodUsed; }
    public void setElixirsUsed(int elixirsUsed) { this.elixirsUsed = elixirsUsed; }
    public void setScrollsUsed(int scrollsUsed) { this.scrollsUsed = scrollsUsed; }
    public void setAttacksMade(int attacksMade) { this.attacksMade = attacksMade; }
    public void setAttacksMissed(int attacksMissed) { this.attacksMissed = attacksMissed; }
    public void setMovesMade(int movesMade) { this.movesMade = movesMade; }
}
