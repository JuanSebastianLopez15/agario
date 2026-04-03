package org.juanse.ui.renderer;

import org.juanse.logic.entities.Pellet;

import java.awt.*;
import java.util.List;

/**
 * Se encarga de dibujar los pellets del mapa.
 * Principio S (SRP): solo dibuja pellets, nada más.
 */
public class PelletRenderer {

    private static final Color[] PELLET_COLORS = {
            new Color(255, 215, 0),    // dorado
            new Color(144, 238, 144),  // verde claro
            new Color(255, 182, 193),  // rosado
            new Color(173, 216, 230),  // azul claro
            new Color(255, 160, 122)   // salmón
    };

    private int colorIndex = 0;

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
