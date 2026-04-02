package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Regla 5 – Victoria por tiempo o eliminación:
 * - Si queda solo un jugador vivo → gana inmediatamente.
 * - Si se acaba el tiempo → gana quien tenga mayor masa total acumulada.
 */
public class VictoryRule implements IGameRule {

    @Override
    public void apply(GameEngine engine) {
        if (engine.isGameOver()) return;

        Set<String> activePlayers = getActivePlayers(engine);

        // Condición 1: un solo jugador sobreviviente
        if (activePlayers.size() == 1) {
            String winner = activePlayers.iterator().next();
            engine.triggerGameOver(winner, "¡Eliminó a todos los rivales!");
            return;
        }

        // Condición 2: tiempo agotado
        if (engine.isTimeUp()) {
            String winner = getWinnerByMass(engine, activePlayers);
            engine.triggerGameOver(winner, "¡Ganó por mayor masa al finalizar el tiempo!");
        }
    }

    /**
     * Obtiene los nombres únicos de jugadores que aún tienen al menos una célula viva.
     */
    private Set<String> getActivePlayers(GameEngine engine) {
        return engine.getPlayers().stream()
                .filter(PlayerCell::isAlive)
                .map(PlayerCell::getOwnerName)
                .collect(Collectors.toSet());
    }

    /**
     * Suma la masa total de todas las células de cada jugador y devuelve el que más tiene.
     */
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

