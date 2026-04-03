package org.juanse.ui.screens;

import org.juanse.ui.GameWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Pantalla de fin del juego.
 * Muestra ganador, puntajes y tiempo total.
 * Principio S (SRP): solo muestra resultados finales.
 */
public class EndScreen {

    private JPanel mainPanel;
    private JPanel infoPanel;
    private JLabel tituloLabel;
    private JButton volverButton;

    private final GameWindow gameWindow;

    public EndScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void initListeners() {
        volverButton.addActionListener(e -> {
            gameWindow.dispose();
            SwingUtilities.invokeLater(() -> {
                GameWindow newWindow = new GameWindow();
                newWindow.launch();
            });
        });
    }

    public void show(String winner, Map<String, Integer> scores, long totalTimeMs) {
        initListeners();

        infoPanel.removeAll();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(Box.createVerticalStrut(40));

        JLabel ganadorLabel = new JLabel("★ Ganador: " + winner + " ★");
        ganadorLabel.setForeground(new Color(255, 215, 0));
        ganadorLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        ganadorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(ganadorLabel);

        infoPanel.add(Box.createVerticalStrut(30));

        JLabel puntajesLabel = new JLabel("— PUNTAJES FINALES —");
        puntajesLabel.setForeground(new Color(255, 215, 0));
        puntajesLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        puntajesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(puntajesLabel);

        infoPanel.add(Box.createVerticalStrut(20));

        scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    JLabel label = new JLabel(entry.getKey() + ":  " + entry.getValue() + " pts");
                    label.setForeground(Color.WHITE);
                    label.setFont(new Font("Consolas", Font.BOLD, 18));
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    infoPanel.add(label);
                    infoPanel.add(Box.createVerticalStrut(12));
                });

        infoPanel.add(Box.createVerticalStrut(20));

        long min = totalTimeMs / 60000;
        long sec = (totalTimeMs % 60000) / 1000;
        JLabel tiempoLabel = new JLabel(String.format("Tiempo total: %d:%02d", min, sec));
        tiempoLabel.setForeground(new Color(180, 180, 180));
        tiempoLabel.setFont(new Font("Consolas", Font.BOLD, 16));
        tiempoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(tiempoLabel);
        infoPanel.revalidate();
        infoPanel.repaint();
    }

    public JPanel getMainPanel() { return mainPanel; }
}