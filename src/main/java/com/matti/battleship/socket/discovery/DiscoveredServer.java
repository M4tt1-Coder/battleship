package com.matti.battleship.socket.discovery;

public record DiscoveredServer(String host, int port, String name, long lastSeenMillis) {}
