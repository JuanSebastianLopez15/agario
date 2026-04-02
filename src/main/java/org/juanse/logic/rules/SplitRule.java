package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.HazardBall;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla 3 – División por bola peligrosa:
 * Si una célula toca una HazardBall, se divide en dos partes iguales.
 * La HazardBall no puede ser comida y permanece en el mapa.
 */
public class SplitRule implements IGameRule {

    @Override
    public void apply(GameEngine engine) {
        List<PlayerCell> toAdd = new ArrayList<>();

        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            for (HazardBall hazard : engine.getHazardBalls()) {
                if (player.collidesWith(hazard)) {
                    // Solo se divide si tiene masa suficiente para que ambas mitades sean viables
                    if (player.getMass() > 20.0) {
                        PlayerCell newCell = player.splitInto();
                        toAdd.add(newCell);
                        engine.notifySplit(player, newCell);
                    }
                    break; // una sola colisión por tick
                }
            }
        }

        toAdd.forEach(engine::addPlayer);
    }
}

