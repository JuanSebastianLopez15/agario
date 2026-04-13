package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.HazardBall;
import org.juanse.logic.entities.PlayerCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Regla de division por bola peligrosa.
 * Si una celula toca una HazardBall y tiene masa suficiente (mayor a 20),
 * se divide en dos partes iguales.
 * La HazardBall no puede ser comida y permanece fija en el mapa.
 *
 * <p>Principio S (SRP): solo aplica la logica de division por HazardBall.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class SplitRule implements IGameRule {

    /**
     * Masa minima requerida para que una celula pueda dividirse.
     * Garantiza que ambas mitades sean viables despues de la division.
     */
    private static final double MIN_MASS_TO_SPLIT = 20.0;

    /**
     * Aplica la regla de division en cada tick del juego.
     * Recorre todas las celulas vivas y verifica si colisionan con alguna HazardBall.
     * Solo se procesa una colision por celula por tick para evitar divisiones multiples.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void apply(GameEngine engine) {
        List<PlayerCell> toAdd = new ArrayList<>();

        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            for (HazardBall hazard : engine.getHazardBalls()) {
                if (player.collidesWith(hazard)) {
                    if (player.getMass() > MIN_MASS_TO_SPLIT) {
                        PlayerCell newCell = player.splitInto();
                        toAdd.add(newCell);
                        engine.notifySplit(player, newCell);
                    }
                    break; // una sola colision por tick
                }
            }
        }

        toAdd.forEach(engine::addPlayer);
    }
}