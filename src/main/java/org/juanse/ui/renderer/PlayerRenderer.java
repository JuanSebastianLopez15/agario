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
            new Color(100, 149, 237),  // azul
            new Color(255, 99, 71),    // rojo
            new Color(50, 205, 50)     // verde
    };

    public void render(Graphics2D g, List<PlayerCell> players, String localPlayerName) {
        for (int i = 0; i < players.size(); i++) {
            PlayerCell cell = players.get(i);
            if (!cell.isAlive()) continue;

            int x      = (int) cell.getX();
            int y      = (int) cell.getY();
            int radius = (int) cell.getRadius();

            Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];

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

            // Nombre encima
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int textX = x - fm.stringWidth(cell.getOwnerName()) / 2;
            g.drawString(cell.getOwnerName(), textX, y + 5);
        }
    }
}