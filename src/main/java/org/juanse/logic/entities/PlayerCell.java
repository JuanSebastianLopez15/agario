package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Representa la célula de un jugador en el juego.
 * Puede crecer, encogerse y dividirse.
 */
public class PlayerCell extends GameEntity {

    private double targetX;
    private double targetY;

    private final String cellId;       // ID único de esta célula
    private final String ownerName;    // Nombre del jugador dueño
    private boolean alive;

    // Tiempo mínimo (ms) antes de que dos células del mismo jugador puedan volver a fusionarse
    private static final long MERGE_COOLDOWN_MS = 15000;
    private long splitTimestamp;

    // Variables para la aceleración (Dash)
    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN_MS = 10000; // 15 segundos
    private static final long DASH_DURATION_MS = 4000;  // 2 segundos de aceleración

    public PlayerCell(String ownerName, double x, double y, double initialMass) {
        super(x, y, initialMass);
        this.ownerName = ownerName;
        this.cellId = UUID.randomUUID().toString();
        this.alive = true;
        this.splitTimestamp = 0;
        this.targetX = x;
        this.targetY = y;
    }

    /**
     * Crea una célula hija al dividirse, con la mitad de la masa.
     */
    public PlayerCell splitInto() {
        double halfMass = this.mass / 2.0;
        setMass(halfMass);
        this.splitTimestamp = System.currentTimeMillis();

        PlayerCell newCell = new PlayerCell(ownerName, x + radius + 2, y, halfMass);
        newCell.splitTimestamp = System.currentTimeMillis();
        return newCell;
    }

    /**
     * Devuelve true si esta célula puede fusionarse con otra del mismo jugador.
     */
    public boolean canMergeWith(PlayerCell other) {
        if (!this.ownerName.equals(other.ownerName)) return false;
        long now = System.currentTimeMillis();
        return (now - splitTimestamp >= MERGE_COOLDOWN_MS)
                && (now - other.splitTimestamp >= MERGE_COOLDOWN_MS);
    }

    public void kill() {
        this.alive = false;
    }

    /**
     * Intenta activar la habilidad de aceleración si el cooldown lo permite.
     */
    public void tryDash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime >= DASH_COOLDOWN_MS) {
            lastDashTime = now;
        }
    }

    /**
     * Indica si la célula está actualmente bajo el efecto de aceleración.
     */
    public boolean isDashing() {
        return (System.currentTimeMillis() - lastDashTime) < DASH_DURATION_MS;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getCellId() { return cellId; }
    public String getOwnerName() { return ownerName; }
    public boolean isAlive() { return alive; }
    public long getSplitTimestamp() { return splitTimestamp; }

    public void setTarget(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }

    @Override
    public String toString() {
        return "PlayerCell[owner=" + ownerName + ", id=" + cellId.substring(0, 6)
                + ", mass=" + String.format("%.1f", mass) + "]";
    }
}