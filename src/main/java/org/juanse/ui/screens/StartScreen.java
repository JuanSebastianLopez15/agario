package org.juanse.ui.screens;

import org.juanse.ui.GameWindow;
import org.juanse.ui.sound.SoundManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Pantalla de inicio del juego.
 * Muestra el formulario de entrada donde el jugador escribe su nombre,
 * la IP del host y si actuara como host o cliente.
 *
 * <p>Principio S (SRP): solo gestiona la entrada de datos del jugador.</p>
 * <p>Principio D (DIP): depende de GameWindow a través de su interfaz publica.</p>
 */
public class StartScreen extends JPanel {

    /** Campo de texto para el nombre del jugador. */
    private JTextField nombreField;

    /** Campo de texto para la IP del host. */
    private JTextField ipField;

    /** Checkbox que indica si este jugador es el host. */
    private JCheckBox hostCheckBox;

    /** Botón para iniciar el juego. */
    private JButton jugarButton;

    /** Panel principal del formulario. */
    private JPanel mainPanel;

    /** Referencia a la ventana principal del juego. */
    private GameWindow gameWindow;

    /**
     * Constructor de la pantalla de inicio.
     * Inicia la música de fondo y configura el botón de jugar.
     *
     * @param gameWindow referencia a la ventana principal
     */
    public StartScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;

        SoundManager.playBackground("inicio.wav");

        jugarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre  = nombreField.getText().trim();
                String ip      = ipField.getText().trim();
                boolean isHost = hostCheckBox.isSelected();

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "¡Escribe tu nombre!");
                    return;
                }

                if (!isHost && ip.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "¡Escribe la IP del host!");
                    return;
                }

                SoundManager.stopBackground();
                gameWindow.startGame(nombre, ip, isHost);
            }
        });
    }

    /**
     * Retorna el panel principal para agregarlo al contenedor de la ventana.
     *
     * @return panel principal del formulario
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
}