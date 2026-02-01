package com.matti.battleship.socket.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Small logging helper used by the socket layer to print readable turn-based communication logs.
 *
 * <p>The goal of this class is debugging and transparency during development: - It prints a turn
 * header whenever the "turn owner" changes (SERVER/CLIENT) - It logs sent and received protocol
 * lines - It prints a separator after a complete send/receive duo (used by SocketConnector pair
 * logic)
 *
 * <p>Note: This logger does not enforce game rules or protocol correctness. It is only for output
 * and debugging. The real protocol flow should be handled by a NetworkStateMachine / controller.
 *
 * @author WoFabian
 */
public class TurnLog {

  /** log4j logger instance used for all output in this class. */
  private static final Logger logger = LogManager.getLogger(TurnLog.class);

  /**
   * Defines on which side this log instance runs.
   *
   * <p>This is used by higher layers (e.g. SocketConnector) to decide whether "self" is SERVER or
   * CLIENT.
   *
   * @author WoFabian
   */
  public enum Side {
    SERVER,
    CLIENT
  }

  /** The side of this log instance (SERVER or CLIENT). */
  private final Side side;

  /** Stores the last printed turn owner so we only print the header when it changes. */
  private String currentTurnOwner = null;

  /**
   * Creates a new TurnLog for the given side.
   *
   * @param side side of this instance (SERVER or CLIENT)
   */
  public TurnLog(Side side) {
    this.side = side;
  }

  /**
   * Returns the side of this log instance.
   *
   * @return SERVER or CLIENT
   */
  public Side getSide() {
    return side;
  }

  /**
   * Sets the current turn owner (if needed) and prints the turn header.
   *
   * <p>This method only prints a new header if the turn owner changed compared to the last call.
   *
   * @param turnOwner name of the current turn owner (usually "SERVER" or "CLIENT")
   */
  public void beginTurn(String turnOwner) {
    if (turnOwner == null) return;

    // Print header only when the turn owner changes to keep the log readable.
    if (!turnOwner.equals(currentTurnOwner)) {
      currentTurnOwner = turnOwner;
      printTurnHeader();
    }
  }

  /** Repeats the current turn header (useful after separators). */
  public void repeatTurnHeader() {
    if (currentTurnOwner != null) {
      printTurnHeader();
    }
  }

  /**
   * Logs an outgoing message.
   *
   * @param msg protocol line that was sent
   */
  public void sent(String msg) {
    logger.info("  [GESENDET ] " + msg);
  }

  /**
   * Logs an incoming message.
   *
   * @param msg protocol line that was received
   */
  public void received(String msg) {
    logger.info("  [RECEIVED ] " + msg);
  }

  /**
   * Prints a separator line.
   *
   * <p>Used by SocketConnector after a complete send/receive pair so the console output is grouped.
   */
  public void separator() {
    logger.info("  ------------------------");
  }

  /**
   * Prints the current turn header.
   *
   * <p>Uses parameterized logging ("{}") which is the recommended log4j style because it avoids
   * unnecessary string concatenations and keeps the log formatting consistent.
   */
  private void printTurnHeader() {
    logger.info("");
    logger.info("[{} ZUG]", currentTurnOwner);
  }
}
