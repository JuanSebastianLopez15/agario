package org.juanse.udp;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) que el cliente envía al host repetidamente
 * para comunicar la posición del mouse y si activó el dash.
 *
 * <p>Se serializa y transmite por UDP en cada tick del juego.</p>
 * <p>Principio S (SRP): solo transporta datos de entrada del jugador.</p>
 */
public class MouseInputDTO implements Serializable {

    /** Nombre del jugador que envía el input. */
    private String playerName;

    /** Coordenada X del mouse en la pantalla del cliente. */
    private double targetX;

    /** Coordenada Y del mouse en la pantalla del cliente. */
    private double targetY;

    /** Indica si el jugador presionó la tecla de dash (Espacio). */
    private boolean dashPressed;

    /**
     * Constructor del DTO de input del mouse.
     *
     * @param playerName  nombre del jugador que envía el input
     * @param targetX     coordenada X del mouse
     * @param targetY     coordenada Y del mouse
     * @param dashPressed true si el jugador presionó la tecla de dash
     */
    public MouseInputDTO(String playerName, double targetX, double targetY, boolean dashPressed) {
        this.playerName  = playerName;
        this.targetX     = targetX;
        this.targetY     = targetY;
        this.dashPressed = dashPressed;
    }

    /**
     * Retorna el nombre del jugador que envió el input.
     *
     * @return nombre del jugador
     */
    public String getPlayerName() { return playerName; }

    /**
     * Retorna la coordenada X del mouse del cliente.
     *
     * @return coordenada X del objetivo
     */
    public double getTargetX() { return targetX; }

    /**
     * Retorna la coordenada Y del mouse del cliente.
     *
     * @return coordenada Y del objetivo
     */
    public double getTargetY() { return targetY; }

    /**
     * Indica si el jugador presionó la tecla de dash en este tick.
     *
     * @return true si el dash fue activado
     */
    public boolean isDashPressed() { return dashPressed; }
}