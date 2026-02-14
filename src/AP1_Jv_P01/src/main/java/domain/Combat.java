package domain;

import domain.TurnManager.Turn;

import java.util.Random;

public class Combat {
    private Random random = new Random();

    private int calculatePlayerDamage(Character player, Enemy monster) {
        if (player.getWeapon().getType() != ItemTypes.Type.WEAPON) return 0;
        int base = player.getStrength() + random.nextInt(2);
        double result = 0.0;
        if (!(monster.getType() == Enemy.Type.VAMPIRE && monster.getType().isFirstVampireHit())
                && !(monster.getType() == Enemy.Type.SNAKE_MAGE && monster.isPlayerAsleep())) {
            switch (player.getWeapon().getSubtype()) {
                case SWORD:
                    result = base * 0.6;
                    break;
                case MACE:
                    result = base * 0.7;
                    break;
                case AXE:
                    result = base * 0.8;
                    break;
                default:
                    result = base * 0.45;
                    break;
            }
        } else if (monster.getType() == Enemy.Type.VAMPIRE && monster.getType().isFirstVampireHit()) {
            monster.getType().setFirstVampireHit(false);
            return 0;
        } else {
            monster.setPlayerAsleep(false);
            return 0;
        }

        return (int) Math.max(1, Math.round(result));
    }

    private int calculateZombieGhostDamage(Enemy monster) {
        return (int) Math.max(1, Math.round(monster.getStrength() * 0.55));
    }

    private int calculateVampireDamage(Enemy monster, Character player) {
        if (monster.getType() != Enemy.Type.VAMPIRE) return 0;
        return player.getMaximumHealth() / 10;
    }

    private int calculateOgreDamage(Enemy monster) {
        int damage = 0;
        if (monster.getType() != Enemy.Type.OGRE) return damage;
        if (!monster.getType().isOgreCooldown()) {
            damage = (int) Math.round(monster.getStrength() * 0.75);
            monster.getType().setOgreCooldown(true);
        } else {
            monster.getType().setOgreCooldown(false);
        }
        return damage;
    }

    private int calculateSnakeMageDamage(Enemy monster) {
        int damage = 0;
        if (monster.getType() != Enemy.Type.SNAKE_MAGE) return damage;
        if (Math.random() < Enemy.Type.SLEEP_CHANCE) {
            monster.setPlayerAsleep(true);
            System.out.println("PLAYA SLEEPY PEEPY");
        }
        return calculateZombieGhostDamage(monster);
    }

    private int calculateMimicDamage(Enemy monster) {
        if (monster.getType() != Enemy.Type.MIMIC) return 0;
        return (int) Math.max(1, Math.round(monster.getStrength() * 0.8));
    }

    private boolean checkIfHit(Character player, Enemy monster, TurnManager.Turn currentTurn) {
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
        if (currentTurn.getCurrentTurn().equals(Turn.PLAYER)) {
            if (checkIfHit(player, monster, currentTurn.getCurrentTurn())) {
                int damage = calculatePlayerDamage(player, monster);
                monster.setHealth(monster.getHealth() - damage);
                System.out.println("Player dealt: " + damage + " to: " + monster);
            }
            if (monster.getHealth() <= 0) {
                player.setGold(monster.getType().getValue() + player.getGold());
            }
        } else {
            int damage = 0;
            if (checkIfHit(player, monster, currentTurn.getCurrentTurn()))
                switch (monster.getType()) {
                    case OGRE:
                        damage = calculateOgreDamage(monster);
                        player.setHealth(player.getHealth() - damage);
                        break;
                    case VAMPIRE:
                        damage = calculateVampireDamage(monster, player);
                        player.setHealth(player.getHealth() - damage);
                        break;
                    case SNAKE_MAGE:
                        damage = calculateSnakeMageDamage(monster);
                        player.setHealth(player.getHealth() - damage);
                        break;
                    case ZOMBIE:
                    case GHOST:
                        damage = calculateZombieGhostDamage(monster);
                        player.setHealth(player.getHealth() - damage);
                        break;
                    case MIMIC:
                        damage = calculateMimicDamage(monster);
                        player.setHealth(player.getHealth() - damage);
                        break;
                }
            if (player.getHealth() <= 0) {
                player.setAlive(false);
            }
            System.out.println("Enemy dealt: " + damage + " to: " + player);
        }
    }
}
