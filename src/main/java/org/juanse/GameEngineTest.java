package org.juanse;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.entities.Pellet;
import org.juanse.logic.entities.PlayerCell;
import org.juanse.logic.observers.IGameEventListener;

import java.util.Arrays;

public class GameEngineTest implements IGameEventListener {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== TEST LÓGICA AGARIO ===\n");

        GameEngineTest listener = new GameEngineTest();

        // ── TEST 1: Absorción ─────────────────────────────────
        System.out.println("--- TEST 1: Absorción por tamaño ---");

        GameEngine engine = new GameEngine(1200, 800);
        engine.addListener(listener);
        engine.startGame(Arrays.asList("Jugador1", "Jugador2"), 50.0);

        PlayerCell jugador1 = engine.getPlayers().get(0);
        PlayerCell jugador2 = engine.getPlayers().get(1);

        jugador1.setX(100); jugador1.setY(100);
        jugador2.setX(105); jugador2.setY(100);
        jugador1.growBy(20); // jugador1: 70 masa, jugador2: 50 -> ventaja del 40%

        System.out.println("Antes -> " + jugador1 + " | " + jugador2);
        engine.update();
        System.out.println("Jugadores vivos: " + engine.getPlayers().size());
        System.out.println("Puntaje Jugador1: " + engine.getScoreManager().getScore("Jugador1"));

        // ── TEST 2: Comer un pellet ───────────────────────────
        System.out.println("\n--- TEST 2: Comer un pellet ---");

        GameEngine engine2 = new GameEngine(1200, 800);
        engine2.addListener(listener);
        engine2.startGame(Arrays.asList("Jugador1", "Jugador2"), 50.0);

        PlayerCell p1 = engine2.getPlayers().get(0);
        Pellet primerPellet = engine2.getPellets().get(0);

        p1.setX(primerPellet.getX());
        p1.setY(primerPellet.getY());

        System.out.println("Masa antes de comer pellet: " + p1.getMass());
        engine2.update();
        System.out.println("Masa despues de comer pellet: " + p1.getMass());

        // ── TEST 3: División por HazardBall ───────────────────
        System.out.println("\n--- TEST 3: Division por HazardBall ---");

        GameEngine engine3 = new GameEngine(1200, 800);
        engine3.addListener(listener);
        engine3.startGame(Arrays.asList("Jugador1", "Jugador2"), 100.0);

        PlayerCell p1e3 = engine3.getPlayers().get(0);
        p1e3.setX(engine3.getHazardBalls().get(0).getX());
        p1e3.setY(engine3.getHazardBalls().get(0).getY());

        System.out.println("Celulas antes del split: " + engine3.getPlayers().size());
        System.out.println("Masa antes del split: " + p1e3.getMass());
        engine3.update();
        System.out.println("Celulas despues del split: " + engine3.getPlayers().size());

        // ── TEST 4: Límites del mapa ──────────────────────────
        System.out.println("\n--- TEST 4: Paredes invisibles ---");

        GameEngine engine4 = new GameEngine(1200, 800);
        engine4.addListener(listener);
        engine4.startGame(Arrays.asList("Jugador1", "Jugador2"), 50.0);

        PlayerCell p1e4 = engine4.getPlayers().get(0);
        p1e4.setX(-999);
        p1e4.setY(-999);

        System.out.println("Posicion antes: x=" + p1e4.getX() + " y=" + p1e4.getY());
        engine4.update();
        System.out.println("Posicion despues: x=" + p1e4.getX() + " y=" + p1e4.getY());

        System.out.println("\n=== FIN DEL TEST ===");
    }

    @Override
    public void onAbsorption(PlayerCell absorber, PlayerCell absorbed) {
        System.out.println("[EVENTO] " + absorber.getOwnerName()
                + " absorbio a " + absorbed.getOwnerName()
                + " -> nueva masa: " + String.format("%.1f", absorber.getMass()));
    }

    @Override
    public void onSplit(PlayerCell original, PlayerCell newCell) {
        System.out.println("[EVENTO] " + original.getOwnerName()
                + " se dividio -> masa de cada parte: " + String.format("%.1f", original.getMass()));
    }

    @Override
    public void onPelletEaten(PlayerCell player, Pellet pellet) {
        System.out.println("[EVENTO] " + player.getOwnerName()
                + " comio un pellet -> masa: " + String.format("%.1f", player.getMass()));
    }

    @Override
    public void onGameOver(String winnerName, String reason) {
        System.out.println("[FIN] Ganador: " + winnerName + " - " + reason);
    }
}


