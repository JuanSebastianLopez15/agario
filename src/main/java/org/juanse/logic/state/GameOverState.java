package org.juanse.logic.state;

import org.juanse.logic.engine.GameEngine;

/**
 * Estado final del juego.
 * No aplica ninguna regla; solo conserva la informacion del ganador
 * y el momento en que termino la partida.
 *
 * <p>Patron State: representa el estado de fin de juego.</p>
 * <p>Principio S (SRP): solo almacena el resultado final de la partida.</p>
 */
public class GameOverState implements IGameState {

    /** Nombre del jugador ganador. */
    private final String winnerName;

    /** Razon por la que termino el juego. */
    private final String reason;

    /** Timestamp del momento en que termino la partida. */
    private final long endTimestamp;

    /**
     * Constructor del estado de fin de juego.
     *
     * @param winnerName nombre del jugador ganador
     * @param reason     razon de la victoria
     */
    public GameOverState(String winnerName, String reason) {
        this.winnerName   = winnerName;
        this.reason       = reason;
        this.endTimestamp = System.currentTimeMillis();
    }

    /**
     * No aplica ninguna logica ya que el juego termino.
     *
     * @param engine motor del juego (no se usa en este estado)
     */
    @Override
    public void update(GameEngine engine) {}

    /**
     * Indica que el juego no esta en curso.
     *
     * @return false siempre
     */
    @Override
    public boolean isRunning() { return false; }

    /**
     * Retorna el nombre descriptivo de este estado.
     *
     * @return "FIN DEL JUEGO"
     */
    @Override
    public String getStateName() { return "FIN DEL JUEGO"; }

    /**
     * Retorna el nombre del jugador ganador.
     *
     * @return nombre del ganador
     */
    public String getWinnerName() { return winnerName; }

    /**
     * Retorna la razon por la que termino el juego.
     *
     * @return razon de la victoria
     */
    public String getReason() { return reason; }

    /**
     * Retorna el timestamp del momento en que termino la partida.
     *
     * @return timestamp en milisegundos
     */
    public long getEndTimestamp() { return endTimestamp; }
}