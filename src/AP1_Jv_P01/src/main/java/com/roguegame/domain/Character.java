package com.roguegame.domain;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.roguegame.domain.ItemTypes.Subtype;

public class Character extends Entity {
    private int maximumHealth;
    private int health;
    private int strength;
    private int gold;
    private final Backpack backpack;
    private Item weapon;
    private final Map<ItemTypes.Subtype, Integer> activeBuffs;
    private final Set<DungeonLevel.DoorColor> keys = new HashSet<>();

    public void addKey(DungeonLevel.DoorColor color) {
        keys.add(color);
    }

    public boolean hasKey(DungeonLevel.DoorColor color) {
        return keys.contains(color);
    }

    public Set<DungeonLevel.DoorColor> getKeys() {
        return keys;
    }

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

    public Item getWeapon() {
        return weapon;
    }

    public void setWeapon(Item weapon) {
        this.weapon = weapon;
    }

    public int getMaximumHealth() {
        int tempMaximumHealth = 0;
        if (activeBuffs.containsKey(Subtype.ELIXIR_MAX_HEALTH)) {
            tempMaximumHealth += Subtype.ELIXIR_MAX_HEALTH.getMaximumHealth();
        }
        return maximumHealth + tempMaximumHealth;
    }

    public Character(int posX, int posY) {
        super(posX, posY);
        maximumHealth = 25;
        health = 25;
        setAgility(4);
        strength = 4;
        gold = 0;
        setSpeed(2);
        weapon = new Item(Subtype.FISTS, 0, 0);
        backpack = new Backpack(54);
        activeBuffs = new HashMap<>();
    }

    @Override
    public String toString() {
        return "Character: {health: " + health + ", maximum health: " + maximumHealth + ", agility: " +
                getAgility() + ", \nstrength: " + strength + ", speed: " + getSpeed() + ", gold: " + gold + "}";
    }

    public void addBuff(ItemTypes.Subtype item) {
        activeBuffs.put(item, 40);
    }

    public void tickBuffs() {
        activeBuffs.replaceAll((k, v) -> v - 1);
        activeBuffs.entrySet().removeIf(e -> e.getValue() <= 0);
    }
}