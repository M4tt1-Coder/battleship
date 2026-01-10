package com.matti.battleship.socket.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import socket.config.EnvConfig;

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
