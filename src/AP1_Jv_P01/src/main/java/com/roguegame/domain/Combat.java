package com.roguegame.domain;

import com.roguegame.domain.TurnManager.Turn;

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
        int damage = 0;
        if (currentTurn.getCurrentTurn().equals(Turn.PLAYER)) {
            boolean hit = checkIfHit(player, monster, currentTurn.getCurrentTurn());
            if (hit) {
                damage = calculatePlayerDamage(player, monster);
                monster.setHealth(monster.getHealth() - damage);
                player.setAttacksLanded(player.getAttacksLanded() + 1);
            } else {
                player.setAttacksMissed(player.getAttacksMissed() + 1);
            }
            if (monster.getHealth() <= 0) {
                player.setGold(monster.getType().getValue() + player.getGold());
            }
            player.setCombatResult(new CombatResult("Player", monster.getType().name(), damage, hit));
        } else {
            boolean hit = checkIfHit(player, monster, currentTurn.getCurrentTurn());
            if (hit) {
                switch (monster.getType()) {
                    case OGRE -> damage = calculateOgreDamage(monster);
                    case VAMPIRE -> damage = calculateVampireDamage(monster, player);
                    case SNAKE_MAGE -> damage = calculateSnakeMageDamage(monster);
                    case ZOMBIE, GHOST -> damage = calculateZombieGhostDamage(monster);
                    case MIMIC -> damage = calculateMimicDamage(monster);
                }
                player.setHealth(player.getHealth() - damage);
            }
            if (player.getHealth() <= 0) {
                player.setAlive(false);
            }
            monster.setCombatResult(new CombatResult(monster.getType().name(), "Player", damage, hit));
        }
    }
}
