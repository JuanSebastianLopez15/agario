package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;

/**
 * Interfaz del patrón State.
 * El GameEngine delega el comportamiento de cada tick a su estado actual.
 *
 * Estados posibles: RunningState → GameOverState
 */
public interface IGameState {

    /** Ejecuta la lógica correspondiente a este estado en cada tick. */
    void update(GameEngine engine);

    /** Indica si el juego está activo. */
    boolean isRunning();

    /** Nombre descriptivo del estado (útil para la UI). */
    String getStateName();
}

