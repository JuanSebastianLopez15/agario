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
 * Coordina las pantallas usando JLayeredPane para efecto overlay.
 *
 * Principio S (SRP): solo coordina pantallas y red.
 * Principio D (DIP): depende de abstracciones, no de implementaciones concretas.
 */
public class GameWindow extends JFrame {

    private final JLayeredPane layeredPane;

    private GameEngine    engine;
    private UDPSender     sender;
    private UDPReceiver   receiver;

    private String  playerName;
    private boolean isHost;
    private String  targetIp;

    private GameScreen      gameScreen;
    private EndScreen       endScreen;
    private JPanel          startOverlay;
    private JPanel          endOverlay;
    private BackgroundPanel backgroundPanel;

    /**
     * Lista de clientes conectados (solo la usa el host).
     * Cada entrada es la dirección IP:puerto desde donde llegó un MouseInputDTO.
     * CopyOnWriteArrayList para seguridad entre hilos.
     */
    private final List<InetSocketAddress> connectedClients = new CopyOnWriteArrayList<>();

    /** Puerto fijo en el que el host escucha MouseInputDTOs de todos los clientes. */
    private static final int HOST_LISTEN_PORT   = 5000;

    /** Puerto fijo en el que cada cliente escucha los GameSnapshots del host. */
    private static final int CLIENT_LISTEN_PORT = 5001;

    public GameWindow() {
        setTitle("Agar.io - Multijugador");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) setupLayers();
    }

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
     * Inicia el juego.
     *
     * HOST:
     * - Escucha en HOST_LISTEN_PORT (5000) los MouseInputDTO de TODOS los clientes.
     * - Cuando llega un cliente nuevo, guarda su IP y le envía snapshots al puerto
     * CLIENT_LISTEN_PORT (5001).
     *
     * CLIENTE:
     * - Escucha en CLIENT_LISTEN_PORT (5001) los GameSnapshot del host.
     * - Envía su mouse al HOST_LISTEN_PORT (5000) de la IP del host.
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
                                mouse.getTargetY()
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

    public void launch() {
        setVisible(true);
    }
}