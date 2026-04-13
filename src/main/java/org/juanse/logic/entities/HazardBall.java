package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Bola de peligro estatica en el mapa.
 * No puede ser comida por ninguna celula.
 * Si una celula colisiona con ella y tiene masa suficiente (mayor a 20),
 * se divide en dos partes iguales.
 *
 * <p>Principio S (SRP): solo representa el estado de una HazardBall.</p>
 * <p>Principio L (LSP): puede usarse donde se espere GameEntity.</p>
 */
public class HazardBall extends GameEntity {

    /**
     * Masa fija de la HazardBall.
     * Siempre es grande para que nunca pueda ser absorbida por una celula.
     * Radio resultante: sqrt(100) * 4 = 40px.
     */
    public static final double HAZARD_MASS = 100.0;

    /** Identificador unico de la HazardBall. */
    private final String hazardId;

    /**
     * Constructor de la HazardBall.
     * Se posiciona en el mapa con masa fija y genera un ID unico.
     *
     * @param x coordenada X en el mapa
     * @param y coordenada Y en el mapa
     */
    public HazardBall(double x, double y) {
        super(x, y, HAZARD_MASS);
        this.hazardId = UUID.randomUUID().toString();
    }

    /**
     * Retorna el identificador unico de la HazardBall.
     *
     * @return id de la HazardBall
     */
    public String getHazardId() { return hazardId; }
}