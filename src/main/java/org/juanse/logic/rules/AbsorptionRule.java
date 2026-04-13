package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla 1 – Absorción por tamaño:
 * Una célula puede comerse a otra solo si su masa es al menos un 10% mayor.
 */
public class AbsorptionRule implements IGameRule {

    private static final double SIZE_ADVANTAGE = 1.10;

    @Override
    public void apply(GameEngine engine) {
        List<PlayerCell> players = engine.getPlayers();
        List<PlayerCell> toRemove = new ArrayList<>();

        for (PlayerCell attacker : players) {
            if (!attacker.isAlive() || toRemove.contains(attacker)) continue;

            for (PlayerCell target : players) {
                if (attacker == target) continue;
                if (!target.isAlive() || toRemove.contains(target)) continue;

                if (attacker.getOwnerName().equals(target.getOwnerName())) {
                    if (attacker.canMergeWith(target) && attacker.collidesWith(target)) {
                        attacker.growBy(target.getMass());
                        target.kill();
                        toRemove.add(target);
                        engine.notifyAbsorption(attacker, target);
                    }
                    continue;
                }

                if (canAbsorb(attacker, target) && attacker.collidesWith(target)) {
                    attacker.growBy(target.getMass());
                    target.kill();
                    toRemove.add(target);
                    engine.notifyAbsorption(attacker, target);
                }
            }
        }

        toRemove.forEach(cell -> engine.removePlayer(cell));
    }

    /**
     * El atacante debe tener al menos un 10% más de masa que el objetivo.
     */
    private boolean canAbsorb(PlayerCell attacker, PlayerCell target) {
        return attacker.getMass() >= target.getMass() * SIZE_ADVANTAGE;
    }
}