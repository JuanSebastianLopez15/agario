package org.juanse.logic.dto;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) que representa el estado de una célula
 * de jugador en un instante de tiempo para ser transmitido por UDP.
 *
 * <p>Implementa {@link Serializable} para convertirse en bytes
 * y ser transportado a través de sockets UDP.</p>
 * <p>Principio S (SRP): solo transporta datos de una célula de jugador.</p>
 */
public class PlayerDTO implements Serializable {

    /** Identificador único de la célula. */
    public String id;

    /** Nombre del jugador dueño de la célula. */
    public String owner;

    /** Coordenada X de la célula en el mapa. */
    public double x;

    /** Coordenada Y de la célula en el mapa. */
    public double y;

    /** Masa actual de la célula. */
    public double mass;

    /**
     * Constructor con todos los campos.
     *
     * @param id    identificador único de la célula
     * @param owner nombre del jugador dueño
     * @param x     coordenada X en el mapa
     * @param y     coordenada Y en el mapa
     * @param mass  masa actual de la célula
     */
    public PlayerDTO(String id, String owner, double x, double y, double mass) {
        this.id    = id;
        this.owner = owner;
        this.x     = x;
        this.y     = y;
        this.mass  = mass;
    }

    /**
     * Constructor vacío requerido para deserialización.
     */
    public PlayerDTO() {}

    /**
     * Retorna el identificador único de la célula.
     *
     * @return id de la célula
     */
    public String getId() { return id; }

    /**
     * Establece el identificador único de la célula.
     *
     * @param id nuevo id
     */
    public void setId(String id) { this.id = id; }

    /**
     * Retorna el nombre del jugador dueño de la célula.
     *
     * @return nombre del jugador
     */
    public String getOwner() { return owner; }

    /**
     * Establece el nombre del jugador dueño de la célula.
     *
     * @param owner nombre del jugador
     */
    public void setOwner(String owner) { this.owner = owner; }

    /**
     * Retorna la coordenada X de la célula.
     *
     * @return coordenada X
     */
    public double getX() { return x; }

    /**
     * Establece la coordenada X de la célula.
     *
     * @param x nueva coordenada X
     */
    public void setX(double x) { this.x = x; }

    /**
     * Retorna la coordenada Y de la célula.
     *
     * @return coordenada Y
     */
    public double getY() { return y; }

    /**
     * Establece la coordenada Y de la célula.
     *
     * @param y nueva coordenada Y
     */
    public void setY(double y) { this.y = y; }

    /**
     * Retorna la masa actual de la célula.
     *
     * @return masa de la célula
     */
    public double getMass() { return mass; }

    /**
     * Establece la masa actual de la célula.
     *
     * @param mass nueva masa
     */
    public void setMass(double mass) { this.mass = mass; }
}