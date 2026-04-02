package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.rules.IGameRule;

/**
 * Estado activo: el juego está en curso.
 * En cada tick aplica todas las reglas registradas.
 */
public class RunningState implements IGameState {

    @Override
    public void update(GameEngine engine) {
        for (IGameRule rule : engine.getRules()) {
            rule.apply(engine);
        }
    }

    @Override
    public boolean isRunning() { return true; }

    @Override
    public String getStateName() { return "EN JUEGO"; }
}
