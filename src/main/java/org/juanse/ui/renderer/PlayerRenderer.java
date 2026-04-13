package org.juanse.ui.renderer;

import org.juanse.logic.entities.PlayerCell;

import java.awt.*;
import java.util.List;

/**
 * Se encarga de dibujar las células de los jugadores.
 * Principio S (SRP): solo dibuja jugadores, nada más.
 */
public class PlayerRenderer {

    private static final Color[] PLAYER_COLORS = {
            new Color(100, 149, 237),
            new Color(255, 99, 71),
            new Color(50, 205, 50)
    };

    public void render(Graphics2D g, List<PlayerCell> players, String localPlayerName) {
        for (int i = 0; i < players.size(); i++) {
            PlayerCell cell = players.get(i);
            if (!cell.isAlive()) continue;

            int x      = (int) cell.getX();
            int y      = (int) cell.getY();
            int radius = (int) cell.getRadius();

            Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];

            // Borde brillante si está en dash
            if (cell.isDashing()) {
                g.setColor(new Color(255, 255, 0));
                g.setStroke(new BasicStroke(4));
                g.drawOval(x - radius - 3, y - radius - 3, (radius + 3) * 2, (radius + 3) * 2);
            }

            // Sombra
            g.setColor(color.darker());
            g.fillOval(x - radius + 3, y - radius + 3, radius * 2, radius * 2);

            // Cuerpo
            g.setColor(color);
            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Borde
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

            // Rayo dentro de la célula si está en dash
            if (cell.isDashing()) {
                drawLightning(g, x, y, radius);
            }

            // Nombre
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int textX = x - fm.stringWidth(cell.getOwnerName()) / 2;
            g.drawString(cell.getOwnerName(), textX, y + 5);
        }
    }

    /**
     * Dibuja un rayo dentro de la célula cuando está en modo dash.
     */
    private void drawLightning(Graphics2D g, int cx, int cy, int radius) {
        int size = radius / 2;

        int[] xPoints = {
                cx,
                cx - size / 3,
                cx + size / 5,
                cx - size / 2,
        };
        int[] yPoints = {
                cy - size,
                cy - size / 5,
                cy - size / 5,
                cy + size,
        };

        g.setColor(new Color(255, 230, 0));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(xPoints, yPoints, 4);

        g.setColor(new Color(255, 255, 180, 150));
        g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(xPoints, yPoints, 4);
    }
}