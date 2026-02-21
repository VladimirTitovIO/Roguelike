package com.roguegame.domain;

public class CombatResult {
    public final String attacker;
    public final String target;
    public final int damage;
    public final boolean hit;

    public CombatResult(String attacker, String target, int damage, boolean hit) {
        this.attacker = attacker;
        this.target = target;
        this.damage = damage;
        this.hit = hit;
    }

    @Override
    public String toString() {
        if (!hit) {
            return attacker + " missed " + target + "!";
        }
        return attacker + " dealt " + damage + " to: "+ target;
    }
}
