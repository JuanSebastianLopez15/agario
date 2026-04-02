package org.juanse.logic.engine;

import org.juanse.logic.dto.HazardDTO;
import org.juanse.logic.dto.PelletDTO;
import org.juanse.logic.dto.PlayerDTO;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Snapshot inmutable del estado del juego en un instante de tiempo.
 */
//Combierte un objeto a bytes, los sockets UDP solo entienden arreglos de bytes
public class GameSnapshot implements Serializable{

    private final List<PlayerDTO> players;
    private final List<PelletDTO> pellets;
    private final List<HazardDTO> hazards;
    private final Map<String, Integer> scores;
    private final long remainingTimeMs;
    private final String gameStateName;
    private final long timestamp;

    public GameSnapshot(
            List<PlayerDTO> players,
            List<PelletDTO> pellets,
            List<HazardDTO> hazards,
            Map<String, Integer> scores,
            long remainingTimeMs,
            String gameStateName
    ) {
        this.players = players;
        this.pellets = pellets;
        this.hazards = hazards;
        this.scores = scores;
        this.remainingTimeMs = remainingTimeMs;
        this.gameStateName = gameStateName;
        this.timestamp = System.currentTimeMillis();
    }

    public List<PlayerDTO> getPlayers() { return players; }
    public List<PelletDTO> getPellets() { return pellets; }
    public List<HazardDTO> getHazards() { return hazards; }
    public Map<String, Integer> getScores() { return scores; }
    public long getRemainingTimeMs() { return remainingTimeMs; }
    public String getGameStateName() { return gameStateName; }
    public long getTimestamp() { return timestamp; }
}

