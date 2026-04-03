package org.juanse.ui.screens;

import org.juanse.ui.GameWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Pantalla de inicio del juego.
 * Pide nombre del jugador, IP del host y si es host o cliente.
 */
public class StartScreen extends JPanel {

    private JPanel mainPanel;
    private JTextField nombreField;
    private JTextField ipField;
    private JCheckBox hostCheckBox;
    private JButton jugarButton;

    private GameWindow gameWindow;

    public StartScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        jugarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre = nombreField.getText().trim();
                String ip     = ipField.getText().trim();
                boolean isHost = hostCheckBox.isSelected();

                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "¡Escribe tu nombre!");
                    return;
                }

                if (!isHost && ip.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "¡Escribe la IP del host!");
                    return;
                }

                gameWindow.startGame(nombre, ip, isHost);
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
