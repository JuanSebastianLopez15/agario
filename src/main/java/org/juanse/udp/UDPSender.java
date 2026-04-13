package org.juanse.udp;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Emisor UDP que serializa objetos Java y los envía como paquetes
 * a una dirección IP y puerto destino.
 *
 * <p>Puede enviar cualquier objeto serializable, como {@code GameSnapshot}
 * o {@code MouseInputDTO}.</p>
 * <p>Principio S (SRP): solo serializa y envía paquetes UDP.</p>
 */
public class UDPSender {

    /** Socket UDP genérico para enviar paquetes. */
    private DatagramSocket socket;

    /**
     * Constructor del emisor UDP.
     * Crea un socket genérico listo para enviar datos.
     */
    public UDPSender() {
        try {
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Serializa un objeto Java y lo envía como paquete UDP
     * a la dirección IP y puerto especificados.
     *
     * @param data       objeto serializable a enviar (ej: {@code GameSnapshot}, {@code MouseInputDTO})
     * @param targetIp   dirección IP del destinatario
     * @param targetPort puerto del destinatario
     */
    public void sendObject(Object data, String targetIp, int targetPort) {
        try {
            // Serializar el objeto a bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();

            // Preparar y enviar el paquete UDP
            InetAddress address = InetAddress.getByName(targetIp);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, targetPort);
            socket.send(packet);

        } catch (Exception e) {
            System.err.println("Error al enviar el paquete UDP: " + e.getMessage());
        }
    }

    /**
     * Cierra el socket UDP liberando el recurso de red.
     * Debe llamarse cuando el juego termina o el jugador se desconecta.
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}