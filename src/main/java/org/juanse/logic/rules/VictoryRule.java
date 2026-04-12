package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Regla 5 – Victoria por tiempo o eliminación.
 * CORRECCIÓN: la eliminación solo cuenta si ya hubo al menos 2 jugadores.
 * Así el host puede esperar clientes sin que el juego termine solo.
 */
public class VictoryRule implements IGameRule {

    @Override
    public void apply(GameEngine engine) {
        if (engine.isGameOver()) return;

        Set<String> activePlayers = getActivePlayers(engine);

        // Necesitamos saber cuántos jugadores han sido registrados en total
        int totalRegistered = engine.getScoreManager().getAllScores().size();

        // Condición 1: eliminación — solo aplica si ya había al menos 2 jugadores
        if (totalRegistered >= 2 && activePlayers.size() == 1) {
            String winner = activePlayers.iterator().next();
            engine.triggerGameOver(winner, "¡Eliminó a todos los rivales!");
            return;
        }

        // Condición 2: tiempo agotado — aplica siempre que haya al menos 1 jugador
        if (!activePlayers.isEmpty() && engine.isTimeUp()) {
            String winner = getWinnerByMass(engine, activePlayers);
            engine.triggerGameOver(winner, "¡Ganó por mayor masa al finalizar el tiempo!");
        }
    }

    private Set<String> getActivePlayers(GameEngine engine) {
        return engine.getPlayers().stream()
                .filter(PlayerCell::isAlive)
                .map(PlayerCell::getOwnerName)
                .collect(Collectors.toSet());
    }

    private String getWinnerByMass(GameEngine engine, Set<String> activePlayers) {
        Map<String, Double> totalMass = new HashMap<>();
        for (String name : activePlayers) totalMass.put(name, 0.0);

        for (PlayerCell cell : engine.getPlayers()) {
            if (cell.isAlive() && totalMass.containsKey(cell.getOwnerName())) {
                totalMass.merge(cell.getOwnerName(), cell.getMass(), Double::sum);
            }
        }

        return totalMass.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Empate");
    }
}