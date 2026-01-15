package com.matti.battleship.socket.discovery;

import java.net.*;
import java.util.*;

public class ClientDiscoveryScanner {

  private final int port;

  public ClientDiscoveryScanner(int port) {
    this.port = port;
  }

  public List<DiscoveredServer> discover(int timeoutMillis) throws Exception {
    Map<String, DiscoveredServer> found = new HashMap<>();

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);

      // DISCOVER broadcasten
      byte[] data = DiscoveryProtocol.bytes(DiscoveryProtocol.DISCOVER);
      DatagramPacket packet =
          new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
      socket.send(packet);

      // Antworten einsammeln
      long end = System.currentTimeMillis() + timeoutMillis;
      socket.setSoTimeout(Math.max(50, timeoutMillis / 5));

      while (System.currentTimeMillis() < end) {
        try {
          byte[] buf = new byte[256];
          DatagramPacket resp = new DatagramPacket(buf, buf.length);
          socket.receive(resp);

          String msg = DiscoveryProtocol.str(resp.getData(), resp.getLength());
          // Erwartet: BS_HERE_V1 <port> <name...>
          if (!msg.startsWith(DiscoveryProtocol.HERE_PREFIX)) continue;

          String[] parts = msg.split("\\s+");
          if (parts.length < 3) continue;

          int tcpPort;
          try {
            tcpPort = Integer.parseInt(parts[1]);
          } catch (NumberFormatException nfe) {
            continue;
          }

          String name = joinFrom(parts, 2);
          String host = resp.getAddress().getHostAddress();

          found.put(host, new DiscoveredServer(host, tcpPort, name, System.currentTimeMillis()));

        } catch (SocketTimeoutException ignored) {
        }
      }
    }

    List<DiscoveredServer> list = new ArrayList<>(found.values());
    list.sort(Comparator.comparing(DiscoveredServer::host));
    return list;
  }

  private String joinFrom(String[] parts, int start) {
    StringBuilder b = new StringBuilder();
    for (int i = start; i < parts.length; i++) {
      if (i > start) b.append(' ');
      b.append(parts[i]);
    }
    return b.toString();
  }
}
