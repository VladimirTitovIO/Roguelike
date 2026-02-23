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
        FISTS(ItemTypes.Type.WEAPON, 0, 0, 0, 3, 0, 0),
        AXE(ItemTypes.Type.WEAPON, 0, 0, 0, 7, 0, 2),
        SWORD(ItemTypes.Type.WEAPON, 0, 0, 0, 5, 0, 5),
        MACE(ItemTypes.Type.WEAPON, 0, 0, 0, 6, 0, 3),

        MEDKIT_SMALL(ItemTypes.Type.MEDKIT, 12, 0, 0, 0, 0, 5),
        MEDKIT_MEDIUM(ItemTypes.Type.MEDKIT, 18, 0, 0, 0, 0, 3),
        MEDKIT_BIG(ItemTypes.Type.MEDKIT, 25, 0, 0, 0, 0, 2),

        ELIXIR_STRENGTH(ItemTypes.Type.ELIXIR, 0, 0, 0, 2, 0, 4),
        ELIXIR_AGILITY(ItemTypes.Type.ELIXIR, 0, 0, 2, 0, 0, 4),
        ELIXIR_MAX_HEALTH(ItemTypes.Type.ELIXIR, 0, 10, 0, 0, 0, 2),

        FOOD_SMALL(ItemTypes.Type.FOOD, 5, 0, 0, 0, 0, 5),
        FOOD_MEDIUM(ItemTypes.Type.FOOD, 10, 0, 0, 0, 0, 3),
        FOOD_BIG(ItemTypes.Type.FOOD, 15, 0, 0, 0, 0, 1),

        TREASURE_SMALL(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 15, 5),
        TREASURE_MEDIUM(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 25, 4),
        TREASURE_BIG(ItemTypes.Type.TREASURE, 0, 0, 0, 0, 35, 2),

        SCROLLS_STRENGTH(ItemTypes.Type.SCROLLS, 0, 0, 0, 3, 0, 4),
        SCROLLS_AGILITY(ItemTypes.Type.SCROLLS, 0, 0, 3, 0, 0, 4),
        SCROLLS_MAX_HEALTH(ItemTypes.Type.SCROLLS, 0, 10, 0, 0, 0, 2);

        public static final Subtype[] LOW_STRUGGLE = {
            TREASURE_SMALL,
            TREASURE_BIG,
            TREASURE_MEDIUM,
            ELIXIR_STRENGTH,
            ELIXIR_AGILITY,
            ELIXIR_MAX_HEALTH,
            SCROLLS_AGILITY,
            FOOD_SMALL,
            FOOD_MEDIUM,
            MEDKIT_SMALL,
            SWORD,
            MACE
        };

        public static final Subtype[] HIGH_STRUGGLE = {
          MEDKIT_SMALL,
          MEDKIT_BIG,
          MEDKIT_MEDIUM,
          FOOD_BIG,
          FOOD_MEDIUM,
          SCROLLS_STRENGTH,
          SCROLLS_MAX_HEALTH,
          AXE,
          MACE,
          ELIXIR_STRENGTH,
          ELIXIR_MAX_HEALTH
        };

        public static final Subtype[] BALANCED_STRUGGLE = {
          MEDKIT_SMALL,
          MEDKIT_MEDIUM,
          MEDKIT_BIG,
          TREASURE_SMALL,
          TREASURE_BIG,
          TREASURE_MEDIUM,
          ELIXIR_AGILITY,
          ELIXIR_MAX_HEALTH,
          ELIXIR_STRENGTH,
          FOOD_SMALL,
          FOOD_MEDIUM,
          FOOD_BIG,
          SCROLLS_AGILITY,
          SCROLLS_STRENGTH,
          SCROLLS_MAX_HEALTH,
          SWORD,
          MACE,
          AXE
        };

        private final ItemTypes.Type type;
        private final int maximumHealth;
        private final int agility;
        private final int strength;
        private final int value;
        private final int health;
        private final int weight;

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


        Subtype(ItemTypes.Type type, int health, int maximumHealth, int agility, int strength, int value, int weight) {
            this.type = type;
            this.agility = agility;
            this.strength = strength;
            this.value = value;
            this.health = health;
            this.maximumHealth = maximumHealth;
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }
}
