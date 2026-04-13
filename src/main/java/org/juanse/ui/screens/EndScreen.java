package org.juanse.ui.screens;

import org.juanse.ui.GameWindow;
import org.juanse.ui.sound.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Pantalla de fin del juego.
 * Muestra el ganador, puntajes finales y tiempo total de la partida.
 *
 * <p>Principio S (SRP): solo se encarga de mostrar los resultados finales.</p>
 * <p>Principio D (DIP): depende de GameWindow a través de su interfaz pública.</p>
 */
public class EndScreen {

    /** Panel principal de la pantalla. */
    private JPanel mainPanel;

    /** Panel central con la información del resultado. */
    private JPanel infoPanel;

    /** Label del título de la pantalla. */
    private JLabel tituloLabel;

    /** Botón para volver a la pantalla de inicio. */
    private JButton volverButton;

    /** Referencia a la ventana principal del juego. */
    private final GameWindow gameWindow;

    /**
     * Constructor de la pantalla final.
     *
     * @param gameWindow referencia a la ventana principal
     */
    public EndScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    /**
     * Inicializa el listener del botón volver al inicio.
     * Detiene la música, cierra la ventana actual y abre una nueva.
     */
    public void initListeners() {
        volverButton.addActionListener(e -> {
            SoundManager.stopBackground();
            gameWindow.dispose();
            SwingUtilities.invokeLater(() -> {
                GameWindow newWindow = new GameWindow();
                newWindow.launch();
            });
        });
    }

    /**
     * Muestra los resultados finales del juego.
     * Reproduce la música final, muestra el ganador, puntajes ordenados
     * de mayor a menor y el tiempo total de la partida.
     *
     * @param winner      nombre del jugador ganador
     * @param scores      mapa de nombre -> puntaje de cada jugador
     * @param totalTimeMs tiempo total de juego en milisegundos
     */
    public void show(String winner, Map<String, Integer> scores, long totalTimeMs) {
        SoundManager.playBackground("final.wav");
        initListeners();

        infoPanel.removeAll();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        infoPanel.add(Box.createVerticalStrut(40));

        // Label del ganador
        JLabel ganadorLabel = new JLabel("<< Ganador: " + winner + " >>");
        ganadorLabel.setForeground(new Color(255, 215, 0));
        ganadorLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        ganadorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(ganadorLabel);

        infoPanel.add(Box.createVerticalStrut(30));

        // Label de puntajes
        JLabel puntajesLabel = new JLabel("— PUNTAJES FINALES —");
        puntajesLabel.setForeground(new Color(255, 215, 0));
        puntajesLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        puntajesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(puntajesLabel);

        infoPanel.add(Box.createVerticalStrut(20));

        // Puntaje de cada jugador ordenado de mayor a menor
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

        // Tiempo total de la partida
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

    /**
     * Retorna el panel principal para agregarlo al contenedor de la ventana.
     *
     * @return panel principal de la pantalla final
     */
    public JPanel getMainPanel() { return mainPanel; }
}
