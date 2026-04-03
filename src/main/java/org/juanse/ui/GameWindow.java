package org.juanse.ui;

import org.juanse.logic.engine.GameEngine;
import org.juanse.ui.screens.StartScreen;
import org.juanse.ui.screens.GameScreen;
import org.juanse.ui.screens.EndScreen;
import org.juanse.UDP.UDPReceiver;
import org.juanse.UDP.UDPSender;
import org.juanse.UDP.MouseInputDTO;
import org.juanse.logic.engine.GameSnapshot;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal del juego.
 * Coordina las pantallas usando JLayeredPane para efecto overlay.
 * Principio S (SRP): solo coordina pantallas y red.
 */
public class GameWindow extends JFrame {

    private final JLayeredPane layeredPane;

    private GameEngine engine;
    private UDPSender sender;
    private UDPReceiver receiver;
    private String playerName;
    private boolean isHost;
    private String targetIp;

    private GameScreen gameScreen;
    private EndScreen endScreen;
    private JPanel startOverlay;
    private JPanel endOverlay;

    public GameWindow() {
        setTitle("Agar.io - Multijugador");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            setupLayers();
        }
    }

    private void setupLayers() {
        int w = getWidth();
        int h = getHeight();

        engine = new GameEngine(1200, 800);
        gameScreen = new GameScreen(this, engine, "", null, "", 0);
        JPanel gamePanel = gameScreen.getMainPanel();
        gamePanel.setBounds(0, 0, w, h);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);

        StartScreen startScreen = new StartScreen(this);
        startOverlay = startScreen.getMainPanel();
        int ow = 450, oh = 350;
        startOverlay.setBounds((w - ow) / 2, (h - oh) / 2, ow, oh);
        layeredPane.add(startOverlay, JLayeredPane.PALETTE_LAYER);

        gameScreen.startLoop();
    }

    public void startGame(String nombre, String ip, boolean isHost) {
        this.playerName = nombre;
        this.isHost     = isHost;
        this.targetIp   = ip;

        int listenPort = isHost ? 5000 : 5001;
        int targetPort = isHost ? 5001 : 5000;

        engine.setHost(isHost);
        sender = new UDPSender();

        receiver = new UDPReceiver(listenPort, data -> {
            if (isHost && data instanceof MouseInputDTO) {
                MouseInputDTO mouse = (MouseInputDTO) data;
                engine.getPlayers().stream()
                        .filter(p -> p.getOwnerName().equals(mouse.getPlayerName()))
                        .forEach(p -> p.setTarget(mouse.getTargetX(), mouse.getTargetY()));
            } else if (!isHost && data instanceof GameSnapshot) {
                engine.applySnapshot((GameSnapshot) data);
            }
        });
        receiver.start();

        if (isHost) {
            engine.startGame(List.of(nombre, "Jugador2", "Jugador3"), 50.0);
        }

        gameScreen.setPlayerName(nombre);
        gameScreen.setSender(sender);
        gameScreen.setTargetIp(targetIp);
        gameScreen.setTargetPort(targetPort);

        layeredPane.remove(startOverlay);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

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

    public void launch() {
        setVisible(true);
    }
}
