package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;

/**
 * Contrato que debe cumplir cada regla del juego.
 *
 * Patrón Strategy: cada regla es una estrategia intercambiable.
 * Principio O (OCP): para añadir una regla nueva, creas una clase nueva;
 *                    el GameEngine no necesita modificarse.
 * Principio D (DIP): el GameEngine depende de esta interfaz, no de implementaciones concretas.
 */
public interface IGameRule {

    /**
     * Aplica la regla sobre el estado actual del motor de juego.
     * Se llama en cada tick del ciclo de actualización.
     *
     * @param engine el motor de juego con acceso al estado completo
     */
    void apply(GameEngine engine);
}
