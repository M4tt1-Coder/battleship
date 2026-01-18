package com.matti.battleship.socket.discovery;

import java.net.*;
import java.util.*;

/**
 * Scans the local network for available Battleship servers using UDP broadcast discovery. This
 * class sends a broadcast message ("DISCOVER") to the configured port and then listens for
 * responses from servers that are currently free (not busy). The discovery protocol is defined in
 * {@link DiscoveryProtocol}. Expected server reply format: "BS_HERE_V1 <tcpPort> <serverName...>"
 * The result is a list of {@link DiscoveredServer} entries that can be displayed in the GUI and
 * used to establish a TCP connection afterwards.
 *
 * <p>GUI-IMPORTANT: Used by the GUI to build the "server list" screen. Important methods: -
 * discover(timeoutMillis): scans the LAN via UDP broadcast and returns a List<DiscoveredServer>
 * Typical GUI flow: - On "Search" button: new
 * ClientDiscoveryScanner(EnvConfig.getPort()).discover(600) - Put the returned list into a
 * ListView/TableView - On selection: use DiscoveredServer.host()/port() to connect via
 * ClientConnection
 *
 * @author WoFabian
 */
public class ClientDiscoveryScanner {

  /** UDP port used for discovery (same as TCP port for simplicity). */
  private final int port;

  /**
   * Creates a new discovery scanner for a specific port.
   *
   * @param port the UDP port where servers listen for discovery requests
   * @author WoFabian
   */
  public ClientDiscoveryScanner(int port) {
    this.port = port;
  }

  /**
   * Performs a discovery scan and returns a list of found servers. The scan works as follows: 1)
   * Create a UDP socket with broadcast enabled 2) Send one broadcast datagram to
   * 255.255.255.255:port 3) Collect server responses until the timeout expires 4) Deduplicate
   * servers by host address (one entry per IP) 5) Sort results by host address for stable output
   *
   * @param timeoutMillis how long to wait for server responses
   * @return list of discovered servers (can be empty if no server answered)
   * @throws Exception if socket creation or sending fails
   * @author WoFabian
   */
  public List<DiscoveredServer> discover(int timeoutMillis) throws Exception {
    // Key: host IP, Value: server info. Using a map avoids duplicates.
    Map<String, DiscoveredServer> found = new HashMap<>();

    // Temporary UDP socket used only for this discovery scan.
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);

      // Build and send the DISCOVER broadcast message.
      byte[] data = DiscoveryProtocol.bytes(DiscoveryProtocol.DISCOVER);
      DatagramPacket packet =
          new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
      socket.send(packet);

      // Collect responses until the timeout expires.
      long end = System.currentTimeMillis() + timeoutMillis;

      // Set short socket timeout so we can loop until "end" without blocking forever.
      socket.setSoTimeout(Math.max(50, timeoutMillis / 5));

      while (System.currentTimeMillis() < end) {
        try {
          byte[] buf = new byte[256];
          DatagramPacket resp = new DatagramPacket(buf, buf.length);
          socket.receive(resp);

          // Parse response string (protocol uses UTF-8 strings).
          String msg = DiscoveryProtocol.str(resp.getData(), resp.getLength());

          // Expected: BS_HERE_V1 <port> <name...>
          // Ignore any unknown packets.
          if (!msg.startsWith(DiscoveryProtocol.HERE_PREFIX)) continue;

          String[] parts = msg.split("\\s+");
          if (parts.length < 3) continue; // must at least contain prefix + port + name

          // Parse TCP port from the response.
          int tcpPort;
          try {
            tcpPort = Integer.parseInt(parts[1]);
          } catch (NumberFormatException nfe) {
            // invalid port -> ignore response
            continue;
          }

          // Everything after the port is considered the server name (may contain spaces).
          String name = joinFrom(parts, 2);

          // Use sender's IP address as host.
          String host = resp.getAddress().getHostAddress();

          // Store latest info. (If the same server answers multiple times, we overwrite.)
          found.put(host, new DiscoveredServer(host, tcpPort, name, System.currentTimeMillis()));

        } catch (SocketTimeoutException ignored) {
          // No response in the current wait window -> continue until overall timeout is reached.
        }
      }
    }

    // Convert map values to list and sort for stable ordering.
    List<DiscoveredServer> list = new ArrayList<>(found.values());
    list.sort(Comparator.comparing(DiscoveredServer::host));
    return list;
  }

  /**
   * Joins an array of string tokens from a given start index into a single string. This is used
   * because the server name may contain spaces and is transmitted as multiple tokens.
   *
   * @param parts the split message tokens
   * @param start the starting index (inclusive)
   * @return joined string from parts[start..end]
   * @author WoFabian
   */
  private String joinFrom(String[] parts, int start) {
    StringBuilder b = new StringBuilder();
    for (int i = start; i < parts.length; i++) {
      if (i > start) b.append(' ');
      b.append(parts[i]);
    }
    return b.toString();
  }
}
