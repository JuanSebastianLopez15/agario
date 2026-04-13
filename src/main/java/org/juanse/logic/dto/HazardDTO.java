package org.juanse.logic.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) que representa el estado de una HazardBall
 * en un instante de tiempo para ser transmitido por UDP.
 *
 * <p>Implementa {@link Serializable} para convertirse en bytes
 * y ser transportado a través de sockets UDP.</p>
 * <p>Principio S (SRP): solo transporta datos de una HazardBall.</p>
 */
public class HazardDTO implements Serializable {

    /** Identificador único de la HazardBall. */
    public String id;

    /** Coordenada X de la HazardBall en el mapa. */
    public double x;

    /** Coordenada Y de la HazardBall en el mapa. */
    public double y;

    /**
     * Constructor con todos los campos.
     *
     * @param id identificador único de la HazardBall
     * @param x  coordenada X en el mapa
     * @param y  coordenada Y en el mapa
     */
    public HazardDTO(String id, double x, double y) {
        this.id = id;
        this.x  = x;
        this.y  = y;
    }

    /**
     * Constructor vacío requerido para deserialización.
     */
    public HazardDTO() {}

    /**
     * Retorna el identificador único de la HazardBall.
     *
     * @return id de la HazardBall
     */
    public String getId() { return id; }

    /**
     * Establece el identificador único de la HazardBall.
     *
     * @param id nuevo id
     */
    public void setId(String id) { this.id = id; }

    /**
     * Retorna la coordenada X de la HazardBall.
     *
     * @return coordenada X
     */
    public double getX() { return x; }

    /**
     * Establece la coordenada X de la HazardBall.
     *
     * @param x nueva coordenada X
     */
    public void setX(double x) { this.x = x; }

    /**
     * Retorna la coordenada Y de la HazardBall.
     *
     * @return coordenada Y
     */
    public double getY() { return y; }

    /**
     * Establece la coordenada Y de la HazardBall.
     *
     * @param y nueva coordenada Y
     */
    public void setY(double y) { this.y = y; }
}