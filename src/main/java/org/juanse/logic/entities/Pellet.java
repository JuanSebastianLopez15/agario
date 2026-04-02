package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Pellet: punto de comida estático que el jugador puede absorber
 * para aumentar su masa en una cantidad fija.
 */
public class Pellet extends GameEntity {

    public static final double DEFAULT_MASS = 5.0;

    private final String pelletId;
    private boolean consumed;

    public Pellet(double x, double y) {
        super(x, y, DEFAULT_MASS);
        this.pelletId = UUID.randomUUID().toString();
        this.consumed = false;
    }

    public void consume() {
        this.consumed = true;
    }

    public boolean isConsumed() { return consumed; }
    public String getPelletId() { return pelletId; }
}

