package org.juanse.logic.observers;

import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

/**
 * Interfaz del patron Observer para eventos del juego.
 *
 * <p>Cualquier componente (UI, red, sonido, puntajes) que quiera reaccionar
 * a eventos de la logica debe implementar esta interfaz.</p>
 *
 * <p>Principio D (DIP): la logica nunca depende de quien escucha,
 * solo de esta interfaz.</p>
 * <p>Principio I (ISP): se puede extender con mas metodos si se necesitan
 * sin romper los oyentes existentes usando metodos default.</p>
 */
public interface IGameEventListener {

    /**
     * Se dispara cuando una celula absorbe a otra.
     *
     * @param absorber celula que realizo la absorcion
     * @param absorbed celula que fue absorbida
     */
    void onAbsorption(PlayerCell absorber, PlayerCell absorbed);

    /**
     * Se dispara cuando una celula choca con una HazardBall y se divide.
     *
     * @param original celula original que se dividio
     * @param newCell  nueva celula generada por la division
     */
    void onSplit(PlayerCell original, PlayerCell newCell);

    /**
     * Se dispara cuando una celula come un pellet.
     *
     * @param player celula que comio el pellet
     * @param pellet pellet que fue consumido
     */
    void onPelletEaten(PlayerCell player, Pellet pellet);

    /**
     * Se dispara cuando el juego termina.
     *
     * @param winnerName nombre del jugador ganador
     * @param reason     razon de la victoria
     */
    void onGameOver(String winnerName, String reason);
}