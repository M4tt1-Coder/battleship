package com.matti.battleship.socket.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerDiscoveryResponder implements Runnable {

  private final int port;
  private final AtomicBoolean busy;
  private final String serverName;

  public ServerDiscoveryResponder(int port, AtomicBoolean busy, String serverName) {
    this.port = port;
    this.busy = busy;
    this.serverName = (serverName == null || serverName.isBlank()) ? "Battleship" : serverName;
  }

  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket(port)) {
      socket.setBroadcast(true);

      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);

      while (true) {
        socket.receive(packet);

        String msg = DiscoveryProtocol.str(packet.getData(), packet.getLength());
        if (!DiscoveryProtocol.DISCOVER.equals(msg)) {
          continue; // fremde Nachricht ignorieren
        }

        // Wenn busy: NICHT antworten -> verschwindet aus der Liste
        if (busy.get()) {
          continue;
        }

        // Unicast-Antwort direkt an den Client, der gefragt hat
        InetAddress clientAddr = packet.getAddress();
        int clientPort = packet.getPort();

        String reply = DiscoveryProtocol.HERE_PREFIX + " " + port + " " + serverName;
        byte[] out = DiscoveryProtocol.bytes(reply);

        DatagramPacket response = new DatagramPacket(out, out.length, clientAddr, clientPort);
        socket.send(response);
      }

    } catch (Exception e) {
      System.out.println("[DISCOVERY] Responder gestoppt: " + e.getMessage());
    }
  }
}
