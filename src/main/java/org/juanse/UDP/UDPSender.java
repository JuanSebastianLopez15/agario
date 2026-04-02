package org.juanse.UDP;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * toma un objeto, puede ser del GameSnapshot
 * y lo convierte a una secuancia de bytes que se pasa a la direccion
 * ip y puerto del otro jugador
 */

public class UDPSender {

    private DatagramSocket socket;

    public UDPSender() {
        try {
            // Creamos un socket genérico para enviar datos
            this.socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convierte un objeto a bytes y lo envía por UDP.
     */
    public void sendObject(Object data, String targetIp, int targetPort) {
        try {
            // 1. Convertir el objeto a bytes (Serialización)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            byte[] buffer = baos.toByteArray();

            // 2. Preparar el paquete con la dirección de destino
            InetAddress address = InetAddress.getByName(targetIp);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, targetPort);

            // 3. Enviar el paquete
            socket.send(packet);

        } catch (Exception e) {
            System.err.println("Error al enviar el paquete UDP: " + e.getMessage());
        }
    }

    /**
     * cierra el socket que este activo en algun momento que se le solicite
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
