package org.juanse.ui;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.engine.GameSnapshot;
import org.juanse.udp.MouseInputDTO;
import org.juanse.udp.UDPReceiver;
import org.juanse.udp.UDPSender;
import org.juanse.ui.renderer.BackgroundPanel;
import org.juanse.ui.screens.EndScreen;
import org.juanse.ui.screens.GameScreen;
import org.juanse.ui.screens.StartScreen;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ventana principal del juego.
 * Coordina las pantallas usando JLayeredPane para efecto overlay,
 * y gestiona la comunicacion UDP entre host y clientes.
 *
 * <p>Principio S (SRP): solo coordina pantallas y red.</p>
 * <p>Principio D (DIP): depende de abstracciones, no de implementaciones concretas.</p>
 */
public class GameWindow extends JFrame {

    /** Panel en capas que permite el efecto overlay entre pantallas. */
    private final JLayeredPane layeredPane;

    /** Motor del juego que contiene toda la logica. */
    private GameEngine engine;

    /** Sender UDP para enviar datos a los otros jugadores. */
    private UDPSender sender;

    /** Receiver UDP para recibir datos de los otros jugadores. */
    private UDPReceiver receiver;

    /** Nombre del jugador local. */
    private String playerName;

    /** Indica si este jugador actua como host. */
    private boolean isHost;

    /** IP del host destino. */
    private String targetIp;

    /** Pantalla principal del juego con canvas y HUD. */
    private GameScreen gameScreen;

    /** Pantalla de fin del juego con resultados. */
    private EndScreen endScreen;

    /** Panel overlay de la pantalla de inicio. */
    private JPanel startOverlay;

    /** Panel overlay de la pantalla final. */
    private JPanel endOverlay;

    /** Panel de fondo decorativo con celulas y pellets. */
    private BackgroundPanel backgroundPanel;

    /**
     * Lista de clientes conectados (solo la usa el host).
     * CopyOnWriteArrayList para seguridad entre hilos.
     */
    private final List<InetSocketAddress> connectedClients = new CopyOnWriteArrayList<>();

    /** Puerto fijo en el que el host escucha MouseInputDTOs de todos los clientes. */
    private static final int HOST_LISTEN_PORT = 5000;

    /** Puerto fijo en el que cada cliente escucha los GameSnapshots del host. */
    private static final int CLIENT_LISTEN_PORT = 5001;

    /**
     * Constructor de la ventana principal.
     * Inicializa el JFrame en pantalla completa y configura el JLayeredPane.
     */
    public GameWindow() {
        setTitle("Agar.io - Multijugador");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        if (visible) setupLayers();
    }

    /**
     * Configura las capas del juego.
     * Capa DEFAULT: fondo decorativo y pantalla del juego oculta.
     * Capa PALETTE: overlay de inicio encima del fondo.
     */
    private void setupLayers() {
        int w = getWidth();
        int h = getHeight();

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setBounds(0, 0, w, h);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        engine     = new GameEngine(w, h);
        gameScreen = new GameScreen(this, engine, "", null, "", 0);
        JPanel gamePanel = gameScreen.getMainPanel();
        gamePanel.setBounds(0, 0, w, h);
        gamePanel.setVisible(false);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

        StartScreen startScreen = new StartScreen(this);
        startOverlay = startScreen.getMainPanel();
        int ow = 450, oh = 350;
        startOverlay.setBounds((w - ow) / 2, (h - oh) / 2, ow, oh);
        layeredPane.add(startOverlay, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * Inicia el juego configurando la red UDP y el game loop.
     *
     * <p>HOST: escucha en {@code HOST_LISTEN_PORT} los MouseInputDTO de todos los
     * clientes, registra clientes nuevos y actualiza el motor con su input.</p>
     * <p>CLIENTE: escucha en {@code CLIENT_LISTEN_PORT} los GameSnapshot del host
     * y los aplica al motor local.</p>
     *
     * @param nombre  nombre del jugador local
     * @param ip      IP del host (ignorada si es host)
     * @param isHost  true si este jugador actua como host
     */
    public void startGame(String nombre, String ip, boolean isHost) {
        this.playerName = nombre;
        this.isHost     = isHost;
        this.targetIp   = (ip == null || ip.trim().isEmpty()) ? "127.0.0.1" : ip.trim();

        engine.setHost(isHost);
        sender = new UDPSender();

        if (isHost) {
            receiver = new UDPReceiver(HOST_LISTEN_PORT, (data, senderAddress) -> {
                SwingUtilities.invokeLater(() -> {
                    if (data instanceof MouseInputDTO) {
                        MouseInputDTO mouse = (MouseInputDTO) data;

                        InetSocketAddress clientAddr =
                                new InetSocketAddress(senderAddress.getAddress(), CLIENT_LISTEN_PORT);
                        if (!connectedClients.contains(clientAddr)) {
                            connectedClients.add(clientAddr);
                            System.out.println("Nuevo cliente conectado: " + clientAddr);
                        }

                        engine.updatePlayerTarget(
                                mouse.getPlayerName(),
                                mouse.getTargetX(),
                                mouse.getTargetY(),
                                mouse.isDashPressed()
                        );
                    }
                });
            });

        } else {
            receiver = new UDPReceiver(CLIENT_LISTEN_PORT, (data, senderAddress) -> {
                SwingUtilities.invokeLater(() -> {
                    if (data instanceof GameSnapshot) {
                        engine.applySnapshot((GameSnapshot) data);
                    }
                });
            });
        }

        receiver.start();

        if (isHost) {
            engine.startGame(List.of(nombre), 50.0);
        }

        gameScreen.setPlayerName(nombre);
        gameScreen.setSender(sender);
        gameScreen.setTargetIp(this.targetIp);
        gameScreen.setTargetPort(isHost ? CLIENT_LISTEN_PORT : HOST_LISTEN_PORT);
        gameScreen.setConnectedClients(connectedClients);
        gameScreen.setHost(isHost);

        layeredPane.remove(backgroundPanel);
        layeredPane.remove(startOverlay);
        gameScreen.getMainPanel().setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();

        gameScreen.startLoop();
    }

    /**
     * Muestra la pantalla final con los resultados del juego como overlay.
     *
     * @param winner    nombre del jugador ganador
     * @param totalTime tiempo total de juego en milisegundos
     */
    public void showEndScreen(String winner, long totalTime) {
        int w = getWidth();
        int h = getHeight();

        endScreen  = new EndScreen(this);
        endOverlay = endScreen.getMainPanel();

        int ow = 550, oh = 450;
        endOverlay.setBounds((w - ow) / 2, (h - oh) / 2, ow, oh);
        layeredPane.add(endOverlay, JLayeredPane.PALETTE_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();

        endScreen.show(winner, engine.getScoreManager().getAllScores(), totalTime);
    }

    /**
     * Lanza la ventana principal del juego haciendola visible.
     */
    public void launch() {
        setVisible(true);
    }
}