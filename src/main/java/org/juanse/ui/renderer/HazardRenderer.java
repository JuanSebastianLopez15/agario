package org.juanse.ui.renderer;

import org.juanse.logic.entities.HazardBall;

import java.awt.*;
import java.util.List;

/**
 * Se encarga de dibujar las HazardBalls del mapa.
 * Principio S (SRP): solo dibuja peligros, nada más.
 */
public class HazardRenderer {

    private static final Color HAZARD_COLOR  = new Color(139, 0, 0);
    private static final Color BORDER_COLOR  = new Color(255, 68, 68);

    public void render(Graphics2D g, List<HazardBall> hazards) {
        for (HazardBall hazard : hazards) {
            int x      = (int) hazard.getX();
            int y      = (int) hazard.getY();
            int radius = (int) hazard.getRadius();

            // Cuerpo
            g.setColor(HAZARD_COLOR);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Borde rojo brillante
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(3));
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            // Símbolo de peligro
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            String symbol = "!";
            int textX = x - fm.stringWidth(symbol) / 2;
            g.drawString(symbol, textX, y + 5);
        }
    }
}

