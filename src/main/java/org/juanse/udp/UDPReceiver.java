package org.juanse.udp;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * Hilo receptor UDP que escucha constantemente en un puerto
 * y notifica al listener cuando llega un paquete.
 *
 * <p>Corre en segundo plano como un hilo independiente.</p>
 * <p>Principio S (SRP): solo recibe y deserializa paquetes UDP.</p>
 * <p>Principio D (DIP): notifica a través de la interfaz {@link INetworkListener}.</p>
 */
public class UDPReceiver extends Thread {

    /** Socket UDP para recibir paquetes. */
    private DatagramSocket socket;

    /** Indica si el receptor está activo. */
    private boolean running;

    /** Listener que recibe los objetos deserializados. */
    private final INetworkListener listener;

    /**
     * Constructor del receptor UDP.
     * Crea el socket y lo enlaza al puerto especificado.
     * Usa {@code setReuseAddress(true)} para permitir reutilizar
     * el puerto si quedó ocupado por una instancia anterior.
     *
     * @param listenPort puerto en el que escuchar
     * @param listener   callback que recibe los datos y la dirección del emisor
     */
    public UDPReceiver(int listenPort, INetworkListener listener) {
        this.listener = listener;
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(listenPort));
            this.running = true;
            System.out.println("Escuchando en puerto: " + listenPort);
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo abrir el puerto " + listenPort
                    + ". Cierra otras instancias del juego y vuelve a intentar.");
            e.printStackTrace();
        }
    }

    /**
     * Bucle principal del hilo receptor.
     * Espera paquetes UDP, los deserializa y notifica al listener
     * junto con la dirección IP y puerto del emisor.
     */
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
                Object receivedObject = ois.readObject();

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

    /**
     * Detiene el receptor cerrando el socket y terminando el hilo.
     * Al cerrar el socket se interrumpe el bloqueo de {@code socket.receive()}.
     */
    public void stopReceiver() {
        running = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }

    /**
     * Interfaz callback para notificar cuando llega un paquete UDP.
     * Patrón Observer — desacopla el receptor de quien procesa los datos.
     */
    public interface INetworkListener {

        /**
         * Se invoca cuando se recibe y deserializa un objeto por UDP.
         *
         * @param data          objeto deserializado recibido
         * @param senderAddress dirección IP y puerto del emisor
         */
        void onDataReceived(Object data, InetSocketAddress senderAddress);
    }
}