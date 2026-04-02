package org.juanse.logic.observers;

import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del Observer que lleva el registro del puntaje.
 *
 * Principio S (SRP): solo se ocupa del conteo de puntos.
 * No dibuja, no envía paquetes; solo guarda datos.
 *
 * Sistema de puntuación:
 *  +10 puntos  → por absorber a otro jugador
 *  +1  punto   → por comer un pellet
 *  +50 puntos  → por sobrevivir hasta el final (aplicado por el GameEngine)
 */
public class ScoreManager implements IGameEventListener {

    private final Map<String, Integer> scores = new HashMap<>();
    private final Map<String, Integer> absorptions = new HashMap<>();

    // ── Observer callbacks ────────────────────────────────────

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        addScore(absorber.getOwnerName(), 10);
        absorptions.merge(absorber.getOwnerName(), 1, Integer::sum);
    }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        // El split no otorga ni quita puntos por sí solo
    }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        addScore(player.getOwnerName(), 1);
    }

    @Override
    public void onGameOver(String winnerName, String reason) {
        // Bonus por ganar
        addScore(winnerName, 50);
    }

    // ── API pública para la UI y la red ──────────────────────

    /** Registra un jugador en el sistema de puntaje (llamar al inicio). */
    public void registerPlayer(String playerName) {
        scores.putIfAbsent(playerName, 0);
        absorptions.putIfAbsent(playerName, 0);
    }

    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

    public void setScores(Map<String, Integer> newScores) {
        scores.clear();
        scores.putAll(newScores);
    }

    public int getAbsorptions(String playerName) {
        return absorptions.getOrDefault(playerName, 0);
    }

    /** Devuelve copia inmutable del mapa de puntajes. */
    public Map<String, Integer> getAllScores() {
        return Collections.unmodifiableMap(scores);
    }

    /** Retorna el nombre del jugador con mayor puntaje. */
    public String getLeader() {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("—");
    }

    // ── Privado ───────────────────────────────────────────────

    private void addScore(String playerName, int points) {
        scores.merge(playerName, points, Integer::sum);
    }
}

