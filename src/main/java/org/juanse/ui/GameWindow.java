package org.juanse.ui;

import org.juanse.logic.engine.GameEngine;
import org.juanse.ui.screens.StartScreen;
import org.juanse.ui.screens.EndScreen;
import org.juanse.UDP.UDPReceiver;
import org.juanse.UDP.UDPSender;
import org.juanse.UDP.MouseInputDTO;
import org.juanse.logic.engine.GameSnapshot;
import org.juanse.ui.screens.GameScreen;
import org.juanse.ui.renderer.BackgroundPanel;

import javax.swing.*;
import java.util.List;

/**
 * Ventana principal del juego.
 * Coordina las pantallas usando JLayeredPane para efecto overlay.
 *
 * <p>Principio S (SRP): solo coordina pantallas y red.</p>
 * <p>Principio D (DIP): depende de abstracciones, no de implementaciones concretas.</p>
 */
public class GameWindow extends JFrame {

    /** Panel en capas que permite el efecto overlay. */
    private final JLayeredPane layeredPane;

    /** Motor del juego. */
    private GameEngine engine;

    /** Sender UDP para enviar datos al otro jugador. */
    private UDPSender sender;

    /** Receiver UDP para recibir datos del otro jugador. */
    private UDPReceiver receiver;

    /** Nombre del jugador local. */
    private String playerName;

    /** Indica si este jugador es el host. */
    private boolean isHost;

    /** IP del host destino. */
    private String targetIp;

    /** Pantalla principal del juego. */
    private GameScreen gameScreen;

    /** Pantalla final del juego. */
    private EndScreen endScreen;

    /** Panel overlay de inicio. */
    private JPanel startOverlay;

    /** Panel overlay de fin. */
    private JPanel endOverlay;

    /** Panel de fondo decorativo. */
    private BackgroundPanel backgroundPanel;

    /**
     * Constructor de la ventana principal.
     * Inicializa el JFrame y el JLayeredPane.
     */
    public GameWindow() {
        setTitle("Agar.io - Multijugador");
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
    }

    /**
     * Sobrescribe setVisible para inicializar las capas cuando la ventana se muestra.
     *
     * @param visible true para mostrar la ventana
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            setupLayers();
        }
    }

    /**
     * Configura las capas del juego.
     * Capa 0: fondo decorativo.
     * Capa 1: pantalla del juego.
     * Capa 2: overlay de inicio.
     */
    private void setupLayers() {
        int w = getWidth();
        int h = getHeight();

        // Capa 0: fondo decorativo con células y pellets
        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setBounds(0, 0, w, h);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // Capa 1: pantalla del juego (oculta hasta que se le dé jugar)
        engine = new GameEngine(1200, 800);
        gameScreen = new GameScreen(this, engine, "", null, "", 0);
        JPanel gamePanel = gameScreen.getMainPanel();
        gamePanel.setBounds(0, 0, w, h);
        gamePanel.setVisible(false);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

        // Capa 2: overlay de inicio encima del fondo
        StartScreen startScreen = new StartScreen(this);
        startOverlay = startScreen.getMainPanel();
        int ow = 450, oh = 350;
        startOverlay.setBounds((w - ow) / 2, (h - oh) / 2, ow, oh);
        layeredPane.add(startOverlay, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Inicia el juego con los datos del jugador.
     * Configura la red UDP y arranca el game loop.
     *
     * @param nombre  nombre del jugador
     * @param ip      IP del host
     * @param isHost  true si este jugador es el host
     */
    public void startGame(String nombre, String ip, boolean isHost) {
        this.playerName = nombre;
        this.isHost = isHost;
        this.targetIp = (ip == null || ip.trim().isEmpty()) ? "127.0.0.1" : ip;

        int listenPort = isHost ? 5000 : 5001;
        int targetPort = isHost ? 5001 : 5000;

        engine.setHost(isHost);
        sender = new UDPSender();

        receiver = new UDPReceiver(listenPort, data -> {
            // Usamos invokeLater para que los datos en red no choquen con el dibujado de la pantalla
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (isHost && data instanceof MouseInputDTO) {
                    MouseInputDTO mouse = (MouseInputDTO) data;
                    engine.updatePlayerTarget(mouse.getPlayerName(), mouse.getTargetX(), mouse.getTargetY());
                } else if (!isHost && data instanceof GameSnapshot) {
                    engine.applySnapshot((GameSnapshot) data);
                }
            });
        });
        receiver.start();

        if (isHost) {
            engine.startGame(List.of(nombre, "Jugador2", "Jugador3"), 50.0);
        }

        gameScreen.setPlayerName(nombre);
        gameScreen.setSender(sender);
        gameScreen.setTargetIp(this.targetIp);
        gameScreen.setTargetPort(targetPort);

        layeredPane.remove(backgroundPanel);
        layeredPane.remove(startOverlay);

        gameScreen.getMainPanel().setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();

        gameScreen.startLoop();
    }
    /**
     * Muestra la pantalla final con los resultados del juego.
     *
     * @param winner    nombre del ganador
     * @param totalTime tiempo total de juego en milisegundos
     */
    public void showEndScreen(String winner, long totalTime) {
        int w = getWidth();
        int h = getHeight();

        endScreen = new EndScreen(this);
        endOverlay = endScreen.getMainPanel();

        int ow = 550, oh = 450;
        endOverlay.setBounds((w - ow) / 2, (h - oh) / 2, ow, oh);
        layeredPane.add(endOverlay, JLayeredPane.PALETTE_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();

        endScreen.show(winner, engine.getScoreManager().getAllScores(), totalTime);
    }

    /**
     * Lanza la ventana principal del juego.
     */
    public void launch() {
        setVisible(true);
    }
}