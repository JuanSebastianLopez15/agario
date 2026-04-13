package org.juanse.logic.entities;

/**
 * Clase base abstracta para todas las entidades del juego.
 *
 * <p>Principio S (SRP): solo representa propiedades fisicas y posicion.</p>
 * <p>Principio L (LSP): PlayerCell, Pellet y HazardBall pueden usarse
 * donde se espere GameEntity.</p>
 */
public abstract class GameEntity {

    /** Coordenada X de la entidad en el mapa. */
    protected double x;

    /** Coordenada Y de la entidad en el mapa. */
    protected double y;

    /** Masa actual de la entidad. */
    protected double mass;

    /** Radio de la entidad calculado a partir de la masa. */
    protected double radius;

    /**
     * Constructor de la entidad del juego.
     *
     * @param x    coordenada X inicial
     * @param y    coordenada Y inicial
     * @param mass masa inicial de la entidad
     */
    public GameEntity(double x, double y, double mass) {
        this.x      = x;
        this.y      = y;
        this.mass   = mass;
        this.radius = computeRadius(mass);
    }

    /**
     * Calcula el radio de la entidad a partir de su masa.
     * El radio crece con la raiz cuadrada de la masa, igual que en Agar.io.
     *
     * @param mass masa de la entidad
     * @return radio calculado
     */
    protected double computeRadius(double mass) {
        return Math.sqrt(mass) * 4.0;
    }

    /**
     * Calcula la distancia euclidiana entre esta entidad y otra.
     *
     * @param other otra entidad del juego
     * @return distancia en pixeles entre los centros de ambas entidades
     */
    public double distanceTo(GameEntity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Detecta colision entre esta entidad y otra basandose en la suma de radios.
     *
     * @param other otra entidad del juego
     * @return true si las entidades se superponen
     */
    public boolean collidesWith(GameEntity other) {
        return distanceTo(other) < (this.radius + other.radius);
    }

    // ── Getters ──────────────────────────────────────────────

    /**
     * Retorna la coordenada X de la entidad.
     *
     * @return coordenada X
     */
    public double getX() { return x; }

    /**
     * Retorna la coordenada Y de la entidad.
     *
     * @return coordenada Y
     */
    public double getY() { return y; }

    /**
     * Retorna la masa actual de la entidad.
     *
     * @return masa
     */
    public double getMass() { return mass; }

    /**
     * Retorna el radio actual de la entidad.
     *
     * @return radio
     */
    public double getRadius() { return radius; }

    // ── Setters ──────────────────────────────────────────────

    /**
     * Establece la coordenada X de la entidad.
     *
     * @param x nueva coordenada X
     */
    public void setX(double x) { this.x = x; }

    /**
     * Establece la coordenada Y de la entidad.
     *
     * @param y nueva coordenada Y
     */
    public void setY(double y) { this.y = y; }

    /**
     * Establece la masa de la entidad y recalcula su radio.
     * La masa minima es 0.
     *
     * @param mass nueva masa
     */
    public void setMass(double mass) {
        this.mass   = Math.max(mass, 0);
        this.radius = computeRadius(this.mass);
    }

    /**
     * Incrementa la masa de la entidad en una cantidad dada y recalcula el radio.
     *
     * @param amount cantidad de masa a agregar
     */
    public void growBy(double amount) {
        setMass(this.mass + amount);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[x=" + x + ", y=" + y + ", mass=" + mass + "]";
    }
}