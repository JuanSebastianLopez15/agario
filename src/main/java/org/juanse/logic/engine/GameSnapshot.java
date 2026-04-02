package org.juanse.logic.engine;

import org.juanse.logic.entities.HazardBall;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

import java.util.List;
import java.util.Map;

/**
 * Snapshot inmutable del estado del juego en un instante de tiempo.
 *
 * Principio S (SRP): su única responsabilidad es ser un DTO (Data Transfer Object)
 * que el componente de red puede serializar y enviar por UDP sin conocer
 * la lógica interna del GameEngine.
 */
public class GameSnapshot {

    private final List<PlayerCell> players;
    private final List<Pellet> pellets;
    private final List<HazardBall> hazardBalls;
    private final Map<String, Integer> scores;
    private final long remainingTimeMs;
    private final String gameStateName;
    private final long timestamp;

    public GameSnapshot(
            List<PlayerCell> players,
            List<Pellet> pellets,
            List<HazardBall> hazardBalls,
            Map<String, Integer> scores,
            long remainingTimeMs,
            String gameStateName
    ) {
        this.players = players;
        this.pellets = pellets;
        this.hazardBalls = hazardBalls;
        this.scores = scores;
        this.remainingTimeMs = remainingTimeMs;
        this.gameStateName = gameStateName;
        this.timestamp = System.currentTimeMillis();
    }

    // ── Getters ──────────────────────────────────────────────

    public List<PlayerCell> getPlayers() { return players; }
    public List<Pellet> getPellets() { return pellets; }
    public List<HazardBall> getHazardBalls() { return hazardBalls; }
    public Map<String, Integer> getScores() { return scores; }
    public long getRemainingTimeMs() { return remainingTimeMs; }
    public String getGameStateName() { return gameStateName; }
    public long getTimestamp() { return timestamp; }
}

