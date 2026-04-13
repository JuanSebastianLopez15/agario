package org.juanse.logic.engine;

import org.juanse.logic.dto.HazardDTO;
import org.juanse.logic.dto.PelletDTO;
import org.juanse.logic.dto.PlayerDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Snapshot inmutable del estado del juego en un instante de tiempo.
 * Se serializa y transmite por UDP del host a los clientes
 * para mantener el juego sincronizado.
 *
 * <p>Implementa {@link Serializable} para convertirse en bytes
 * y ser transportado a traves de sockets UDP.</p>
 * <p>Principio S (SRP): solo almacena el estado del juego en un momento dado.</p>
 */
public class GameSnapshot implements Serializable {

    /** Lista de DTOs de las celulas activas en el juego. */
    private final List<PlayerDTO> players;

    /** Lista de DTOs de los pellets activos en el mapa. */
    private final List<PelletDTO> pellets;

    /** Lista de DTOs de las HazardBalls activas en el mapa. */
    private final List<HazardDTO> hazards;

    /** Mapa de puntajes por nombre de jugador. */
    private final Map<String, Integer> scores;

    /** Tiempo restante de la partida en milisegundos. */
    private final long remainingTimeMs;

    /** Nombre del estado actual del juego (ej: "EN JUEGO", "FIN DEL JUEGO"). */
    private final String gameStateName;

    /** Timestamp del momento en que se creo el snapshot. */
    private final long timestamp;

    /**
     * Constructor del snapshot del juego.
     *
     * @param players        lista de DTOs de celulas activas
     * @param pellets        lista de DTOs de pellets activos
     * @param hazards        lista de DTOs de HazardBalls activas
     * @param scores         mapa de puntajes por jugador
     * @param remainingTimeMs tiempo restante de la partida en ms
     * @param gameStateName  nombre del estado actual del juego
     */
    public GameSnapshot(
            List<PlayerDTO> players,
            List<PelletDTO> pellets,
            List<HazardDTO> hazards,
            Map<String, Integer> scores,
            long remainingTimeMs,
            String gameStateName
    ) {
        this.players          = players;
        this.pellets          = pellets;
        this.hazards          = hazards;
        this.scores           = scores;
        this.remainingTimeMs  = remainingTimeMs;
        this.gameStateName    = gameStateName;
        this.timestamp        = System.currentTimeMillis();
    }

    /**
     * Retorna la lista de DTOs de las celulas activas.
     *
     * @return lista de PlayerDTO
     */
    public List<PlayerDTO> getPlayers() { return players; }

    /**
     * Retorna la lista de DTOs de los pellets activos.
     *
     * @return lista de PelletDTO
     */
    public List<PelletDTO> getPellets() { return pellets; }

    /**
     * Retorna la lista de DTOs de las HazardBalls activas.
     *
     * @return lista de HazardDTO
     */
    public List<HazardDTO> getHazards() { return hazards; }

    /**
     * Retorna el mapa de puntajes de los jugadores.
     *
     * @return mapa nombre -> puntaje
     */
    public Map<String, Integer> getScores() { return scores; }

    /**
     * Retorna el tiempo restante de la partida en milisegundos.
     *
     * @return tiempo restante en ms
     */
    public long getRemainingTimeMs() { return remainingTimeMs; }

    /**
     * Retorna el nombre del estado actual del juego.
     *
     * @return nombre del estado (ej: "EN JUEGO", "FIN DEL JUEGO")
     */
    public String getGameStateName() { return gameStateName; }

    /**
     * Retorna el timestamp del momento en que se creo el snapshot.
     *
     * @return timestamp en milisegundos
     */
    public long getTimestamp() { return timestamp; }
}