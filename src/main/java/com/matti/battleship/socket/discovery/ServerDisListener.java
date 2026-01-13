package com.matti.battleship.socket.discovery;

import com.matti.battleship.socket.config.EnvConfig;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerDisListener {

  public static String listen() throws Exception {
    try (DatagramSocket socket = new DatagramSocket(EnvConfig.getPort())) {

      byte[] buffer = new byte[256];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

      socket.receive(packet);

      // Jede empfangene UDP-Nachricht gilt als Server
      return packet.getAddress().getHostAddress();
    }
  }
}
