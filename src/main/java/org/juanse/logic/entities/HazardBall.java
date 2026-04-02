package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Bola de peligro: no puede ser comida.
 * Si una célula colisiona con ella, se divide en dos partes iguales.
 * Permanece fija en el mapa.
 */
public class HazardBall extends GameEntity {

    public static final double HAZARD_MASS = 100.0; // siempre grande, nunca comible

    private final String hazardId;

    public HazardBall(double x, double y) {
        super(x, y, HAZARD_MASS);
        this.hazardId = UUID.randomUUID().toString();
    }

    public String getHazardId() { return hazardId; }
}

