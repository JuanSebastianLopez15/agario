package org.juanse.logic.entities;

/**
 * Clase base abstracta para todas las entidades del juego.
 * Principio S (SRP): solo representa propiedades físicas y posición.
 * Principio L (LSP): PlayerCell, Pellet y HazardBall pueden usarse donde se espere GameEntity.
 */
public abstract class GameEntity {

    protected double x;
    protected double y;
    protected double mass;
    protected double radius;

    public GameEntity(double x, double y, double mass) {
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.radius = computeRadius(mass);
    }

    /**
     * El radio crece con la raíz cuadrada de la masa (como en agar.io real).
     */
    protected double computeRadius(double mass) {
        return Math.sqrt(mass) * 4.0;
    }

    /**
     * Calcula la distancia euclidiana entre dos entidades.
     */
    public double distanceTo(GameEntity other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Detecta colisión basada en la suma de radios.
     */
    public boolean collidesWith(GameEntity other) {
        return distanceTo(other) < (this.radius + other.radius);
    }

    // ── Getters ──────────────────────────────────────────────

    public double getX() { return x; }
    public double getY() { return y; }
    public double getMass() { return mass; }
    public double getRadius() { return radius; }

    // ── Setters ──────────────────────────────────────────────

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public void setMass(double mass) {
        this.mass = Math.max(mass, 0);
        this.radius = computeRadius(this.mass);
    }

    public void growBy(double amount) {
        setMass(this.mass + amount);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[x=" + x + ", y=" + y + ", mass=" + mass + "]";
    }
}

