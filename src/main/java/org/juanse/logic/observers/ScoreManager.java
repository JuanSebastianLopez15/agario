package org.juanse.logic.observers;

import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementacion del patron Observer que lleva el registro del puntaje.
 *
 * <p>Principio S (SRP): solo se ocupa del conteo de puntos.
 * No dibuja, no envia paquetes; solo guarda datos.</p>
 *
 * <p>Sistema de puntuacion:</p>
 * <ul>
 *   <li>+1 punto por comer un pellet</li>
 *   <li>+10 puntos por absorber a otro jugador</li>
 *   <li>+50 puntos de bonus por ganar la partida</li>
 * </ul>
 */
public class ScoreManager implements IGameEventListener {

    /** Mapa de puntajes por nombre de jugador. */
    private final Map<String, Integer> scores = new HashMap<>();

    /** Mapa de absorciones realizadas por cada jugador. */
    private final Map<String, Integer> absorptions = new HashMap<>();

    // ── Observer callbacks ────────────────────────────────────

    /**
     * Suma 10 puntos al jugador que realizo la absorcion
     * y registra la absorcion en el contador.
     *
     * @param absorber celula que realizo la absorcion
     * @param absorbed celula absorbida
     */
    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        addScore(absorber.getOwnerName(), 10);
        absorptions.merge(absorber.getOwnerName(), 1, Integer::sum);
    }

    /**
     * El split no otorga ni quita puntos por si solo.
     *
     * @param original celula original que se dividio
     * @param newCell  nueva celula generada
     */
    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {}

    /**
     * Suma 1 punto al jugador que comio el pellet.
     *
     * @param player celula que comio el pellet
     * @param pellet pellet consumido
     */
    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        addScore(player.getOwnerName(), 1);
    }

    /**
     * Suma 50 puntos de bonus al jugador ganador.
     *
     * @param winnerName nombre del jugador ganador
     * @param reason     razon de la victoria
     */
    @Override
    public void onGameOver(String winnerName, String reason) {
        addScore(winnerName, 50);
    }

    // ── API publica para la UI y la red ──────────────────────

    /**
     * Registra un jugador en el sistema de puntaje.
     * Debe llamarse al inicio de la partida para cada jugador.
     *
     * @param playerName nombre del jugador a registrar
     */
    public void registerPlayer(String playerName) {
        scores.putIfAbsent(playerName, 0);
        absorptions.putIfAbsent(playerName, 0);
    }

    /**
     * Retorna el puntaje actual de un jugador.
     *
     * @param playerName nombre del jugador
     * @return puntaje actual, 0 si no esta registrado
     */
    public int getScore(String playerName) {
        return scores.getOrDefault(playerName, 0);
    }

    /**
     * Reemplaza todos los puntajes con los valores recibidos del host.
     * Usado al aplicar un snapshot en el cliente.
     *
     * @param newScores nuevo mapa de puntajes
     */
    public void setScores(Map<String, Integer> newScores) {
        scores.clear();
        scores.putAll(newScores);
    }

    /**
     * Retorna el numero de absorciones realizadas por un jugador.
     *
     * @param playerName nombre del jugador
     * @return numero de absorciones, 0 si no esta registrado
     */
    public int getAbsorptions(String playerName) {
        return absorptions.getOrDefault(playerName, 0);
    }

    /**
     * Retorna una copia inmutable del mapa de puntajes.
     *
     * @return mapa nombre -> puntaje (inmutable)
     */
    public Map<String, Integer> getAllScores() {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * Retorna el nombre del jugador con mayor puntaje.
     *
     * @return nombre del lider, o "-" si no hay jugadores registrados
     */
    public String getLeader() {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    // ── Privado ───────────────────────────────────────────────

    /**
     * Suma puntos al puntaje de un jugador.
     *
     * @param playerName nombre del jugador
     * @param points     puntos a sumar
     */
    private void addScore(String playerName, int points) {
        scores.merge(playerName, points, Integer::sum);
    }
}