package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

/**
 * Regla 4 – Zona de juego:
 * Ninguna célula puede salir de los límites del mapa.
 * Si intenta hacerlo, se reposiciona en el borde.
 */
public class BoundaryRule implements IGameRule {

    @Override
    public void apply(GameEngine engine) {
        double maxX = engine.getMapWidth();
        double maxY = engine.getMapHeight();

        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            double r = player.getRadius();

            // Clamp X
            if (player.getX() - r < 0) {
                player.setX(r);
            } else if (player.getX() + r > maxX) {
                player.setX(maxX - r);
            }

            // Clamp Y
            if (player.getY() - r < 0) {
                player.setY(r);
            } else if (player.getY() + r > maxY) {
                player.setY(maxY - r);
            }
        }
    }
}

