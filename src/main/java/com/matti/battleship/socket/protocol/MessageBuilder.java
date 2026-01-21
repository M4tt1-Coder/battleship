package com.matti.battleship.socket.protocol;

/**
 * Utility class to build protocol messages as raw strings.
 *
 * This class is used by the network/gameflow layer to create correct command lines that are sent
 * over the TCP connection. Each method returns exactly one line (without newline), which can be
 * passed to ClientConnection/ServerConnection send(...).
 *
 * Protocol examples:
 * - size 10
 * - ships 5 4 3 3 2
 * - shot 3 7
 * - answer 0
 *
 * @author WoFabian
 */
public class MessageBuilder {

  /**
   * Builds a SIZE command.
   *
   * Example: "size 10"
   *
   * @param rows board size (number of rows/columns)
   * @return protocol line for size
   * @author WoFabian
   */
  public static String size(int rows) {
    return "size " + rows;
  }

  /**
   * Builds a SHIPS command.
   *
   * Example: "ships 5 4 3 3 2"
   *
   * @param l ship lengths
   * @return protocol line for ships
   * @author WoFabian
   */
  public static String ships(int... l) {
    StringBuilder sb = new StringBuilder("ships");
    for (int i : l) sb.append(" ").append(i);
    return sb.toString();
  }

  /**
   * Builds a DONE command.
   *
   * DONE is typically used as acknowledgement after receiving SIZE or SHIPS.
   *
   * @return protocol line "done"
   * @author WoFabian
   */
  public static String done() {
    return "done";
  }

  /**
   * Builds a READY command.
   *
   * READY is used to indicate that the player finished setup and is ready to start.
   *
   * @return protocol line "ready"
   * @author WoFabian
   */
  public static String ready() {
    return "ready";
  }

  /**
   * Builds a SHOT command.
   *
   * IMPORTANT: The official protocol uses 1-based coordinates (row/col start at 1).
   * If your internal board uses 0-based indices, convert before calling this method.
   *
   * Example: "shot 3 7"
   *
   * @param r row coordinate (usually 1-based in the protocol)
   * @param c column coordinate (usually 1-based in the protocol)
   * @return protocol line for shot
   * @author WoFabian
   */
  public static String shot(int r, int c) {
    return "shot " + r + " " + c;
  }

  /**
   * Builds an ANSWER command.
   *
   * Protocol meaning:
   * - 0 = water (miss)
   * - 1 = hit
   * - 2 = hit + sunk
   *
   * Example: "answer 0"
   *
   * @param a answer code (0/1/2)
   * @return protocol line for answer
   * @author WoFabian
   */
  public static String answer(int a) {
    return "answer " + a;
  }

  /**
   * Builds a PASS command.
   *
   * PASS is used after a miss (answer 0) to clearly switch the turn to the opponent.
   *
   * @return protocol line "pass"
   * @author WoFabian
   */
  public static String pass() {
    return "pass";
  }

  /**
   * Builds a SAVE command.
   *
   * Example: "save 123456"
   *
   * @param id save id
   * @return protocol line for save
   * @author WoFabian
   */
  public static String save(long id) {
    return "save " + id;
  }

  /**
   * Builds a LOAD command.
   *
   * Example: "load 123456"
   *
   * @param id save id
   * @return protocol line for load
   * @author WoFabian
   */
  public static String load(long id) {
    return "load " + id;
  }

  /**
   * Builds an OK command.
   *
   * OK is typically used as acknowledgement after receiving SAVE or LOAD.
   *
   * @return protocol line "ok"
   * @author WoFabian
   */
  public static String ok() {
    return "ok";
  }
}
