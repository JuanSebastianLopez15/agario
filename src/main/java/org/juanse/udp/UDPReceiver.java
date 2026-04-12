package org.juanse.udp;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Hilo que escucha constantemente en un puerto UDP.
 * CAMBIO: el callback ahora incluye la dirección del remitente,
 * lo que permite al host saber desde qué IP llegó cada cliente.
 */
public class UDPReceiver extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private final INetworkListener listener;

    public UDPReceiver(int listenPort, INetworkListener listener) {
        this.listener = listener;
        try {
            this.socket  = new DatagramSocket(listenPort);
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
                socket.receive(packet);

                // Deserializar objeto
                ByteArrayInputStream bais =
                        new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object receivedObject  = ois.readObject();

                // Dirección real del remitente
                InetSocketAddress senderAddress =
                        new InetSocketAddress(packet.getAddress(), packet.getPort());

                if (listener != null) {
                    listener.onDataReceived(receivedObject, senderAddress);
                }

            } catch (Exception e) {
                if (running) {
                    System.err.println("Error al recibir paquete UDP: " + e.getMessage());
                }
            }
        }
    }

    public void stopReceiver() {
        running = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }

    /**
     * CAMBIO: el callback ahora recibe también la dirección del remitente.
     */
    public interface INetworkListener {
        void onDataReceived(Object data, InetSocketAddress senderAddress);
    }
}