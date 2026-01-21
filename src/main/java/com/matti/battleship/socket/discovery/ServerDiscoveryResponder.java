package com.matti.battleship.socket.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP Discovery responder for the Battleship server.
 *
 * This runnable listens on a UDP port for a specific DISCOVER message. If the server is currently
 * not busy, it replies directly to the sender (client) with a HERE message containing the TCP port
 * and a server name. This allows clients to discover available servers in the local network.
 *
 * Important: The "busy" flag is used so that only free servers are visible in the discovery list.
 * If a server is already in a match, it will not respond to discovery requests.
 *
 * @author WoFabian
 */
public class ServerDiscoveryResponder implements Runnable {

  /** UDP port used for discovery. In this project it is the same as the TCP game port. */
  private final int port;

  /**
   * Indicates whether this server is currently busy (already has a connected client).
   * If busy == true, discovery requests will be ignored.
   */
  private final AtomicBoolean busy;

  /** Human readable server name that will be returned to the client. */
  private final String serverName;

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
   * Main loop of the responder.
   *
   * Opens a UDP socket on the discovery port and waits for incoming packets.
   * If the packet contains the DISCOVER message and the server is not busy,
   * it replies to the sender with "BS_HERE_V1 <port> <serverName>".
   *
   * @author WoFabian
   */
  @Override
  public void run() {
    try (DatagramSocket socket = new DatagramSocket(port)) {
      // Enable broadcast reception (so broadcast discovery packets can be received).
      socket.setBroadcast(true);

      // Buffer used to receive UDP packets.
      byte[] buf = new byte[256];
      DatagramPacket packet = new DatagramPacket(buf, buf.length);

      // Run forever until the socket is closed or an exception occurs.
      while (true) {

        // Block until an UDP packet arrives.
        socket.receive(packet);

        // Extract message content (trimmed string).
        String msg = DiscoveryProtocol.str(packet.getData(), packet.getLength());

        // Only respond to the exact discovery keyword.
        if (!DiscoveryProtocol.DISCOVER.equals(msg)) {
          continue;
        }

        // If the server already has a client, it should not be shown in discovery lists.
        if (busy.get()) {
          continue;
        }

        // Reply to the address/port where the request came from.
        InetAddress clientAddr = packet.getAddress();
        int clientPort = packet.getPort();

        // Response format: "BS_HERE_V1 <tcpPort> <serverName>"
        String reply = DiscoveryProtocol.HERE_PREFIX + " " + port + " " + serverName;
        byte[] out = DiscoveryProtocol.bytes(reply);

        DatagramPacket response = new DatagramPacket(out, out.length, clientAddr, clientPort);
        socket.send(response);
      }

    } catch (Exception e) {
      // If the responder stops, we log a small message for debugging.
      System.out.println("[DISCOVERY] Responder gestoppt: " + e.getMessage());
    }
  }
}
