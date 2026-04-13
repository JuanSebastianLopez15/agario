package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla de crecimiento por pellets.
 * Cuando una celula colisiona con un pellet, lo consume y aumenta su masa
 * en la cantidad fija de masa del pellet.
 * Luego se genera un nuevo pellet para mantener la densidad del mapa.
 *
 * <p>Principio S (SRP): solo aplica la logica de crecimiento por pellets.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class GrowthRule implements IGameRule {

    /**
     * Aplica la regla de crecimiento en cada tick del juego.
     * Recorre todas las celulas vivas y verifica si colisionan con pellets.
     * Por cada pellet consumido se genera uno nuevo en posicion aleatoria.
     *
     * @param engine motor del juego con acceso al estado completo
     */
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