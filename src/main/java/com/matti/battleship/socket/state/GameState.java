package com.matti.battleship.socket.state;

/**
 * Protocol states matching the agreed Battleship communication protocol.
 *
 * <p>The protocol is ping-pong like: - Server sends setup commands, client acknowledges with
 * DONE/OK/READY - After READY/READY, server starts the game
 *
 * @author WoFabian
 */
public enum GameState {

  /** TCP connected, protocol not started yet. */
  INIT,

  // ===== CLIENT SETUP PATH =====

  /** Client waits for "size <n>" or "load <id>" from server. */
  C_WAIT_SIZE_OR_LOAD,

  /** Client must send "done" after receiving "size". */
  C_NEED_DONE_AFTER_SIZE,

  /** Client waits for "ships ..." from server. */
  C_WAIT_SHIPS,

  /** Client must send "done" after receiving "ships". */
  C_NEED_DONE_AFTER_SHIPS,

  /** Client waits for "ready" from server. */
  C_WAIT_READY_FROM_SERVER,

  /** Client must answer with "ready" after receiving server "ready". */
  C_NEED_READY,

  // Load path
  /** Client must send "ok" after receiving "load <id>". */
  C_NEED_OK_AFTER_LOAD,

  // ===== SERVER SETUP PATH =====

  /** Server is allowed to send "size" OR "load" in INIT. */
  S_CAN_SEND_SIZE_OR_LOAD,

  /** Server waits for client's "done" after sending "size". */
  S_WAIT_DONE_AFTER_SIZE,

  /** Server is allowed to send "ships" after DONE. */
  S_CAN_SEND_SHIPS,

  /** Server waits for client's "done" after sending "ships". */
  S_WAIT_DONE_AFTER_SHIPS,

  /** Server is allowed to send "ready" after DONE. */
  S_CAN_SEND_READY,

  /** Server waits for client's "ready" after sending "ready". */
  S_WAIT_READY_FROM_CLIENT,

  // Load path
  /** Server waits for client's "ok" after sending "load". */
  S_WAIT_OK_AFTER_LOAD,

  // ===== GAMEPLAY =====

  /** It is our turn, we may send "shot". */
  MY_TURN,

  /** We sent "shot" and must wait for "answer". */
  WAIT_FOR_ANSWER,

  /** Opponent shot at us; we must send "answer*/
  NEED_SEND_ANSWER,

  /** we received answer 0 (miss) for our shot; we must send "pass" */
  NEED_SEND_PASS,

  /** Opponent's turn, we wait for "shot". */
  OPPONENT_TURN,

  /** Game ended (optional). */
  GAME_OVER
}
