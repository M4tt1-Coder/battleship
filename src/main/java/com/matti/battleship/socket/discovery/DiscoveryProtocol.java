package com.matti.battleship.socket.discovery;

import java.nio.charset.StandardCharsets;

/**
 * Defines the UDP discovery protocol used to find Battleship servers in the local network. The
 * protocol is intentionally simple and text-based: - Clients broadcast a discovery request using
 * {@link #DISCOVER} - Free servers answer with a response starting with {@link #HERE_PREFIX}
 *
 * <p>* This class also provides helper methods to convert between String and byte[] using UTF-8.
 *
 * @author WoFabian
 */
public class DiscoveryProtocol {

  /**
   * Private constructor to prevent instantiation.
   *
   * @author WoFabian
   */
  private DiscoveryProtocol() {}

  /** Discovery request message sent by clients. */
  public static final String DISCOVER = "BS_DISCOVER_V1";

  /** Prefix for discovery responses sent by servers. */
  public static final String HERE_PREFIX = "BS_HERE_V1";

  /**
   * Converts a string into a UTF-8 encoded byte array.
   *
   * @param s input string
   * @return UTF-8 encoded bytes
   */
  public static byte[] bytes(String s) {

    return s.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Converts a portion of a byte array into a UTF-8 string and trims it. This is used for reading
   * DatagramPacket content where the buffer is larger than the actual payload length.
   *
   * @param buf byte buffer (e.g. from DatagramPacket.getData())
   * @param len actual message length (e.g. from DatagramPacket.getLength())
   * @return decoded and trimmed message string
   */
  public static String str(byte[] buf, int len) {
    return new String(buf, 0, len, StandardCharsets.UTF_8).trim();
  }
}
