package com.matti.battleship.socket.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TurnLog {

  private static final Logger logger = LogManager.getLogger(TurnLog.class);

  public enum Side {
    SERVER,
    CLIENT
  }

  private final Side side;
  private String currentTurnOwner = null;

  public TurnLog(Side side) {
    this.side = side;
  }

  public Side getSide() {
    return side;
  }

  /** Setzt (falls nötig) den aktuellen Zugbesitzer und druckt den Header */
  public void beginTurn(String turnOwner) {
    if (turnOwner == null) return;

    if (!turnOwner.equals(currentTurnOwner)) {
      currentTurnOwner = turnOwner;
      printTurnHeader();
    }
  }

  /** Wiederholt den aktuellen Turn-Header (für Übersicht) */
  public void repeatTurnHeader() {
    if (currentTurnOwner != null) {
      printTurnHeader();
    }
  }

  public void sent(String msg) {
    logger.info("  [GESENDET ] " + msg);
  }

  public void received(String msg) {
    System.out.println("  [EMPFANGEN] " + msg);
  }

  /** Trennlinie nach jedem Gesendet/Empfangen-Duo */
  public void separator() {
    System.out.println("  ------------------------");
  }

  private void printTurnHeader() {
    System.out.println();
    System.out.println("[" + currentTurnOwner + " ZUG]");
  }
}
