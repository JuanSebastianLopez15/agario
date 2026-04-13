package org.juanse.ui.renderer;

import org.juanse.logic.entities.Pellet;

import java.awt.*;
import java.util.List;

/**
 * Renderer encargado de dibujar los pellets del mapa.
 * Cada pellet se dibuja como un circulo de color usando
 * una paleta de 5 colores que se repite ciclicamente.
 *
 * <p>Principio S (SRP): solo dibuja pellets, nada mas.</p>
 */
public class PelletRenderer {

    /**
     * Paleta de colores usada para dibujar los pellets.
     * Se asigna ciclicamente segun el indice del pellet en la lista.
     */
    private static final Color[] PELLET_COLORS = {
            new Color(255, 215, 0),   // dorado
            new Color(144, 238, 144), // verde claro
            new Color(255, 182, 193), // rosado
            new Color(173, 216, 230), // azul claro
            new Color(255, 160, 122)  // salmon
    };

    /**
     * Dibuja todos los pellets activos en el mapa.
     * Los pellets ya consumidos no se dibujan.
     *
     * @param g       contexto grafico de Swing
     * @param pellets lista de pellets a dibujar
     */
    public void render(Graphics2D g, List<Pellet> pellets) {
        for (int i = 0; i < pellets.size(); i++) {
            Pellet pellet = pellets.get(i);
            if (pellet.isConsumed()) continue;

            int x      = (int) pellet.getX();
            int y      = (int) pellet.getY();
            int radius = (int) pellet.getRadius();

            Color color = PELLET_COLORS[i % PELLET_COLORS.length];

            g.setColor(color);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}