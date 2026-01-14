package com.matti.battleship.socket.discovery;

import java.nio.charset.StandardCharsets;

public class DiscoveryProtocol {

    private DiscoveryProtocol() {}

    // "Magic" Strings, damit wir nicht auf fremde UDP-Pakete reagieren
    public static final String DISCOVER = "BS_DISCOVER_V1";
    public static final String HERE_PREFIX = "BS_HERE_V1";

    public static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String str(byte[] buf, int len) {
        return new String(buf, 0, len, StandardCharsets.UTF_8).trim();
    }
}
