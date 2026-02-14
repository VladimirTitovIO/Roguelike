package domain;

import domain.ItemTypes.Subtype;

public class Item {
    private int health;
    private int maximumHealth;
    private int agility;
    private int strength;
    private int value;
    private final ItemTypes.Type type;
    private Subtype subtype;
    private Character owner;
    private int posX;
    private int posY;


    public int getHealth() {
        return health;
    }

    public int getMaximumHealth() {
        return maximumHealth;
    }

    public int getAgility() {
        return agility;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getValue() {
        return value;
    }

    public Subtype getSubtype() {
        return subtype;
    }

    public void setSubtype(ItemTypes.Subtype subtype) {
        this.subtype = subtype;
    }

    public ItemTypes.Type getType() {
        return type;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public boolean isItemOnGround() {
        return owner == null;
    }

    public boolean isItemInInventory() {
        return owner != null;
    }

    public void pickItemUpBy(Character c) {
        owner = c;
    }

    public void dropItemAt(int posX, int posY) {
        owner = null;
        this.posX = posX;
        this.posY = posY;
    }

    public Item(Subtype subtype, int posX, int posY) {
        type = subtype.getType();
        this.subtype = subtype;
        health = subtype.getHealth();
        maximumHealth = subtype.getMaximumHealth();
        agility = subtype.getAgility();
        strength = subtype.getStrength();
        value = subtype.getValue();
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public String toString() {
        return subtype.toString();
    }

    public void use(Character target) {
        switch (type) {
            case FOOD, MEDKIT -> useFoodOrMedkit(target);
            case ELIXIR -> useElixir(target);
            case WEAPON -> useWeapon(target);
            case SCROLLS -> useScroll(target);
            case TREASURE -> useTreasure(target);
        }
    }
    //permanently increases stats
    public void useScroll(Character target) {
        if (subtype.equals(Subtype.SCROLLS_AGILITY)) {
            target.setAgility(target.getAgility() + agility);
        } else if (subtype.equals(Subtype.SCROLLS_STRENGTH)) {
            target.setStrength(target.getStrength() + strength);
        } else {
            target.setMaximumHealth(target.getMaximumHealth() + maximumHealth);
        }
    }
    //temporarily increases stats
    public void useElixir(Character target) {
        if (!type.equals(ItemTypes.Type.ELIXIR)) return;
        if (getSubtype().equals(Subtype.ELIXIR_AGILITY)) {
            target.addBuff(Subtype.ELIXIR_AGILITY);
        } else if (getSubtype().equals(Subtype.ELIXIR_STRENGTH)) {
            target.addBuff(Subtype.ELIXIR_STRENGTH);
        } else {
            target.addBuff(Subtype.ELIXIR_MAX_HEALTH);
        }
    }

    public void useFoodOrMedkit(Character target) {
        if (target.getHealth() + health >= target.getMaximumHealth())
            target.setHealth(target.getMaximumHealth());
        else
            target.setHealth(target.getHealth() + health);
    }

    public void useTreasure(Character target) {
        target.setGold(value + target.getGold());
    }

    public void useWeapon(Character target) {
        target.setWeapon(this);
    }
}