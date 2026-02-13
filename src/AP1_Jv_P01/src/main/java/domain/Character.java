package domain;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import domain.ItemTypes.Subtype;

public class Character extends Entity {
    private int maximumHealth;
    private int health;
    private int strength;
    private int experience;
    private int gold;
    private final Backpack backpack;
    private Subtype weapon;
    private boolean alive = true;
    private int height;
    private int width;
    private final Map<ItemTypes.Subtype, Integer> activeBuffs;

    public Backpack getBackpack() {
        return backpack;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void setMaximumHealth(int maximumHealth) {
        this.maximumHealth = maximumHealth;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
    }

    public int getStrength() {
        int tempStrength = 0;
        if (weapon != null) {
            tempStrength += weapon.getStrength();
        }
        if (activeBuffs.containsKey(Subtype.ELIXIR_STRENGTH)) {
            tempStrength += Subtype.ELIXIR_STRENGTH.getStrength();
        }
        return strength + tempStrength;
    }
    @Override
    public int getAgility() {
        int tempAgility = 0;
        if (activeBuffs.containsKey(Subtype.ELIXIR_AGILITY)) {
            tempAgility += Subtype.ELIXIR_AGILITY.getAgility();
        }
        return getBaseAgility() + tempAgility;
    }

    public int getHealth() {
        return health;
    }

    public ItemTypes.Subtype getWeapon() {
        return weapon;
    }

    public void setWeapon(ItemTypes.Subtype weapon) {
        this.weapon = weapon;
    }

    public int getMaximumHealth() {
        int tempMaximumHealth = 0;
        if (activeBuffs.containsKey(Subtype.ELIXIR_MAX_HEALTH)) {
            tempMaximumHealth += Subtype.ELIXIR_MAX_HEALTH.getMaximumHealth();
        }
        return maximumHealth + tempMaximumHealth;
    }

    public boolean isAlive() {
        return alive;
    }

    public void die() {
        alive = false;
    }

    public Character(int posX, int posY) {
        super(posX, posY,1, 1);
        maximumHealth = 10;
        health = 10;
        setAgility(4);
        strength = 4;
        experience = 0;
        gold = 0;
        setSpeed(2);
        backpack = new Backpack(45);
        weapon = Subtype.FISTS;
        activeBuffs = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Character: {health: " + health + ", maximum health: " + maximumHealth + ", agility: " +
                getAgility() + ", \nstrength: " + strength + ", speed: " + getSpeed() + ", experience: " + experience
                + ", gold: " + gold + "}";
    }

    public void addBuff(ItemTypes.Subtype item) {
        activeBuffs.put(item, 10);
    }

    public void tickBuffs() {
        activeBuffs.replaceAll((k, v) -> v - 1);
        activeBuffs.entrySet().removeIf(e -> e.getValue() <= 0);
    }
}