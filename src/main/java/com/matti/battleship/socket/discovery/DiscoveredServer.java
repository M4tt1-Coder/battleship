package com.matti.battleship.socket.discovery;

/**
 * Represents a server discovered via UDP discovery. This record is used as a lightweight data
 * container for the GUI and connection logic. A discovered server contains: - host: the IP address
 * of the server (taken from the UDP packet sender) - port: the TCP port to connect to (sent by the
 * server in the discovery response) - name: an optional human-readable server name -
 * lastSeenMillis: timestamp (System.currentTimeMillis) when the server was last seen
 *
 * @param host IP address of the discovered server
 * @param port TCP port of the discovered server
 * @param name human-readable server name
 * @param lastSeenMillis timestamp in milliseconds when this server was discovered
 * @author WoFabian
 */
public record DiscoveredServer(String host, int port, String name, long lastSeenMillis) {}
