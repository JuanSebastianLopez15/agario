package org.juanse.udp;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPReceiver extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private final INetworkListener listener;

    public UDPReceiver(int listenPort, INetworkListener listener) {
        this.listener = listener;
        try {
            socket = new DatagramSocket(null); // sin bind inmediato
            socket.setReuseAddress(true);      // permite reusar el puerto si quedó ocupado
            socket.bind(new InetSocketAddress(listenPort));
            this.running = true;
            System.out.println("Escuchando en puerto: " + listenPort);
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo abrir el puerto " + listenPort
                    + ". Cierra otras instancias del juego y vuelve a intentar.");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (socket == null) return;
        byte[] buffer = new byte[65535];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteArrayInputStream bais =
                        new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object receivedObject  = ois.readObject();

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

    public interface INetworkListener {
        void onDataReceived(Object data, InetSocketAddress senderAddress);
    }
}