package socket.discovery;

import socket.config.EnvConfig;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerDiscoveryBroadcaster implements Runnable {

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {

            socket.setBroadcast(true);
            byte[] data = EnvConfig.getDiscoveryMessage().getBytes();

            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("255.255.255.255"),
                    EnvConfig.getPort()
            );

            while (true) {
                socket.send(packet);
                Thread.sleep(2000);
            }

        } catch (Exception ignored) {}
    }
}

