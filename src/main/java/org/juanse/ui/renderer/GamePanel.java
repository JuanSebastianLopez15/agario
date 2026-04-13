package org.juanse.ui.renderer;

import org.juanse.logic.engine.GameEngine;

import javax.swing.*;
import java.awt.*;

/**
 * Canvas principal del juego.
 * Se encarga de dibujar todos los elementos del juego en pantalla
 * usando Graphics2D con suavizado activado.
 *
 * <p>Principio S (SRP): solo dibuja, no maneja logica del juego.</p>
 */
public class GamePanel extends JPanel {

    /** Motor del juego del que se leen los elementos a dibujar. */
    private GameEngine engine;

    /** Nombre del jugador local para resaltarlo visualmente. */
    private String localPlayerName;

    /** Renderer encargado de dibujar las celulas de los jugadores. */
    private final PlayerRenderer playerRenderer = new PlayerRenderer();

    /** Renderer encargado de dibujar los pellets del mapa. */
    private final PelletRenderer pelletRenderer = new PelletRenderer();

    /** Renderer encargado de dibujar las HazardBalls del mapa. */
    private final HazardRenderer hazardRenderer = new HazardRenderer();

    /**
     * Constructor del canvas principal.
     *
     * @param engine          motor del juego
     * @param localPlayerName nombre del jugador local
     */
    public GamePanel(GameEngine engine, String localPlayerName) {
        this.engine          = engine;
        this.localPlayerName = localPlayerName;
        setPreferredSize(new Dimension(1200, 800));
        setBackground(new Color(20, 20, 40));
    }

    /**
     * Dibuja todos los elementos del juego en cada repintado.
     * Activa el suavizado de bordes para mejor calidad visual.
     * Dibuja en orden: pellets, HazardBalls y celulas de jugadores.
     *
     * @param g contexto grafico de Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (engine == null) return;

        pelletRenderer.render(g2, engine.getPellets());
        hazardRenderer.render(g2, engine.getHazardBalls());
        playerRenderer.render(g2, engine.getPlayers(), localPlayerName);
    }

    /**
     * Actualiza el motor del juego usado para obtener los elementos a dibujar.
     *
     * @param engine nuevo motor del juego
     */
    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}