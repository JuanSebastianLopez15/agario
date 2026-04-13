package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;

/**
 * Interfaz del patron State para el ciclo de vida del juego.
 * El {@link GameEngine} delega el comportamiento de cada tick
 * a su estado actual.
 *
 * <p>Estados posibles: {@link RunningState} → {@link GameOverState}</p>
 * <p>Patron State: permite cambiar el comportamiento del motor
 * segun el estado actual sin usar condicionales.</p>
 * <p>Principio O (OCP): nuevos estados pueden agregarse sin modificar el motor.</p>
 */
public interface IGameState {

    /**
     * Ejecuta la logica correspondiente a este estado en cada tick del juego.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    void update(GameEngine engine);

    /**
     * Indica si el juego esta activo y en curso.
     *
     * @return true si el juego esta corriendo, false si termino
     */
    boolean isRunning();

    /**
     * Retorna el nombre descriptivo del estado actual.
     * Util para la UI y para el snapshot enviado por UDP.
     *
     * @return nombre del estado (ej: "EN JUEGO", "FIN DEL JUEGO")
     */
    String getStateName();
}