package org.juanse;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.engine.GameSnapshot;

import java.util.List;

public class GameLauncher {
    public static void main(String[] args) {

        GameEngine engine = new GameEngine(1200, 800);

        boolean isHost = true; // o false dependiendo del caso
        engine.setHost(isHost);

        if (isHost) {
            engine.startGame(List.of("Jugador1", "Jugador2"), 50.0);
        }

        while (true) {

            engine.update();

            if (isHost) {
                GameSnapshot snapshot = engine.createSnapshot();
                // enviar por UDP
            } else {
                // recibir snapshot
                // engine.applySnapshot(snapshot);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
