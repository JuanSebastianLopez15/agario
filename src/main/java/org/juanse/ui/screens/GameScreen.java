package org.juanse.ui.screens;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.engine.GameSnapshot;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;
import org.juanse.udp.MouseInputDTO;
import org.juanse.udp.UDPSender;
import org.juanse.ui.GameWindow;
import org.juanse.ui.renderer.GamePanel;
import org.juanse.ui.sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Pantalla principal del juego.
 * Principio S (SRP): solo muestra el juego y envía input del mouse.
 * Patrón Observer: implementa IGameEventListener.
 */
public class GameScreen extends JPanel implements IGameEventListener {

    private JPanel mainPanel;
    private JPanel hudPanel;
    private JPanel gamePanel;

    private final GameWindow gameWindow;
    private final GameEngine engine;

    private String playerName;
    private UDPSender sender;
    private String targetIp;
    private int    targetPort;
    private boolean isHost;

    /**
     * Lista de clientes a los que el host debe enviar el snapshot.
     * Vacía si este nodo es cliente.
     */
    private List<InetSocketAddress> connectedClients;

    private GamePanel canvas;
    private Timer     gameLoop;
    private JLabel    timerLabel;
    private JLabel    scoresLabel;

    public GameScreen(GameWindow gameWindow, GameEngine engine, String playerName,
                      UDPSender sender, String targetIp, int targetPort) {
        this.gameWindow  = gameWindow;
        this.engine      = engine;
        this.playerName  = playerName;
        this.sender      = sender;
        this.targetIp    = targetIp;
        this.targetPort  = targetPort;

        engine.addListener(this);
        setupHUD();
        setupCanvas();
    }

    private void setupHUD() {
        hudPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));

        timerLabel  = new JLabel("⏱ 3:00");
        scoresLabel = new JLabel("");

        timerLabel.setForeground(Color.WHITE);
        scoresLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoresLabel.setFont(new Font("Arial", Font.BOLD, 18));

        hudPanel.add(scoresLabel);
        hudPanel.add(timerLabel);
    }

    private void setupCanvas() {
        canvas = new GamePanel(engine, playerName);
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(canvas, BorderLayout.CENTER);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Actualizar target local siempre
                engine.getPlayers().stream()
                        .filter(p -> p.getOwnerName().equals(playerName))
                        .forEach(p -> p.setTarget(e.getX(), e.getY()));

                // Si es cliente, enviar mouse al host
                if (!isHost && sender != null && targetIp != null && !targetIp.isEmpty()) {
                    MouseInputDTO mouse = new MouseInputDTO(playerName, e.getX(), e.getY());
                    sender.sendObject(mouse, targetIp, targetPort);
                }
            }
        });
    }

    public void startLoop() {
        SoundManager.playBackground("juego.wav");

        // Si es cliente, enviar un paquete inicial para que el host lo detecte
        // y empiece a enviarle snapshots de inmediato
        if (!isHost && sender != null && targetIp != null && !targetIp.isEmpty()) {
            MouseInputDTO paqueteInicial = new MouseInputDTO(playerName, 600, 400);
            sender.sendObject(paqueteInicial, targetIp, targetPort);
            System.out.println("Cliente registrado, paquete inicial enviado al host.");
        }

        gameLoop = new Timer(50, e -> {
            if (engine.isHost()) {
                engine.update();

                if (sender != null && connectedClients != null) {
                    GameSnapshot snapshot = engine.createSnapshot();
                    for (InetSocketAddress client : connectedClients) {
                        sender.sendObject(snapshot,
                                client.getAddress().getHostAddress(),
                                client.getPort());
                    }
                }
            }

            updateHUD();
            canvas.repaint();

            if (engine.isGameOver()) {
                gameLoop.stop();
                gameWindow.showEndScreen(
                        engine.getScoreManager().getLeader(),
                        engine.getTotalElapsedMs()
                );
            }
        });
        gameLoop.start();
    }

    private void updateHUD() {
        long ms  = engine.getRemainingTimeMs();
        long min = ms / 60000;
        long sec = (ms % 60000) / 1000;
        timerLabel.setText(String.format("⏱ %d:%02d", min, sec));

        StringBuilder sb = new StringBuilder();
        engine.getScoreManager().getAllScores()
                .forEach((name, score) -> sb.append(name).append(": ").append(score).append("pts   "));
        scoresLabel.setText(sb.toString());
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setPlayerName(String playerName)   { this.playerName = playerName; }
    public void setSender(UDPSender sender)         { this.sender = sender; }
    public void setTargetIp(String targetIp)        { this.targetIp = targetIp; }
    public void setTargetPort(int targetPort)       { this.targetPort = targetPort; }
    public void setHost(boolean host)               { this.isHost = host; }
    public void setConnectedClients(List<InetSocketAddress> clients) {
        this.connectedClients = clients;
    }

    public JPanel getMainPanel() { return mainPanel; }

    // ── Observer ─────────────────────────────────────────────────────────────

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        SoundManager.playEffect("absorption.wav");
    }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        SoundManager.playEffect("split.wav");
    }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        SoundManager.playEffect("eat_pellet.wav");
    }

    @Override
    public void onGameOver(String winnerName, String reason) {
        SwingUtilities.invokeLater(() -> {
            if (gameLoop != null) gameLoop.stop();
            SoundManager.stopBackground();
            gameWindow.showEndScreen(winnerName, engine.getTotalElapsedMs());
        });
    }
}