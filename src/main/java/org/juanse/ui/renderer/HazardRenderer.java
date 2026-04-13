package org.juanse.ui.renderer;

import org.juanse.logic.entities.HazardBall;

import java.awt.*;
import java.util.List;

/**
 * Renderer encargado de dibujar las HazardBalls del mapa.
 * Dibuja cada bola con cuerpo rojo oscuro, borde rojo brillante
 * y el simbolo "!" en el centro para indicar peligro.
 *
 * <p>Principio S (SRP): solo dibuja HazardBalls, nada mas.</p>
 */
public class HazardRenderer {

    /** Color del cuerpo de la HazardBall. */
    private static final Color HAZARD_COLOR = new Color(139, 0, 0);

    /** Color del borde brillante de la HazardBall. */
    private static final Color BORDER_COLOR = new Color(255, 68, 68);

    /**
     * Dibuja todas las HazardBalls activas en el mapa.
     * Cada HazardBall se dibuja con cuerpo, borde y simbolo de peligro.
     *
     * @param g       contexto grafico de Swing
     * @param hazards lista de HazardBalls a dibujar
     */
    public void render(Graphics2D g, List<HazardBall> hazards) {
        for (HazardBall hazard : hazards) {
            int x      = (int) hazard.getX();
            int y      = (int) hazard.getY();
            int radius = (int) hazard.getRadius();

            // Cuerpo rojo oscuro
            g.setColor(HAZARD_COLOR);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Borde rojo brillante
            g.setColor(BORDER_COLOR);
            g.setStroke(new BasicStroke(3));
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            // Simbolo de peligro centrado
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            String symbol = "!";
            int textX = x - fm.stringWidth(symbol) / 2;
            g.drawString(symbol, textX, y + 5);
        }
    }
}