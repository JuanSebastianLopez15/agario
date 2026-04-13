package org.juanse.logic.entities;

import java.util.UUID;

/**
 * Punto de comida estatico en el mapa que una celula puede absorber
 * para aumentar su masa en una cantidad fija.
 *
 * <p>Principio S (SRP): solo representa el estado de un Pellet.</p>
 * <p>Principio L (LSP): puede usarse donde se espere GameEntity.</p>
 */
public class Pellet extends GameEntity {

    /**
     * Masa fija de cada Pellet.
     * Radio resultante: sqrt(5) * 4 = aprox 8.9px.
     */
    public static final double DEFAULT_MASS = 5.0;

    /** Identificador unico del Pellet. */
    private final String pelletId;

    /** Indica si el Pellet ya fue consumido por una celula. */
    private boolean consumed;

    /**
     * Constructor del Pellet.
     * Se posiciona en el mapa con masa fija y genera un ID unico.
     *
     * @param x coordenada X en el mapa
     * @param y coordenada Y en el mapa
     */
    public Pellet(double x, double y) {
        super(x, y, DEFAULT_MASS);
        this.pelletId = UUID.randomUUID().toString();
        this.consumed = false;
    }

    /**
     * Marca el Pellet como consumido.
     * Debe llamarse cuando una celula lo absorbe.
     */
    public void consume() {
        this.consumed = true;
    }

    /**
     * Indica si el Pellet ya fue consumido.
     *
     * @return true si ya fue absorbido por una celula
     */
    public boolean isConsumed() { return consumed; }

    /**
     * Retorna el identificador unico del Pellet.
     *
     * @return id del Pellet
     */
    public String getPelletId() { return pelletId; }
}