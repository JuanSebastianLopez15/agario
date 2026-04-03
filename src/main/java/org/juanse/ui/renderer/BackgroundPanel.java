package org.juanse.ui.renderer;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Panel de fondo animado para la pantalla de inicio y final.
 * Dibuja células vacías, pellets y HazardBalls decorativas sobre
 * una cuadrícula oscura estilo Agar.io.
 *
 * <p>Principio S (SRP): solo se encarga de dibujar el fondo decorativo.</p>
 * <p>Principio O (OCP): puede extenderse para agregar más elementos sin modificar.</p>
 */
public class BackgroundPanel extends JPanel {

    /** Colores disponibles para los pellets decorativos. */
    private static final Color[] PELLET_COLORS = {
            new Color(255, 215, 0),
            new Color(144, 238, 144),
            new Color(255, 182, 193),
            new Color(173, 216, 230),
            new Color(255, 160, 122)
    };

    /** Colores disponibles para las células decorativas. */
    private static final Color[] CELL_COLORS = {
            new Color(100, 149, 237),
            new Color(255, 99, 71),
            new Color(50, 205, 50),
            new Color(255, 165, 0),
            new Color(147, 112, 219)
    };

    /** Coordenadas X de los pellets. */
    private final int[] pelletX;

    /** Coordenadas Y de los pellets. */
    private final int[] pelletY;

    /** Coordenadas X de las células. */
    private final int[] cellX;

    /** Coordenadas Y de las células. */
    private final int[] cellY;

    /** Radios de las células. */
    private final int[] cellRadius;

    /** Coordenadas X de las HazardBalls. */
    private final int[] hazardX;

    /** Coordenadas Y de las HazardBalls. */
    private final int[] hazardY;

    /** Colores asignados a cada célula. */
    private final Color[] cellColors;

    /**
     * Constructor del panel de fondo.
     * Genera posiciones fijas para pellets, células y HazardBalls decorativas.
     */
    public BackgroundPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 40));

        Random rand = new Random(42);

        pelletX = new int[50];
        pelletY = new int[50];
        for (int i = 0; i < 50; i++) {
            pelletX[i] = rand.nextInt(1200);
            pelletY[i] = rand.nextInt(800);
        }

        cellX      = new int[]{150, 400, 650, 900, 1050, 300, 750};
        cellY      = new int[]{200, 350, 150, 400, 250, 550, 500};
        cellRadius = new int[]{60, 40, 50, 35, 45, 30, 55};
        cellColors = new Color[]{
                CELL_COLORS[0], CELL_COLORS[1], CELL_COLORS[2],
                CELL_COLORS[3], CELL_COLORS[4], CELL_COLORS[0], CELL_COLORS[1]
        };

        hazardX = new int[]{250, 700, 1000, 500};
        hazardY = new int[]{100, 450, 150, 650};
    }

    /**
     * Dibuja el fondo del panel con cuadrícula, pellets, HazardBalls y células vacías.
     *
     * @param g contexto gráfico de Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Cuadrícula de fondo
        g2.setColor(new Color(26, 26, 58));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < w; x += 50) g2.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 50) g2.drawLine(0, y, w, y);

        // Pellets decorativos
        for (int i = 0; i < 50; i++) {
            int px = pelletX[i] * w / 1200;
            int py = pelletY[i] * h / 800;
            g2.setColor(PELLET_COLORS[i % PELLET_COLORS.length]);
            g2.fillOval(px - 5, py - 5, 10, 10);
        }

        // HazardBalls decorativas
        for (int i = 0; i < hazardX.length; i++) {
            int hx = hazardX[i] * w / 1200;
            int hy = hazardY[i] * h / 800;
            g2.setColor(new Color(139, 0, 0));
            g2.fillOval(hx - 35, hy - 35, 70, 70);
            g2.setColor(new Color(255, 68, 68));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(hx - 35, hy - 35, 70, 70);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("!", hx - 5, hy + 7);
        }

        // Células vacías decorativas
        for (int i = 0; i < cellX.length; i++) {
            int cx = cellX[i] * w / 1200;
            int cy = cellY[i] * h / 800;
            int r  = cellRadius[i];

            g2.setColor(cellColors[i].darker());
            g2.fillOval(cx - r + 3, cy - r + 3, r * 2, r * 2);

            g2.setColor(cellColors[i]);
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
    }
}
