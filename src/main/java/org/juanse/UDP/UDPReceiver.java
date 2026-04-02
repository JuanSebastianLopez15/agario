package org.juanse.UDP;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Hilo que corre en segundo plano escuchando constantemente
 */
public class UDPReceiver extends Thread {

    private DatagramSocket socket;
    private boolean running;

    // Interfaz para avisarle al juego cuando llega algo(se le pasa datos)
    private final INetworkListener listener;

    public UDPReceiver(int listenPort, INetworkListener listener) {
        this.listener = listener;
        try {
            // Escuchamos en un puerto específico
            this.socket = new DatagramSocket(listenPort);
            this.running = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65535];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // El hilo se queda esperando aquí hasta que llegue un paquete
                socket.receive(packet);

                // 1. Convertir los bytes de vuelta a un Objeto (Deserialización)
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object receivedObject = ois.readObject();

                // 2. Avisarle al listener (el GameLauncher) que llegó un dato
                if (listener != null) {
                    listener.onDataReceived(receivedObject);
                }

            } catch (Exception e) {
                if (running) {
                    System.err.println("Error al recibir el paquete UDP: " + e.getMessage());
                }
            }
        }
    }

    public void stopReceiver() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close(); // Al cerrar el socket, se rompe el bloqueo de socket.receive()
        }
    }

    // Interfaz interna para la comunicación (Callback)
    public interface INetworkListener {
        void onDataReceived(Object data);
    }
}
