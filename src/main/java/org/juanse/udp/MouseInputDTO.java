package org.juanse.udp;

import java.io.Serializable;

/**
 * Este objeto lo enviará el Cliente al Host repetidamente
 * para decirle dónde está su mouse y si presionó el botón de acelerar.
 */
public class MouseInputDTO implements Serializable {
    private String playerName;
    private double targetX;
    private double targetY;
    private boolean dashPressed;

    public MouseInputDTO(String playerName, double targetX, double targetY, boolean dashPressed) {
        this.playerName = playerName;
        this.targetX = targetX;
        this.targetY = targetY;
        this.dashPressed = dashPressed;
    }

    public String getPlayerName() { return playerName; }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    public boolean isDashPressed() { return dashPressed; }
}