package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

/**
 * Regla de zona de juego (paredes invisibles).
 * Ninguna celula puede salir de los limites del mapa.
 * Si intenta hacerlo, se reposiciona en el borde mas cercano.
 *
 * <p>Principio S (SRP): solo aplica la logica de limites del mapa.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class BoundaryRule implements IGameRule {

    /**
     * Aplica la regla de limites en cada tick del juego.
     * Recorre todas las celulas vivas y las reposiciona si salen del mapa,
     * teniendo en cuenta su radio para que no queden parcialmente fuera.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void apply(GameEngine engine) {
        double maxX = engine.getMapWidth();
        double maxY = engine.getMapHeight();

        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            double r = player.getRadius();

            // Limitar en el eje X
            if (player.getX() - r < 0) {
                player.setX(r);
            } else if (player.getX() + r > maxX) {
                player.setX(maxX - r);
            }

            // Limitar en el eje Y
            if (player.getY() - r < 0) {
                player.setY(r);
            } else if (player.getY() + r > maxY) {
                player.setY(maxY - r);
            }
        }
    }
}