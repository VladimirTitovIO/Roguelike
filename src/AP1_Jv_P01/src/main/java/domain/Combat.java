package domain;

import domain.TurnManager.Turn;

public class Combat {

    public int calculatePlayerDamage(Character player, Enemy monster) {
        if (player.getWeapon().getType() != ItemTypes.Type.WEAPON) return 0;
        double result = 0.0;
        if (!(monster.getType() == Enemy.Type.VAMPIRE && monster.getType().isFirstVampireHit())
                && !(monster.getType() == Enemy.Type.SNAKE_MAGE && monster.getType().isPlayerAsleep())) {
            switch (player.getWeapon()) {
                case SWORD:
                    result = (player.getStrength() * 23) / 100.0;
                    break;
                case MACE:
                    result = (player.getStrength() * 21) / 100.0;
                    break;
                case AXE:
                    result = (player.getStrength() * 19) / 100.0;
                    break;
                default:
                    result = ((player.getStrength() + player.getWeapon().getStrength()) * 20) / 100.0;
                    break;
            }
        } else if (monster.getType() == Enemy.Type.VAMPIRE && monster.getType().isFirstVampireHit()) {
            monster.getType().setFirstVampireHit(false);
        } else {
            monster.getType().setPlayerAsleep(false);
        }

        return (int) Math.max(1, Math.round(result));
    }

    public int calculateZombieGhostDamage(Enemy monster) {
        return (int) Math.max(1, Math.round((monster.getStrength() * 20) / 100.0));
    }

    public int calculateVampireDamage(Enemy monster, Character player) {
        if (monster.getType() != Enemy.Type.VAMPIRE) return 0;
        return player.getMaximumHealth() / 10;
    }

    public int calculateOgreDamage(Enemy monster) {
        int damage = 0;
        if (monster.getType() != Enemy.Type.OGRE) return damage;
        if (!monster.getType().isOgreCooldown()) {
            damage = ((monster.getStrength() - 1) * 25) / 100;
            monster.getType().setOgreCooldown(true);
        } else {
            monster.getType().setOgreCooldown(false);
        }
        return damage;
    }

    public int calculateSnakeMageDamage(Enemy monster) {
        int damage = 0;
        if (monster.getType() != Enemy.Type.SNAKE_MAGE) return damage;
        if (Math.random() < Enemy.Type.SLEEP_CHANCE) {
            monster.getType().setPlayerAsleep(true);
        }
        return calculateZombieGhostDamage(monster);
    }

    public boolean checkIfHit(Character player, Enemy monster, TurnManager.Turn currentTurn) {
        double minHitChance = 0.6;
        double maxHitChance = 0.9;
        boolean isHit = false;
        double chanceToHit = minHitChance;
        if (currentTurn.equals(TurnManager.Turn.PLAYER)) {
            chanceToHit = (double) player.getAgility() / (monster.getAgility() + player.getAgility());
        } else {
            chanceToHit = (double) monster.getAgility() / (player.getAgility() + monster.getAgility());
        }
        chanceToHit = Math.max(minHitChance, Math.min(chanceToHit, maxHitChance));
        if (Math.random() < chanceToHit || (monster.getType() == Enemy.Type.OGRE && !monster.getType().isOgreCooldown())) {
            isHit = true;
        }
        return isHit;
    }

    public void attack(Character player, Enemy monster, TurnManager currentTurn) {
        if (!currentTurn.isTurnCalculated()) {
            currentTurn.calculateTurn(player, monster);
            currentTurn.setTurnCalculated(true);
        }
        if (currentTurn.getCurrentTurn().equals(Turn.PLAYER)) {
            if (checkIfHit(player, monster, currentTurn.getCurrentTurn())) {
                monster.setHealth(monster.getHealth() - calculatePlayerDamage(player, monster));
            }
            if (monster.getHealth() <= 0) {
                player.setGold(monster.getType().getValue() + player.getGold());
            }
        } else {
            if (checkIfHit(player, monster, currentTurn.getCurrentTurn()))
                switch (monster.getType()) {
                    case OGRE:
                        player.setHealth(player.getHealth() - calculateOgreDamage(monster));
                        break;
                    case VAMPIRE:
                        player.setHealth(player.getHealth() - calculateVampireDamage(monster, player));
                        break;
                    case SNAKE_MAGE:
                        player.setHealth(player.getHealth() - calculateSnakeMageDamage(monster));
                        break;
                    case ZOMBIE:
                    case GHOST:
                        player.setHealth(player.getHealth() - calculateZombieGhostDamage(monster));
                        break;
                }
        }
        currentTurn.nextTurn();
    }
}
