package org.juanse.udp;

import java.io.Serializable;

/**
 * Este objeto lo enviará el Cliente al Host repetidamente
 * para decirle dónde está su mouse.
 */
public class MouseInputDTO implements Serializable {
    private String playerName;
    private double targetX;
    private double targetY;

    public MouseInputDTO(String playerName, double targetX, double targetY) {
        this.playerName = playerName;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public String getPlayerName() { return playerName; }
    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
}
