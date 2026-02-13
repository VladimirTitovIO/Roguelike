package com.roguegame.domain;

public class ItemTypes {
    public enum Type {
        WEAPON {
            @Override
            public String format(Subtype s) {
                return s.prettyName() + " (STR: " + s.strength + ")";
            }
        },
        MEDKIT {
            @Override
            public String format(Subtype s) {
                return s.prettyName() + " (HP: " + s.health + ")";
            }
        },
        ELIXIR {
            @Override
            public String format(Subtype s) {
                if (s.equals(Subtype.ELIXIR_AGILITY)) {
                    return s.prettyName() + " (AGI: " + s.agility + ")";
                } else if (s.equals(Subtype.ELIXIR_STRENGTH)) {
                    return s.prettyName() + " (STR: " + s.strength + ")";
                } else {
                    return s.prettyName() + " (MHP: " + s.maximumHealth + ")";
                }
            }
        },
        FOOD {
            @Override
            public String format(Subtype s) {
                return s.prettyName() + " (HP: " + s.health + ")";
            }
        },
        TREASURE {
            @Override
            public String format(Subtype s) {
                return s.prettyName() + " (VAL: " + s.value + ")";
            }
        },
        SCROLLS {
            @Override
            public String format(Subtype s) {
                if (s.equals(Subtype.SCROLLS_AGILITY)) {
                    return s.prettyName() + " {AGI: " + s.agility + ")";
                } else if (s.equals(Subtype.SCROLLS_STRENGTH)) {
                    return s.prettyName() + " {STR: " + s.strength + ")";
                } else {
                    return s.prettyName() + " {MHP: " + s.maximumHealth + ")";
                }
            }
        };

        public abstract String format(Subtype s);
    }

    public enum Subtype {
        FISTS(ItemTypes.Type.WEAPON, 0, 0, 0, 1, 0),
        AXE(ItemTypes.Type.WEAPON, 0, 0, 0, 4, 0),
        SWORD(ItemTypes.Type.WEAPON, 0, 0, 0, 2, 0),
        MACE(ItemTypes.Type.WEAPON, 0, 0, 0, 3, 0),

        MEDKIT_SMALL(ItemTypes.Type.MEDKIT, 5, 0, 0, 0, 0),
        MEDKIT_MEDIUM(ItemTypes.Type.MEDKIT, 9, 0, 0, 0, 0),
        MEDKIT_BIG(ItemTypes.Type.MEDKIT, 12, 0, 0, 0, 0),

        ELIXIR_STRENGTH(ItemTypes.Type.ELIXIR, 0, 0, 0, 2, 0),
        ELIXIR_AGILITY(ItemTypes.Type.ELIXIR, 0, 0, 2, 0, 0),
        ELIXIR_MAX_HEALTH(ItemTypes.Type.ELIXIR, 0, 4, 0, 0, 0),

        FOOD_SMALL(ItemTypes.Type.FOOD, 4, 0, 0, 0, 0),
        FOOD_MEDIUM(ItemTypes.Type.FOOD, 8, 0, 0, 0, 0),
        FOOD_BIG(ItemTypes.Type.FOOD, 11, 0, 0, 0, 0),

        TREASURE_SMALL(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 10),
        TREASURE_MEDIUM(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 15),
        TREASURE_BIG(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 20),

        SCROLLS_STRENGTH(ItemTypes.Type.SCROLLS, 0, 0, 0, 3, 0),
        SCROLLS_AGILITY(ItemTypes.Type.SCROLLS, 0, 0, 3, 0, 0),
        SCROLLS_MAX_HEALTH(ItemTypes.Type.SCROLLS, 0, 5, 0, 0, 0);

        private final ItemTypes.Type type;
        private final int maximumHealth;
        private final int agility;
        private final int strength;
        private final int value;
        private final int health;

        public int getHealth() {
            return health;
        }

        public int getValue() {
            return value;
        }

        public int getStrength() {
            return strength;
        }

        public int getAgility() {
            return agility;
        }

        public int getMaximumHealth() {
            return maximumHealth;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.format(this);
        }

        public String prettyName() {
            return name().replace('_', ' ').toLowerCase();
        }


        Subtype(ItemTypes.Type type, int health, int maximumHealth, int agility, int strength, int value) {
            this.type = type;
            this.agility = agility;
            this.strength = strength;
            this.value = value;
            this.health = health;
            this.maximumHealth = maximumHealth;
        }
    }
}

