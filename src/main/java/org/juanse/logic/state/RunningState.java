package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.rules.IGameRule;

/**
 * Estado activo del juego.
 * En cada tick aplica todas las reglas registradas en el motor
 * en el orden en que fueron agregadas.
 *
 * <p>Patron State: representa el estado de juego en curso.</p>
 * <p>Principio S (SRP): solo se encarga de ejecutar las reglas en cada tick.</p>
 * <p>Principio O (OCP): nuevas reglas pueden agregarse sin modificar este estado.</p>
 */
public class RunningState implements IGameState {

    /**
     * Aplica todas las reglas del juego en cada tick.
     * Las reglas se ejecutan en el orden en que fueron registradas en el motor.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void update(GameEngine engine) {
        for (IGameRule rule : engine.getRules()) {
            rule.apply(engine);
        }
    }

    /**
     * Indica que el juego esta activo y en curso.
     *
     * @return true siempre
     */
    @Override
    public boolean isRunning() { return true; }

    /**
     * Retorna el nombre descriptivo de este estado.
     *
     * @return "EN JUEGO"
     */
    @Override
    public String getStateName() { return "EN JUEGO"; }
}