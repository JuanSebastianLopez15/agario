package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

/**
 * Regla de movimiento: mueve cada célula hacia el target (mouse).
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
                double speed = SPEED / (1 + player.getMass() * 0.01);
                player.setX(player.getX() + (dx / distance) * speed);
                player.setY(player.getY() + (dy / distance) * speed);
            }
        }
    }
}

