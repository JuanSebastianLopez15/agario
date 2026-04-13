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
 * Muestra el HUD con puntajes, tiempo y el indicador de FLASH,
 * y el canvas donde se dibuja el juego en tiempo real.
 *
 * <p>Principio S (SRP): solo se encarga de mostrar el juego.</p>
 * <p>Patrón Observer: implementa IGameEventListener para reaccionar a eventos.</p>
 */
public class GameScreen extends JPanel implements IGameEventListener {

    /** Panel principal que contiene HUD y canvas. */
    private JPanel mainPanel;

    /** Panel superior con puntajes, tiempo y FLASH. */
    private JPanel hudPanel;

    /** Panel central donde se dibuja el juego. */
    private JPanel gamePanel;

    /** Referencia a la ventana principal. */
    private final GameWindow gameWindow;

    /** Motor del juego. */
    private final GameEngine engine;

    /** Nombre del jugador local. */
    private String playerName;

    /** Sender UDP para enviar datos al host. */
    private UDPSender sender;

    /** IP del host destino. */
    private String targetIp;

    /** Puerto destino. */
    private int targetPort;

    /** Indica si este jugador es el host. */
    private boolean isHost;

    /** Lista de clientes conectados (solo la usa el host). */
    private List<InetSocketAddress> connectedClients;

    /** Canvas donde se dibuja el juego con Graphics2D. */
    private GamePanel canvas;

    /** Timer del game loop (50ms = 20 FPS). */
    private Timer gameLoop;

    /** Label del tiempo restante. */
    private JLabel timerLabel;

    /** Label con los puntajes de todos los jugadores. */
    private JLabel scoresLabel;

    /** Label del indicador de FLASH del jugador local. */
    private JLabel flashLabel;

    /** Barra de progreso del indicador FLASH. */
    private JProgressBar flashBar;

    /** Indica si el espacio fue presionado en este tick. */
    private boolean spacePressed = false;

    /** Última posición X del mouse. */
    private int lastMouseX = 600;

    /** Última posición Y del mouse. */
    private int lastMouseY = 400;

    /** Duración del dash en ms. */
    private static final long DASH_DURATION_MS = 4000;

    /** Duración del cooldown en ms. */
    private static final long DASH_COOLDOWN_MS = 10000;

    /**
     * Constructor de la pantalla de juego.
     *
     * @param gameWindow referencia a la ventana principal
     * @param engine     motor del juego
     * @param playerName nombre del jugador local
     * @param sender     sender UDP
     * @param targetIp   IP del host
     * @param targetPort puerto destino
     */
    public GameScreen(GameWindow gameWindow, GameEngine engine, String playerName,
                      UDPSender sender, String targetIp, int targetPort) {
        this.gameWindow = gameWindow;
        this.engine     = engine;
        this.playerName = playerName;
        this.sender     = sender;
        this.targetIp   = targetIp;
        this.targetPort = targetPort;

        engine.addListener(this);
        setupHUD();
        setupCanvas();
    }

    /**
     * Configura el HUD con el indicador FLASH a la izquierda
     * y los puntajes con el tiempo al centro.
     */
    private void setupHUD() {
        hudPanel.setLayout(new BorderLayout());

        // Panel izquierdo — indicador FLASH con label y barra
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(160, 40));

        flashLabel = new JLabel("FLASH LISTO!");
        flashLabel.setForeground(new Color(0, 255, 100));
        flashLabel.setFont(new Font("Arial", Font.BOLD, 13));

        flashBar = new JProgressBar(0, 100);
        flashBar.setValue(100);
        flashBar.setStringPainted(false);
        flashBar.setForeground(new Color(0, 255, 100));
        flashBar.setBackground(new Color(40, 40, 40));
        flashBar.setBorderPainted(false);
        flashBar.setPreferredSize(new Dimension(140, 10));

        leftPanel.add(flashLabel);
        leftPanel.add(flashBar);

        // Panel central — puntajes y tiempo
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 8));
        centerPanel.setOpaque(false);

        timerLabel  = new JLabel("⏱ 3:00");
        scoresLabel = new JLabel("");

        timerLabel.setForeground(Color.WHITE);
        scoresLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoresLabel.setFont(new Font("Arial", Font.BOLD, 18));

        centerPanel.add(scoresLabel);
        centerPanel.add(timerLabel);

        hudPanel.add(leftPanel, BorderLayout.WEST);
        hudPanel.add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Configura el canvas de dibujo, el listener del mouse
     * y el listener del teclado para el dash (Espacio).
     */
    private void setupCanvas() {
        canvas = new GamePanel(engine, playerName);
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(canvas, BorderLayout.CENTER);

        canvas.setFocusable(true);
        canvas.requestFocusInWindow();

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

    /**
     * Envía la posición del mouse y el estado del dash al host.
     * Si es host, actualiza directamente el motor.
     * Si es cliente, envía por UDP.
     */
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

    /**
     * Inicia el game loop, la música de fondo y envía el paquete
     * inicial de registro si es cliente.
     */
    public void startLoop() {
        SoundManager.playBackground("juego.wav");

        if (!isHost && sender != null && targetIp != null && !targetIp.isEmpty()) {
            MouseInputDTO paqueteInicial = new MouseInputDTO(playerName, 600, 400, false);
            sender.sendObject(paqueteInicial, targetIp, targetPort);
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

    /**
     * Actualiza el HUD con el tiempo restante, puntajes
     * y el indicador de FLASH con barra de progreso.
     */
    private void updateHUD() {
        long ms  = engine.getRemainingTimeMs();
        long min = ms / 60000;
        long sec = (ms % 60000) / 1000;
        timerLabel.setText(String.format("⏱ %d:%02d", min, sec));

        StringBuilder sb = new StringBuilder();
        engine.getScoreManager().getAllScores()
                .forEach((name, score) -> sb.append(name).append(": ").append(score).append("pts   "));
        scoresLabel.setText(sb.toString());

        // Indicador FLASH con barra
        engine.getPlayers().stream()
                .filter(p -> p.getOwnerName().equals(playerName))
                .findFirst()
                .ifPresent(p -> {
                    long elapsed = System.currentTimeMillis() - p.getLastDashTime();

                    if (p.isDashing()) {
                        // Dash activo — barra amarilla bajando
                        long remaining = DASH_DURATION_MS - elapsed;
                        int segundos = (int)(remaining / 1000) + 1;
                        int progress = (int)((remaining * 100) / DASH_DURATION_MS);
                        flashLabel.setText("FLASH " + segundos + "s");
                        flashLabel.setForeground(new Color(255, 215, 0));
                        flashBar.setValue(Math.max(0, progress));
                        flashBar.setForeground(new Color(255, 215, 0));

                    } else {
                        long remaining = p.getDashCooldownRemaining();
                        if (remaining <= 0) {
                            // Listo — barra verde llena
                            flashLabel.setText("FLASH LISTO!");
                            flashLabel.setForeground(new Color(0, 255, 100));
                            flashBar.setValue(100);
                            flashBar.setForeground(new Color(0, 255, 100));
                        } else {
                            // Cooldown — barra naranja llenándose
                            long elapsed2 = elapsed - DASH_DURATION_MS;
                            int progress = (int)((elapsed2 * 100) / DASH_COOLDOWN_MS);
                            flashLabel.setText("FLASH " + remaining + "s");
                            flashLabel.setForeground(new Color(255, 165, 0));
                            flashBar.setValue(Math.min(100, Math.max(0, progress)));
                            flashBar.setForeground(new Color(255, 165, 0));
                        }
                    }
                });
    }

    /** @param playerName nombre del jugador local */
    public void setPlayerName(String playerName)   { this.playerName = playerName; }

    /** @param sender sender UDP */
    public void setSender(UDPSender sender)         { this.sender = sender; }

    /** @param targetIp IP del host */
    public void setTargetIp(String targetIp)        { this.targetIp = targetIp; }

    /** @param targetPort puerto destino */
    public void setTargetPort(int targetPort)       { this.targetPort = targetPort; }

    /** @param host true si este jugador es el host */
    public void setHost(boolean host)               { this.isHost = host; }

    /** @param clients lista de clientes conectados */
    public void setConnectedClients(List<InetSocketAddress> clients) {
        this.connectedClients = clients;
    }

    /** @return panel principal de la pantalla */
    public JPanel getMainPanel() { return mainPanel; }

    /** Reproduce sonido de absorción cuando una célula come a otra. */
    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        if ((absorber != null && absorber.getOwnerName().equals(this.playerName)) ||
                (absorbed != null && absorbed.getOwnerName().equals(this.playerName))) {
            SoundManager.playEffect("absorption.wav");
        }
    }

    /** Reproduce sonido de split cuando una célula toca una HazardBall. */
    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        if (original != null && original.getOwnerName().equals(this.playerName)) {
            SoundManager.playEffect("split.wav");
        }
    }

    /** Reproduce sonido cuando una célula come un pellet. */
    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        if (player != null && player.getOwnerName().equals(this.playerName)) {
            SoundManager.playEffect("eat_pellet.wav");
        }
    }

    /** Detiene la música y muestra la pantalla final. */
    @Override
    public void onGameOver(String winnerName, String reason) {
        SwingUtilities.invokeLater(() -> {
            if (gameLoop != null) gameLoop.stop();
            SoundManager.stopBackground();
            gameWindow.showEndScreen(winnerName, engine.getTotalElapsedMs());
        });
    }
}