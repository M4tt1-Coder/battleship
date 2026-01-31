package com.matti.battleship.socket.state;

/**
 * Protocol states matching the agreed Battleship communication protocol.
 *
 * <p>This enum describes the allowed communication phases for the ping-pong protocol between server
 * and client. The state machine uses these values to validate message order and to decide which
 * commands are allowed to be sent next.
 *
 * <p>LOGIC-IMPORTANT: The setup is server-driven. The server sends setup commands and the client
 * acknowledges with {@code done}/{@code ok}/{@code ready}. After {@code ready/ready}, gameplay
 * starts and turns alternate via {@code shot}/{@code answer}/{@code pass}.
 *
 * <p>Naming convention:
 *
 * <p>{@code C_*} states are used by the client-side state machine
 *
 * <p>{@code S_*} states are used by the server-side state machine
 *
 * <p>Gameplay states (e.g. {@link #MY_TURN}) are shared for both sides
 *
 * @author WoFabian
 */
public enum GameState {

  /** TCP connected, protocol not started yet. */
  INIT,

  // ===== CLIENT SETUP PATH =====

  /** Client waits for {@code size <n>} or {@code load <id>} from server. */
  C_WAIT_SIZE_OR_LOAD,

  /** Client must send {@code done} after receiving {@code size}. */
  C_NEED_DONE_AFTER_SIZE,

  /** Client waits for {@code ships ...} from server. */
  C_WAIT_SHIPS,

  /** Client must send {@code done} after receiving {@code ships}. */
  C_NEED_DONE_AFTER_SHIPS,

  /** Client waits for server {@code ready}. */
  C_WAIT_READY_FROM_SERVER,

  /** Client must answer with {@code ready} after receiving server {@code ready}. */
  C_NEED_READY,

  /**
   * Client-side helper state for gameplay.
   *
   * <p>LOGIC-IMPORTANT: Some implementations separate "opponent turn" handling during client flow
   * (e.g. after loading) from the generic gameplay turn state for clarity.
   */
  C_OPPONENT_TURN,

  // Load path

  /** Client must send {@code ok} after receiving {@code load <id>}. */
  C_NEED_OK_AFTER_LOAD,

  // ===== SERVER SETUP PATH =====

  /** Server is allowed to send {@code size} OR {@code load} in {@link #INIT}. */
  S_CAN_SEND_SIZE_OR_LOAD,

  /** Server waits for client's {@code done} after sending {@code size}. */
  S_WAIT_DONE_AFTER_SIZE,

  /** Server is allowed to send {@code ships} after DONE. */
  S_CAN_SEND_SHIPS,

  /** Server waits for client's {@code done} after sending {@code ships}. */
  S_WAIT_DONE_AFTER_SHIPS,

  /** Server is allowed to send {@code ready} after DONE. */
  S_CAN_SEND_READY,

  /** Server waits for client's {@code ready} after sending {@code ready}. */
  S_WAIT_READY_FROM_CLIENT,

  // Load path

  /** Server waits for client's {@code ok} after sending {@code load}. */
  S_WAIT_OK_AFTER_LOAD,

  // ===== GAMEPLAY =====

  /** It is our turn; we may send {@code shot}. */
  MY_TURN,

  /** We sent {@code shot} and must wait for {@code answer}. */
  WAIT_FOR_ANSWER,

  /** Opponent's turn; we wait for {@code shot}. */
  OPPONENT_TURN,

  /** Game ended (optional). */
  GAME_OVER
}
