package org.juanse.ui.screens;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;
import org.juanse.ui.GameWindow;
import org.juanse.ui.renderer.GamePanel;
import org.juanse.ui.sound.SoundManager;
import org.juanse.udp.MouseInputDTO;
import org.juanse.udp.UDPSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

/**
 * Pantalla principal del juego.
 * Muestra el HUD y el canvas del juego en tiempo real.
 *
 * <p>Principio S (SRP): solo se encarga de mostrar el juego.</p>
 * <p>Patrón Observer: implementa IGameEventListener para reaccionar a eventos.</p>
 */
public class GameScreen extends JPanel implements IGameEventListener {

    /** Panel principal de la pantalla. */
    private JPanel mainPanel;

    /** Panel superior con puntajes y tiempo. */
    private JPanel hudPanel;

    /** Panel central donde se dibuja el juego. */
    private JPanel gamePanel;

    /** Referencia a la ventana principal. */
    private final GameWindow gameWindow;

    /** Motor del juego. */
    private final GameEngine engine;

    /** Nombre del jugador local. */
    private String playerName;

    /** Sender UDP para enviar posición del mouse. */
    private UDPSender sender;

    /** IP del host destino. */
    private String targetIp;

    /** Puerto destino. */
    private int targetPort;

    /** Canvas donde se dibuja el juego con Graphics2D. */
    private GamePanel canvas;

    /** Timer del game loop (50ms = 20 FPS). */
    private Timer gameLoop;

    /** Label del tiempo restante. */
    private JLabel timerLabel;

    /** Label con los puntajes de todos los jugadores. */
    private JLabel scoresLabel;

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

    /**
     * Configura el HUD con los labels de puntaje y tiempo.
     */
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

    /**
     * Configura el canvas de dibujo y el listener del mouse.
     */
    private void setupCanvas() {
        canvas = new GamePanel(engine, playerName);
        gamePanel.setLayout(new BorderLayout());
        gamePanel.add(canvas, BorderLayout.CENTER);

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (sender == null || targetIp == null || targetIp.isEmpty()) return;

                MouseInputDTO mouse = new MouseInputDTO(playerName, e.getX(), e.getY());
                sender.sendObject(mouse, targetIp, targetPort);

                engine.getPlayers().stream()
                        .filter(p -> p.getOwnerName().equals(playerName))
                        .forEach(p -> p.setTarget(e.getX(), e.getY()));
            }
        });
    }

    /**
     * Inicia el game loop y la música de fondo del juego.
     */
    public void startLoop() {
        SoundManager.playBackground("juego.wav");

        gameLoop = new Timer(50, e -> {
            if (engine.isHost()) {
                engine.update();
                //el host envia el estado del juego
                org.juanse.logic.engine.GameSnapshot snapshot = engine.createSnapshot();
                if (sender != null && targetIp != null && !targetIp.isEmpty()) {
                    sender.sendObject(snapshot, targetIp, targetPort);
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
     * Actualiza los labels del HUD con tiempo y puntajes actuales.
     */
    private void updateHUD() {
        long ms  = engine.getRemainingTimeMs();
        long min = ms / 60000;
        long sec = (ms % 60000) / 1000;
        timerLabel.setText(String.format("⏱ %d:%02d", min, sec));

        StringBuilder sb = new StringBuilder();
        engine.getScoreManager().getAllScores().forEach((name, score) ->
                sb.append(name).append(": ").append(score).append("pts   "));
        scoresLabel.setText(sb.toString());
    }

    /** @param playerName nombre del jugador local */
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    /** @param sender sender UDP */
    public void setSender(UDPSender sender) { this.sender = sender; }

    /** @param targetIp IP del host */
    public void setTargetIp(String targetIp) { this.targetIp = targetIp; }

    /** @param targetPort puerto destino */
    public void setTargetPort(int targetPort) { this.targetPort = targetPort; }

    /** @return panel principal */
    public JPanel getMainPanel() { return mainPanel; }

    /** Reproduce sonido de absorción cuando una célula come a otra. */
    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        SoundManager.playEffect("absorption.wav");
    }

    /** Reproduce sonido de split cuando una célula toca una HazardBall. */
    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        SoundManager.playEffect("split.wav");
    }

    /** Reproduce sonido cuando una célula come un pellet. */
    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        SoundManager.playEffect("eat_pellet.wav");
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
