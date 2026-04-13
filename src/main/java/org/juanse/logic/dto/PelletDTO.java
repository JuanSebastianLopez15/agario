package org.juanse.logic.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) que representa el estado de un Pellet
 * en un instante de tiempo para ser transmitido por UDP.
 *
 * <p>Implementa {@link Serializable} para convertirse en bytes
 * y ser transportado a través de sockets UDP.</p>
 * <p>Principio S (SRP): solo transporta datos de un Pellet.</p>
 */
public class PelletDTO implements Serializable {

    /** Identificador único del Pellet. */
    public String id;

    /** Coordenada X del Pellet en el mapa. */
    public double x;

    /** Coordenada Y del Pellet en el mapa. */
    public double y;

    /**
     * Constructor con todos los campos.
     *
     * @param id identificador único del Pellet
     * @param x  coordenada X en el mapa
     * @param y  coordenada Y en el mapa
     */
    public PelletDTO(String id, double x, double y) {
        this.id = id;
        this.x  = x;
        this.y  = y;
    }

    /**
     * Constructor vacío requerido para deserialización.
     */
    public PelletDTO() {}

    /**
     * Retorna el identificador único del Pellet.
     *
     * @return id del Pellet
     */
    public String getId() { return id; }

    /**
     * Establece el identificador único del Pellet.
     *
     * @param id nuevo id
     */
    public void setId(String id) { this.id = id; }

    /**
     * Retorna la coordenada X del Pellet.
     *
     * @return coordenada X
     */
    public double getX() { return x; }

    /**
     * Establece la coordenada X del Pellet.
     *
     * @param x nueva coordenada X
     */
    public void setX(double x) { this.x = x; }

    /**
     * Retorna la coordenada Y del Pellet.
     *
     * @return coordenada Y
     */
    public double getY() { return y; }

    /**
     * Establece la coordenada Y del Pellet.
     *
     * @param y nueva coordenada Y
     */
    public void setY(double y) { this.y = y; }
}