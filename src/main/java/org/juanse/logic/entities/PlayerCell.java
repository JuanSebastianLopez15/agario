package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Representa la célula de un jugador en el juego.
 * Puede crecer, encogerse y dividirse.
 *
 * <p>Principio S (SRP): solo representa el estado y comportamiento de una célula.</p>
 * <p>Principio L (LSP): puede usarse donde se espere GameEntity.</p>
 */
public class PlayerCell extends GameEntity {

    /** Coordenada X del objetivo (posición del mouse). */
    private double targetX;

    /** Coordenada Y del objetivo (posición del mouse). */
    private double targetY;

    /** ID único de esta célula (un jugador puede tener varias tras dividirse). */
    private final String cellId;

    /** Nombre del jugador dueño de esta célula. */
    private final String ownerName;

    /** Indica si la célula está viva. */
    private boolean alive;

    /** Tiempo mínimo en ms antes de que dos células del mismo jugador puedan fusionarse. */
    private static final long MERGE_COOLDOWN_MS = 15000;

    /** Timestamp del último split para controlar la fusión. */
    private long splitTimestamp;

    /** Timestamp del último dash activado. */
    private long lastDashTime = 0;

    /** Tiempo de cooldown en ms antes de poder usar el dash de nuevo. */
    private static final long DASH_COOLDOWN_MS = 10000;

    /** Duración en ms del efecto de aceleración. */
    private static final long DASH_DURATION_MS = 4000;

    /**
     * Constructor de la célula del jugador.
     *
     * @param ownerName   nombre del jugador dueño
     * @param x           posición inicial X
     * @param y           posición inicial Y
     * @param initialMass masa inicial de la célula
     */
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
     * La nueva célula aparece ligeramente desplazada para evitar superposición.
     *
     * @return nueva célula hija con la mitad de la masa
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
     *
     * @param other otra célula del mismo jugador
     * @return true si ambas células pueden fusionarse
     */
    public boolean canMergeWith(PlayerCell other) {
        if (!this.ownerName.equals(other.ownerName)) return false;
        long now = System.currentTimeMillis();
        return (now - splitTimestamp >= MERGE_COOLDOWN_MS)
                && (now - other.splitTimestamp >= MERGE_COOLDOWN_MS);
    }

    /**
     * Marca la célula como muerta.
     */
    public void kill() { this.alive = false; }

    /**
     * Intenta activar la habilidad de aceleración (dash).
     * Solo se activa si han pasado DASH_COOLDOWN_MS + DASH_DURATION_MS
     * desde el último uso, garantizando que el cooldown empiece
     * después de que termine el dash activo.
     */
    public void tryDash() {
        long now = System.currentTimeMillis();
        if (now - lastDashTime >= DASH_COOLDOWN_MS + DASH_DURATION_MS) {
            lastDashTime = now;
        }
    }

    /**
     * Indica si la célula está actualmente bajo el efecto de aceleración.
     *
     * @return true si el dash está activo
     */
    public boolean isDashing() {
        return (System.currentTimeMillis() - lastDashTime) < DASH_DURATION_MS;
    }

    /**
     * Calcula los segundos restantes del cooldown después de que termina el dash.
     * Retorna 0 si ya está disponible.
     *
     * @return segundos restantes del cooldown
     */
    public long getDashCooldownRemaining() {
        long elapsed = System.currentTimeMillis() - lastDashTime;
        long totalWait = DASH_DURATION_MS + DASH_COOLDOWN_MS;
        return Math.max(0, (totalWait - elapsed) / 1000);
    }

    // ── Getters ──────────────────────────────────────────────

    /** @return ID único de la célula */
    public String getCellId()       { return cellId; }

    /** @return nombre del jugador dueño */
    public String getOwnerName()    { return ownerName; }

    /** @return true si la célula está viva */
    public boolean isAlive()        { return alive; }

    /** @return timestamp del último split */
    public long getSplitTimestamp() { return splitTimestamp; }

    /** @return timestamp del último dash */
    public long getLastDashTime()   { return lastDashTime; }

    /**
     * Actualiza el objetivo de movimiento de la célula.
     *
     * @param tx coordenada X del objetivo
     * @param ty coordenada Y del objetivo
     */
    public void setTarget(double tx, double ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    /** @return coordenada X del objetivo */
    public double getTargetX() { return targetX; }

    /** @return coordenada Y del objetivo */
    public double getTargetY() { return targetY; }

    @Override
    public String toString() {
        return "PlayerCell[owner=" + ownerName + ", id=" + cellId.substring(0, 6)
                + ", mass=" + String.format("%.1f", mass) + "]";
    }
}