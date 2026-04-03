package org.juanse.ui.screens;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;
import org.juanse.ui.GameWindow;
import org.juanse.ui.renderer.GamePanel;
import org.juanse.UDP.MouseInputDTO;
import org.juanse.UDP.UDPSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

/**
 * Pantalla principal del juego.
 * Muestra el HUD y el canvas del juego en tiempo real.
 *
 * Principio S (SRP): solo se encarga de mostrar el juego.
 * Patrón Observer: implementa IGameEventListener para reaccionar a eventos.
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
    private int targetPort;

    private GamePanel canvas;
    private Timer gameLoop;

    private JLabel timerLabel;
    private JLabel scoresLabel;

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
                if (sender == null || targetIp == null || targetIp.isEmpty()) return;

                MouseInputDTO mouse = new MouseInputDTO(playerName, e.getX(), e.getY());
                sender.sendObject(mouse, targetIp, targetPort);

                engine.getPlayers().stream()
                        .filter(p -> p.getOwnerName().equals(playerName))
                        .forEach(p -> p.setTarget(e.getX(), e.getY()));
            }
        });
    }

    public void startLoop() {
        gameLoop = new Timer(50, e -> {
            if (engine.isHost()) {
                engine.update();
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
        engine.getScoreManager().getAllScores().forEach((name, score) ->
                sb.append(name).append(": ").append(score).append("pts   "));
        scoresLabel.setText(sb.toString());
    }

    // ── Setters ──────────────────────────────────────────────

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setSender(UDPSender sender) {
        this.sender = sender;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public JPanel getMainPanel() { return mainPanel; }

    // ── Observer callbacks ────────────────────────────────────

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) { }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) { }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) { }

    @Override
    public void onGameOver(String winnerName, String reason) {
        SwingUtilities.invokeLater(() -> {
            if (gameLoop != null) gameLoop.stop();
            gameWindow.showEndScreen(winnerName, engine.getTotalElapsedMs());
        });
    }
}
