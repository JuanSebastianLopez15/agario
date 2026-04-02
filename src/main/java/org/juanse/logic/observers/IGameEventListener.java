package org.juanse.logic.observers;

import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

/**
 * Interfaz del patrón Observer.
 *
 * Cualquier componente (UI, red, sonido, scores) que quiera reaccionar
 * a eventos de la lógica debe implementar esta interfaz.
 *
 * Principio D (DIP): la lógica nunca depende de quien escucha.
 * Principio I (ISP): se puede extender con más métodos si se necesitan
 *                    sin romper los oyentes existentes (usando default).
 */
public interface IGameEventListener {

    /** Se dispara cuando una célula absorbe a otra. */
    void onAbsorption(PlayerCell absorber, PlayerCell absorbed);

    /** Se dispara cuando una célula choca con una HazardBall y se divide. */
    void onSplit(PlayerCell original, PlayerCell newCell);

    /** Se dispara cuando una célula come un pellet. */
    void onPelletEaten(PlayerCell player, Pellet pellet);

    /** Se dispara cuando el juego termina. */
    void onGameOver(String winnerName, String reason);
}
