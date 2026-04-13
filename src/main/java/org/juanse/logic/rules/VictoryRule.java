package org.juanse.logic.rules;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.PlayerCell;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Regla de victoria por tiempo o eliminacion.
 * La eliminacion solo cuenta si ya hubo al menos 2 jugadores registrados,
 * evitando que el juego termine antes de que los clientes se conecten.
 *
 * <p>Condiciones de victoria:</p>
 * <ul>
 *   <li>Eliminacion: queda un solo jugador vivo y ya habia al menos 2 registrados.</li>
 *   <li>Tiempo: se agota el tiempo y gana quien tenga mayor masa total acumulada.</li>
 * </ul>
 *
 * <p>Principio S (SRP): solo aplica la logica de victoria.</p>
 * <p>Principio O (OCP): implementa {@link IGameRule} sin modificar el motor.</p>
 */
public class VictoryRule implements IGameRule {

    /**
     * Aplica la regla de victoria en cada tick del juego.
     * Verifica si se cumplen las condiciones de eliminacion o tiempo agotado.
     *
     * @param engine motor del juego con acceso al estado completo
     */
    @Override
    public void apply(GameEngine engine) {
        if (engine.isGameOver()) return;

        Set<String> activePlayers  = getActivePlayers(engine);
        int         totalRegistered = engine.getScoreManager().getAllScores().size();

        // Condicion 1: eliminacion — solo aplica si ya habia al menos 2 jugadores
        if (totalRegistered >= 2 && activePlayers.size() == 1) {
            String winner = activePlayers.iterator().next();
            engine.triggerGameOver(winner, "Elimino a todos los rivales!");
            return;
        }

        // Condicion 2: tiempo agotado — aplica siempre que haya al menos 1 jugador
        if (!activePlayers.isEmpty() && engine.isTimeUp()) {
            String winner = getWinnerByMass(engine, activePlayers);
            engine.triggerGameOver(winner, "Gano por mayor masa al finalizar el tiempo!");
        }
    }

    /**
     * Obtiene los nombres unicos de jugadores que aun tienen al menos una celula viva.
     *
     * @param engine motor del juego
     * @return conjunto de nombres de jugadores activos
     */
    private Set<String> getActivePlayers(GameEngine engine) {
        return engine.getPlayers().stream()
                .filter(PlayerCell::isAlive)
                .map(PlayerCell::getOwnerName)
                .collect(Collectors.toSet());
    }

    /**
     * Suma la masa total de todas las celulas de cada jugador activo
     * y retorna el nombre del jugador con mayor masa.
     *
     * @param engine        motor del juego
     * @param activePlayers conjunto de jugadores activos
     * @return nombre del jugador con mayor masa, o "Empate" si no hay jugadores
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