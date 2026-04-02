package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;

/**
 * Estado final: el juego terminó.
 * No aplica ninguna regla; solo conserva la información del ganador.
 */
public class GameOverState implements IGameState {

    private final String winnerName;
    private final String reason;
    private final long endTimestamp;

    public GameOverState(String winnerName, String reason) {
        this.winnerName = winnerName;
        this.reason = reason;
        this.endTimestamp = System.currentTimeMillis();
    }

    @Override
    public void update(GameEngine engine) {
        // El juego terminó: no se aplica ninguna lógica
    }

    @Override
    public boolean isRunning() { return false; }

    @Override
    public String getStateName() { return "FIN DEL JUEGO"; }

    public String getWinnerName() { return winnerName; }
    public String getReason() { return reason; }
    public long getEndTimestamp() { return endTimestamp; }
}

