package com.matti.battleship.socket.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responds to UDP broadcast discovery requests for Battleship servers.
 *
 * <p>This class listens for DISCOVER messages and replies with a HERE response that contains the
 * TCP game port and a human-readable server name.
 *
 * <p>LOGIC-IMPORTANT: If the server is already connected to a client (busy == true), discovery
 * requests are ignored so the server does not appear in the client's server list anymore.
 *
 * @author WoFabian
 */
public class ServerDiscoveryResponder {

  /** UDP port used for discovery. In this project it is the same as the TCP game port. */
  private final int port;

  /**
   * Indicates whether this server is currently busy (already has a connected client). If busy ==
   * true, discovery requests will be ignored.
   */
  private final AtomicBoolean busy;

  /** Human readable server name that will be returned to the client. */
  private final String serverName;

  private volatile boolean running = true;

  /**
   * Creates a new discovery responder.
   *
   * @param port the UDP port to listen on
   * @param busy flag that tells whether the server should answer discovery requests
   * @param serverName the name that will be returned to discovered clients
   * @author WoFabian
   */
  public ServerDiscoveryResponder(int port, AtomicBoolean busy, String serverName) {
    this.port = port;
    this.busy = busy;

    // If no name is provided, use a default value so the client list still looks good.
    this.serverName = (serverName == null || serverName.isBlank()) ? "Battleship" : serverName;
  }

  /**
   * Stops the responder loop.
   *
   * <p>LOGIC-IMPORTANT: This only flips the running flag. If receive(...) is currently blocking,
   * the loop will end after the next packet arrives or the socket is closed from outside.
   *
   * @author WoFabian
   */
  public void stop() {
    running = false;
  }

  /**
   * Main loop that listens for UDP discovery packets and answers valid requests.
   *
   * <p>Protocol: - Incoming: DISCOVER - Outgoing: HERE port servername
   *
   * @author WoFabian
   */
  public void runLoop() {
    try (DatagramSocket socket = new DatagramSocket(port)) {
      socket.setBroadcast(true);

      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);

      while (running) {
        socket.receive(packet);

        String msg = DiscoveryProtocol.str(packet.getData(), packet.getLength());
        if (!DiscoveryProtocol.DISCOVER.equals(msg)) continue;

        // wenn schon ein Client verbunden ist: nicht mehr discoverbar
        if (busy.get()) continue;

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
