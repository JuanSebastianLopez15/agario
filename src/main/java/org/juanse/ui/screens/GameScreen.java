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

    private List<InetSocketAddress> connectedClients;

    private GamePanel canvas;
    private Timer     gameLoop;
    private JLabel    timerLabel;
    private JLabel    scoresLabel;

    // Variables de control de input
    private boolean spacePressed = false;
    private int lastMouseX = 600;
    private int lastMouseY = 400;

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

        // Para que el JPanel escuche teclas, debe tener el foco
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

        // Solicitar foco si el mouse entra al panel (para evitar perder los controles)
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                canvas.requestFocusInWindow();
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                sendInput();
            }
        });

        // Escuchar la tecla Espacio
        canvas.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    spacePressed = true;
                    sendInput();
                }
            }
        });
    }

    private void sendInput() {
        if (engine.isHost()) {
            engine.updatePlayerTarget(playerName, lastMouseX, lastMouseY, spacePressed);
            spacePressed = false;
        } else if (sender != null && targetIp != null && !targetIp.isEmpty()) {
            MouseInputDTO mouse = new MouseInputDTO(playerName, lastMouseX, lastMouseY, spacePressed);
            sender.sendObject(mouse, targetIp, targetPort);
            spacePressed = false;
        }
    }

    public void startLoop() {
        SoundManager.playBackground("juego.wav");

        if (!isHost && sender != null && targetIp != null && !targetIp.isEmpty()) {
            MouseInputDTO paqueteInicial = new MouseInputDTO(playerName, 600, 400, false);
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

    public void setPlayerName(String playerName)   { this.playerName = playerName; }
    public void setSender(UDPSender sender)         { this.sender = sender; }
    public void setTargetIp(String targetIp)        { this.targetIp = targetIp; }
    public void setTargetPort(int targetPort)       { this.targetPort = targetPort; }
    public void setHost(boolean host)               { this.isHost = host; }
    public void setConnectedClients(List<InetSocketAddress> clients) {
        this.connectedClients = clients;
    }

    public JPanel getMainPanel() { return mainPanel; }

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        if ((absorber != null && absorber.getOwnerName().equals(this.playerName)) ||
                (absorbed != null && absorbed.getOwnerName().equals(this.playerName))) {
            SoundManager.playEffect("absorption.wav");
        }
    }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        if (original != null && original.getOwnerName().equals(this.playerName)) {
            SoundManager.playEffect("split.wav");
        }
    }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        if (player != null && player.getOwnerName().equals(this.playerName)) {
            SoundManager.playEffect("eat_pellet.wav");
        }
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