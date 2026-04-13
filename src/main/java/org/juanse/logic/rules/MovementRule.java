package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

/**
 * Regla de movimiento: mueve cada célula hacia el target (mouse)
 * aplicando multiplicadores de velocidad si está en "Dash".
 * Ahora el Dash escala con el tamaño: entre más grande la célula, más rápido el impulso.
 */
public class MovementRule implements IGameRule {

    private static final double SPEED = 5.0;

    @Override
    public void apply(GameEngine engine) {
        for (PlayerCell player : engine.getPlayers()) {
            if (!player.isAlive()) continue;

            double dx = player.getTargetX() - player.getX();
            double dy = player.getTargetY() - player.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 1) {
                // Velocidad base normal (se hace más lento conforme crece)
                double baseSpeed = SPEED / (1 + player.getMass() * 0.01);

                double currentSpeed = baseSpeed;

                // Si la célula está en modo "Dash", calculamos el multiplicador basado en su masa
                if (player.isDashing()) {
                    // Multiplicador base de 2.0, más un extra que crece con la masa.
                    // Ejemplo: Masa 50 (inicial) -> 2.0 + 0.5 = 2.5x de velocidad
                    // Ejemplo: Masa 300 (grande) -> 2.0 + 3.0 = 5.0x de velocidad
                    double dashMultiplier = 2.0 + (player.getMass() * 0.01);

                    // (Opcional) Ponemos un límite máximo para que la célula no "teletransporte"
                    // o se salga del mapa por ir a una velocidad absurda.
                    dashMultiplier = Math.min(dashMultiplier, 7.0);

                    currentSpeed = baseSpeed * dashMultiplier;
                }

                player.setX(player.getX() + (dx / distance) * currentSpeed);
                player.setY(player.getY() + (dy / distance) * currentSpeed);
            }
        }
    }
}