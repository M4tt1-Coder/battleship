package com.matti.battleship.socket.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responds to UDP broadcast discovery requests for Battleship servers.
 *
 * <p>This class listens for {@code DISCOVER} messages and replies with a {@code HERE} response
 * containing the TCP game port and a human-readable server name.
 *
 * <p>LOGIC-IMPORTANT: The {@code busy} flag is shared with the TCP server lifecycle. If a client is
 * already connected ({@code busy == true}), discovery requests are ignored so the server does not
 * appear in the client's server list anymore.
 *
 * @author WoFabian
 */
public class ServerDiscoveryResponder {

  private static final Logger logger = LogManager.getLogger(ServerDiscoveryResponder.class);

  /** UDP port used for discovery. In this project it is the same as the TCP game port. */
  private final int port;

  /**
   * Indicates whether this server is currently busy (already has a connected client).
   *
   * <p>LOGIC-IMPORTANT: If {@code busy == true}, discovery requests will be ignored to prevent new
   * clients from selecting a server that is already in use.
   */
  private final AtomicBoolean busy;

  /** Human readable server name that will be returned to the client. */
  private final String serverName;

  /**
   * Run flag for the discovery loop.
   *
   * <p>Declared volatile so {@link #stop()} can terminate the loop from another thread.
   */
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
   * <p>LOGIC-IMPORTANT: This only flips the running flag. If {@code receive(...)} is currently
   * blocking, the loop will end after the next packet arrives or the socket is closed from outside.
   *
   * @author WoFabian
   */
  public void stop() {
    running = false;
  }

  /**
   * Main loop that listens for UDP discovery packets and answers valid requests.
   *
   * <p>Protocol:
   *
   * <p>Incoming: {@code DISCOVER}
   *
   * <p>Outgoing: {@code HERE <port> <serverName>}
   *
   * <p>LOGIC-IMPORTANT: Only {@code DISCOVER} packets are answered. Unknown messages are ignored to
   * keep LAN discovery robust against noise/broadcast traffic.
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

        // If a client is already connected: do not appear in discovery results anymore.
        if (busy.get()) continue;

        InetAddress clientAddr = packet.getAddress();
        int clientPort = packet.getPort();

        String reply = DiscoveryProtocol.HERE_PREFIX + " " + port + " " + serverName;
        byte[] out = DiscoveryProtocol.bytes(reply);

        DatagramPacket response = new DatagramPacket(out, out.length, clientAddr, clientPort);
        socket.send(response);
      }

    } catch (Exception e) {
      // Shutdown and socket errors are not fatal here; discovery is an optional helper feature.
      logger.info("[DISCOVERY] Responder gestoppt: " + e.getMessage());
    }
  }
}
