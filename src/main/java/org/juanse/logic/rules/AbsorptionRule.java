package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla de absorcion por tamano.
 * Una celula puede comerse a otra solo si su masa es al menos un 10% mayor.
 * Tambien permite la fusion de celulas del mismo jugador si el cooldown lo permite.
 *
 * <p>Principio S (SRP): solo aplica la logica de absorcion.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class AbsorptionRule implements IGameRule {

    /**
     * Ventaja minima de masa requerida para absorber a otro jugador.
     * El atacante debe tener al menos un 10% mas de masa que el objetivo.
     */
    private static final double SIZE_ADVANTAGE = 1.10;

    /**
     * Aplica la regla de absorcion en cada tick del juego.
     * Recorre todas las celulas buscando colisiones donde una puede absorber a otra.
     * Tambien fusiona celulas del mismo jugador si el cooldown lo permite.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void apply(GameEngine engine) {
        List<PlayerCell> players  = engine.getPlayers();
        List<PlayerCell> toRemove = new ArrayList<>();

        for (PlayerCell attacker : players) {
            if (!attacker.isAlive() || toRemove.contains(attacker)) continue;

            for (PlayerCell target : players) {
                if (attacker == target) continue;
                if (!target.isAlive() || toRemove.contains(target)) continue;

                // Fusion de celulas del mismo jugador
                if (attacker.getOwnerName().equals(target.getOwnerName())) {
                    if (attacker.canMergeWith(target) && attacker.collidesWith(target)) {
                        attacker.growBy(target.getMass());
                        target.kill();
                        toRemove.add(target);
                        engine.notifyAbsorption(attacker, target);
                    }
                    continue;
                }

                // Absorcion de celulas enemigas
                if (canAbsorb(attacker, target) && attacker.collidesWith(target)) {
                    attacker.growBy(target.getMass());
                    target.kill();
                    toRemove.add(target);
                    engine.notifyAbsorption(attacker, target);
                }
            }
        }

        toRemove.forEach(engine::removePlayer);
    }

    /**
     * Verifica si el atacante puede absorber al objetivo.
     * El atacante debe tener al menos un 10% mas de masa.
     *
     * @param attacker celula atacante
     * @param target   celula objetivo
     * @return true si el atacante puede absorber al objetivo
     */
    private boolean canAbsorb(PlayerCell attacker, PlayerCell target) {
        return attacker.getMass() >= target.getMass() * SIZE_ADVANTAGE;
    }
}