package org.juanse.logic.observers;

import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;

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
 * <li>+1 punto por comer un pellet</li>
 * <li>+X puntos (equivalente a la masa absorbida) por comer a otro jugador</li>
 * <li>+0 puntos si las celulas absorbidas pertenecen al mismo jugador (fusion)</li>
 * </ul>
 */
public class ScoreManager implements IGameEventListener {

    /** Mapa de puntajes por nombre de jugador. */
    private final Map<String, Integer> scores = new HashMap<>();

    // ── API publica para la UI y la red ──────────────────────

    /**
     * Registra un jugador en el sistema de puntaje con 0 puntos iniciales.
     * Debe llamarse al inicio de la partida para cada jugador.
     *
     * @param playerName nombre del jugador a registrar
     */
    public void registerPlayer(String playerName) {
        scores.putIfAbsent(playerName, 0);
    }

    /**
     * Retorna el mapa completo de puntajes.
     *
     * @return mapa nombre -> puntaje
     */
    public Map<String, Integer> getAllScores() {
        return scores;
    }

    /**
     * Reemplaza todos los puntajes actuales con los valores recibidos.
     * Usado principalmente al aplicar un snapshot de red en el cliente.
     *
     * @param newScores nuevo mapa de puntajes
     */
    public void setScores(Map<String, Integer> newScores) {
        this.scores.clear();
        this.scores.putAll(newScores);
    }

    /**
     * Calcula y retorna el nombre del jugador con mayor puntaje.
     *
     * @return nombre del lider, o "Nadie" si no hay jugadores o puntajes validos
     */
    public String getLeader() {
        String leader = "Nadie";
        int maxScore = -1;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                leader = entry.getKey();
            }
        }
        return leader;
    }

    // ── Observer callbacks ────────────────────────────────────

    /**
     * Evalua una colision de absorcion. Si las celulas pertenecen a diferentes
     * jugadores, suma puntos al ganador. Si son del mismo jugador (fusion), lo ignora.
     *
     * @param absorber celula que realizo la absorcion
     * @param absorbed celula absorbida
     */
    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        // Validacion: Si la celula que absorbe y la absorbida son del mismo dueño (fusion),
        // ignoramos el evento y no damos puntos.
        if (absorber.getOwnerName().equals(absorbed.getOwnerName())) {
            return;
        }

        // Si es otro jugador, sumamos el equivalente a la masa del jugador absorbido.
        int currentScore = scores.getOrDefault(absorber.getOwnerName(), 0);
        int pointsEarned = (int) absorbed.getMass();
        scores.put(absorber.getOwnerName(), currentScore + pointsEarned);
    }

    /**
     * Suma 1 punto al jugador que comio el pellet.
     *
     * @param player celula que comio el pellet
     * @param pellet pellet consumido
     */
    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        int currentScore = scores.getOrDefault(player.getOwnerName(), 0);
        scores.put(player.getOwnerName(), currentScore + 1);
    }

    /**
     * El split/division no otorga ni quita puntos por si solo.
     *
     * @param original celula original que se dividio
     * @param newCell  nueva celula generada
     */
    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        // Al dividirse con la tecla de espacio/split no se alteran los puntos
    }

    /**
     * Logica a ejecutar al finalizar la partida.
     * Actualmente manejada directamente por el motor o la interfaz.
     *
     * @param winnerName nombre del jugador ganador
     * @param reason     razon de la victoria
     */
    @Override
    public void onGameOver(String winnerName, String reason) {
        // Vacio intencionalmente
    }
}