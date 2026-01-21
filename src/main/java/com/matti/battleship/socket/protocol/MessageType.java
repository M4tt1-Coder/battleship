package com.matti.battleship.socket.protocol;

/**
 * Enum of all supported protocol commands for the Battleship socket communication.
 *
 * Each {@link MessageType} represents the first keyword of a protocol line.
 * The arguments depend on the command, for example:
 * - SIZE   -> "size <rows>"
 * - SHOT   -> "shot <row> <col>"
 * - ANSWER -> "answer <a>"
 *
 * UNKNOWN is used when the parser receives an unexpected or invalid command.
 *
 * @author WoFabian
 */
public enum MessageType {

  /** Server sends board size: "size <rows>" */
  SIZE,

  /** Server sends ship lengths: "ships <length> <length> ..." */
  SHIPS,

  /** Client acknowledges setup commands: "done" */
  DONE,

  /** Used by both sides to indicate readiness: "ready" */
  READY,

  /** A shot action: "shot <row> <col>" */
  SHOT,

  /** Result of a shot: "answer <a>" where a is 0/1/2 */
  ANSWER,

  /** Turn switch after a miss: "pass" */
  PASS,

  /** Request to save a game state: "save <id>" */
  SAVE,

  /** Request to load a game state: "load <id>" */
  LOAD,

  /** Acknowledgement for save/load: "ok" */
  OK,

  /** Fallback for unknown/invalid commands */
  UNKNOWN
}
