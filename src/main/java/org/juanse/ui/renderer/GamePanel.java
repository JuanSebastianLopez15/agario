package org.juanse.ui.renderer;

import org.juanse.logic.engine.GameEngine;
import org.juanse.ui.sound.SoundManager;

import javax.swing.*;
import java.awt.*;

/**
 * Canvas principal del juego.
 * Se encarga de dibujar todos los elementos en pantalla.
 * Principio S (SRP): solo dibuja, no maneja lógica.
 */
public class GamePanel extends JPanel {

    private GameEngine engine;
    private String localPlayerName;

    private final PlayerRenderer playerRenderer = new PlayerRenderer();
    private final PelletRenderer pelletRenderer = new PelletRenderer();
    private final HazardRenderer hazardRenderer = new HazardRenderer();

    public GamePanel(GameEngine engine, String localPlayerName) {
        this.engine          = engine;
        this.localPlayerName = localPlayerName;
        setPreferredSize(new Dimension(1200, 800));
        setBackground(new Color(20, 20, 40));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Suavizado
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (engine == null) return;

        // Dibujar elementos
        pelletRenderer.render(g2, engine.getPellets());
        hazardRenderer.render(g2, engine.getHazardBalls());
        playerRenderer.render(g2, engine.getPlayers(), localPlayerName);
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}
