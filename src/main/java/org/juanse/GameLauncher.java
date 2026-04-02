package org.juanse;

import org.juanse.logic.engine.GameEngine;
import org.juanse.logic.engine.GameSnapshot;

import java.util.List;

//public class GameLauncher {
//    public static void main(String[] args) {
//
//        GameEngine engine = new GameEngine(1200, 800);
//
//        boolean isHost = true; // o false dependiendo del caso
//        engine.setHost(isHost);
//
//        if (isHost) {
//            engine.startGame(List.of("Jugador1", "Jugador2"), 50.0);
//        }
//
//        while (true) {
//
//            engine.update();
//
//            if (isHost) {
//                GameSnapshot snapshot = engine.createSnapshot();
//                // enviar por UDP
//            } else {
//                // recibir snapshot
//                // engine.applySnapshot(snapshot);
//            }
//
//            try {
//                Thread.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
//parte de Cris Arriba

////////////////////////
//prueba de el crack de lopez abajo

import org.juanse.UDP.UDPReceiver;
import org.juanse.UDP.UDPSender;
import org.juanse.UDP.MouseInputDTO;

import java.util.List;
import java.util.Scanner;

public class GameLauncher {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("¿Eres el Host? (true/false): ");
        boolean isHost = scanner.nextBoolean();
        scanner.nextLine();

        // 2. CONFIGURACIÓN DE RED (Automática basada en isHost)
        String targetIp = "127.0.0.1"; // "localhost" para probar en la misma PC.
        int listenPort;
        int targetPort;

        if (isHost) {
            listenPort = 5000;
            targetPort = 5001;
            System.out.println("Iniciando como HOST. Escuchando en " + listenPort + " | Enviando a " + targetPort);
        } else {
            listenPort = 5001;
            targetPort = 5000;
            System.out.println("Iniciando como CLIENTE. Escuchando en " + listenPort + " | Enviando a " + targetPort);

        }

        GameEngine engine = new GameEngine(1200, 800);
        engine.setHost(isHost);
        UDPSender sender = new UDPSender();

        // PREPARAR LA "ANTENA" RECEPTORA (El Hilo)
        UDPReceiver receiver = new UDPReceiver(listenPort, new UDPReceiver.INetworkListener() {
            @Override
            public void onDataReceived(Object data) {
                if (isHost && data instanceof MouseInputDTO) {
                    // EL HOST RECIBE: Las coordenadas del mouse del cliente
                    MouseInputDTO mouseData = (MouseInputDTO) data;
                    // TODO: Aquí luego conectaremos el mouse con la lógica de movimiento
                    // System.out.println("Host recibió movimiento de: " + mouseData.getPlayerName());
                }
                else if (!isHost && data instanceof GameSnapshot) {
                    // EL CLIENTE RECIBE: La foto del estado del juego
                    GameSnapshot snapshot = (GameSnapshot) data;
                    engine.applySnapshot(snapshot); // ¡Actualiza su lógica local inmediatamente!
                }
            }
        });
        receiver.start(); // ¡Arrancamos el hilo para que escuche en segundo plano!

        // ARRANCAR EL JUEGO (Solo el Host crea las células y la comida)
        if (isHost) {
            engine.startGame(List.of("Jugador1", "Jugador2"), 50.0);
        }

        // 6. BUCLE PRINCIPAL (Game Loop)
        while (true) {
            // El Host ejecuta las reglas del juego (colisiones, etc)
            engine.update();

            // Lógica de envío constante
            if (isHost) {
                // El Host toma la "foto" y se la manda al cliente
                GameSnapshot snapshot = engine.createSnapshot();
                sender.sendObject(snapshot, targetIp, targetPort);
            } else {
                // El Cliente lee su mouse y se lo manda al Host
                // (Como aún no hay interfaz, mandamos datos falsos estáticos para probar)
                MouseInputDTO myMouse = new MouseInputDTO("Jugador2", 500.0, 500.0);
                sender.sendObject(myMouse, targetIp, targetPort);
            }

            try {
                Thread.sleep(50); // Pausa de 50ms (juego a 20 FPS aprox) para no saturar la red
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}