package socket.discovery;

import socket.config.EnvConfig;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerDiscoveryListener {

    public static String listen() throws Exception {
        try (DatagramSocket socket = new DatagramSocket(EnvConfig.getPort())) {

            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength());

            if (msg.equals(EnvConfig.getDiscoveryMessage())) {
                return packet.getAddress().getHostAddress();
            }
        }
        return null;
    }
}
