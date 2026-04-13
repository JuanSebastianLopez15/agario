package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

/**
 * Regla de movimiento hacia el objetivo (mouse).
 * Mueve cada celula en direccion al target aplicando velocidad base
 * o velocidad de dash si el jugador activo el potenciador.
 *
 * <p>El dash escala con el tamano de la celula: entre mas grande,
 * mayor es el impulso de velocidad.</p>
 * <p>Principio S (SRP): solo aplica la logica de movimiento.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class MovementRule implements IGameRule {

    /**
     * Velocidad base de movimiento.
     * Se reduce conforme la celula crece para equilibrar el juego.
     */
    private static final double SPEED = 5.0;

    /**
     * Aplica la regla de movimiento en cada tick del juego.
     * Calcula la direccion hacia el target y mueve la celula
     * a velocidad normal o de dash segun corresponda.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void apply(GameEngine engine) {
        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            double dx       = player.getTargetX() - player.getX();
            double dy       = player.getTargetY() - player.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 1) {
                // Velocidad base — se reduce conforme la celula crece
                double baseSpeed    = SPEED / (1 + player.getMass() * 0.01);
                double currentSpeed = baseSpeed;

                if (player.isDashing()) {
                    // Multiplicador escala con la masa de la celula:
                    // Masa  50 (inicial) -> 2.0 + 0.5 = 2.5x
                    // Masa 300 (grande)  -> 2.0 + 3.0 = 5.0x
                    double dashMultiplier = 2.0 + (player.getMass() * 0.01);

                    // Limite maximo para evitar velocidades absurdas
                    dashMultiplier = Math.min(dashMultiplier, 7.0);

                    currentSpeed = baseSpeed * dashMultiplier;
                }

                player.setX(player.getX() + (dx / distance) * currentSpeed);
                player.setY(player.getY() + (dy / distance) * currentSpeed);
            }
        }
    }
}