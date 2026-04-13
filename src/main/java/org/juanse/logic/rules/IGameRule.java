package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;

/**
 * Contrato que debe cumplir cada regla del juego.
 *
 * <p>Patron Strategy: cada regla es una estrategia intercambiable
 * que puede agregarse o removerse sin modificar el motor.</p>
 * <p>Principio O (OCP): para agregar una regla nueva se crea una clase nueva;
 * el {@link GameEngine} no necesita modificarse.</p>
 * <p>Principio D (DIP): el {@link GameEngine} depende de esta interfaz,
 * no de implementaciones concretas.</p>
 */
public interface IGameRule {

    /**
     * Aplica la regla sobre el estado actual del motor de juego.
     * Se llama en cada tick del ciclo de actualizacion.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    void apply(GameEngine engine);
}