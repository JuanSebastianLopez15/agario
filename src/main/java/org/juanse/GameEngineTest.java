package org.juanse;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;

import java.util.Arrays;

/**
 * Prueba rápida de la lógica sin necesidad de UI ni red.
 * Ejecutar con: javac -d out -sourcepath src src/GameEngineTest.java && java -cp out GameEngineTest
 */
public class GameEngineTest implements IGameEventListener {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== TEST LÓGICA AGARIO ===\n");

        GameEngine engine = new GameEngine(1200, 800);
        GameEngineTest listener = new GameEngineTest();
        engine.addListener(listener);

        // Iniciar con 2 jugadores
        engine.startGame(Arrays.asList("Jugador1", "Jugador2"), 50.0);

        System.out.println("Jugadores iniciales:");
        engine.getPlayers().forEach(System.out::println);
        System.out.println("Pellets: " + engine.getPellets().size());
        System.out.println("Hazards: " + engine.getHazardBalls().size());
        System.out.println();

        // Simular 10 ticks
        for (int i = 0; i < 10; i++) {
            engine.update();
            Thread.sleep(50);
        }

        System.out.println("\nEstado tras 10 ticks:");
        engine.getPlayers().forEach(p ->
                System.out.println("  " + p + " | Puntos: " + engine.getScoreManager().getScore(p.getOwnerName()))
        );

        System.out.println("Tiempo restante: " + (engine.getRemainingTimeMs() / 1000) + "s");
        System.out.println("Snapshot creado: " + engine.createSnapshot().getGameStateName());
        System.out.println("\n=== FIN DEL TEST ===");
    }

    // ── Observer callbacks ────────────────────────────────────

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        System.out.println("[EVENTO] " + absorber.getOwnerName() + " absorbió a " + absorbed.getOwnerName());
    }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        System.out.println("[EVENTO] " + original.getOwnerName() + " se dividió en dos!");
    }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        System.out.println("[EVENTO] " + player.getOwnerName() + " comió un pellet → masa: "
                + String.format("%.1f", player.getMass()));
    }

    @Override
    public void onGameOver(String winnerName, String reason) {
        System.out.println("\n[FIN] Ganador: " + winnerName + " — " + reason);
    }
}

