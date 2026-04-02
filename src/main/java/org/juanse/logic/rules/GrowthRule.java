package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla 2 – Crecimiento por pellets:
 * Comer un pellet aumenta la masa del jugador en la masa fija del pellet.
 */
public class GrowthRule implements IGameRule {

    @Override
    public void apply(GameEngine engine) {
        List<Pellet> consumed = new ArrayList<>();

        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            for (Pellet pellet : engine.getPellets()) {
                if (pellet.isConsumed()) continue;

                if (player.collidesWith(pellet)) {
                    player.growBy(pellet.getMass());
                    pellet.consume();
                    consumed.add(pellet);
                    engine.notifyPelletEaten(player, pellet);
                }
            }
        }

        // Eliminar pellets consumidos y generar nuevos para mantener densidad
        consumed.forEach(engine::removePellet);
        consumed.forEach(p -> engine.spawnPellet());
    }
}

