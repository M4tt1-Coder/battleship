package com.matti.battleship.socket.state;

import com.matti.battleship.socket.protocol.Message;
import com.matti.battleship.socket.protocol.MessageType;

/**
 * Protocol state machine for the Battleship setup handshake.
 *
 * <p>This class tracks the current {@link GameState} and validates the allowed protocol flow for
 * server and client. It is intentionally small: it only covers the setup/handshake path
 * (size/ships/load + done/ok + ready/ready) and switches into gameplay states afterwards.
 *
 * <p>LOGIC-IMPORTANT: This is not the gameplay rule engine. It does not implement shots, answers,
 * win/lose, etc. Its job is only to enforce correct protocol order and to provide a clear
 * "canSend(...)" gate for GUI/controller code.
 *
 * <p>LOGIC-IMPORTANT: Side-specific behavior is controlled by {@code isServer}. The same class is
 * used for both roles so tests and GUI integration stay symmetric.
 *
 * @author WoFabian
 */
public class NetworkStateMachine {

  /** True if this instance represents the server side (affects allowed message order). */
  private final boolean isServer;

  /** Current protocol state. */
  private GameState state;

  /**
   * Creates a new state machine for the given side.
   *
   * <p>LOGIC-IMPORTANT: The initial state is different for server and client: the server starts by
   * being allowed to send {@code size} or {@code load}, while the client starts by waiting for one
   * of those commands.
   *
   * @param isServer true for server rules, false for client rules
   */
  public NetworkStateMachine(boolean isServer) {
    this.isServer = isServer;
    this.state = isServer ? GameState.S_CAN_SEND_SIZE_OR_LOAD : GameState.C_WAIT_SIZE_OR_LOAD;
  }

  /**
   * Returns the current protocol state.
   *
   * @return current {@link GameState}
   */
  public GameState getState() {
    return state;
  }

  /**
   * Advances the state machine based on a received message.
   *
   * <p>LOGIC-IMPORTANT: This method only transitions on messages that are meaningful for the
   * current setup state. Unexpected messages are ignored here; higher layers may choose to handle
   * them as errors (or gameplay messages once the handshake is complete).
   *
   * @param msg parsed incoming protocol message
   */
  public void onMessageReceived(Message msg) {
    MessageType t = msg.getType();

    if (!isServer) {
      switch (state) {
        case C_WAIT_SIZE_OR_LOAD -> {
          if (t == MessageType.SIZE) state = GameState.C_NEED_DONE_AFTER_SIZE;
          else if (t == MessageType.LOAD) state = GameState.C_NEED_OK_AFTER_LOAD;
        }
        case C_WAIT_SHIPS -> {
          if (t == MessageType.SHIPS) state = GameState.C_NEED_DONE_AFTER_SHIPS;
        }
        case C_WAIT_READY_FROM_SERVER -> {
          if (t == MessageType.READY) state = GameState.C_NEED_READY;
        }
        default -> {}
      }
      return;
    }

    switch (state) {
      case S_WAIT_DONE_AFTER_SIZE -> {
        if (t == MessageType.DONE) state = GameState.S_CAN_SEND_SHIPS;
      }
      case S_WAIT_DONE_AFTER_SHIPS -> {
        if (t == MessageType.DONE) state = GameState.S_CAN_SEND_READY;
      }
      case S_WAIT_READY_FROM_CLIENT -> {
        if (t == MessageType.READY) state = GameState.MY_TURN;
      }
      case S_WAIT_OK_AFTER_LOAD -> {
        if (t == MessageType.OK) state = GameState.S_CAN_SEND_READY;
      }
      default -> {}
    }
  }

  /**
   * Checks whether a message of the given type is allowed to be sent in the current state.
   *
   * <p>GUI-IMPORTANT: GUI/controller code should call this before sending, so invalid protocol
   * transitions can be prevented early (instead of producing confusing peer-side errors).
   *
   * @param type message type to send
   * @return true if sending this type is allowed in the current state
   */
  public boolean canSend(MessageType type) {
    if (!isServer) {
      return switch (state) {
        case C_NEED_DONE_AFTER_SIZE -> type == MessageType.DONE;
        case C_NEED_DONE_AFTER_SHIPS -> type == MessageType.DONE;
        case C_NEED_READY -> type == MessageType.READY;
        case C_NEED_OK_AFTER_LOAD -> type == MessageType.OK;
        default -> false;
      };
    }

    return switch (state) {
      case S_CAN_SEND_SIZE_OR_LOAD -> (type == MessageType.SIZE || type == MessageType.LOAD);
      case S_CAN_SEND_SHIPS -> type == MessageType.SHIPS;
      case S_CAN_SEND_READY -> type == MessageType.READY;
      default -> false;
    };
  }

  /**
   * Advances the state machine after a message was sent successfully.
   *
   * <p>LOGIC-IMPORTANT: Sending and receiving drive different transitions. The controller
   * typically:
   *
   * <p>1) validates via {@link #canSend(MessageType)}
   *
   * <p>2) sends the raw line over the network
   *
   * <p>3) calls this method so the state machine moves forward
   *
   * @param type message type that was sent
   */
  public void onMessageSent(MessageType type) {
    if (!isServer) {
      switch (state) {
        case C_NEED_DONE_AFTER_SIZE -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_SHIPS;
        }
        case C_NEED_DONE_AFTER_SHIPS -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        case C_NEED_READY -> {
          if (type == MessageType.READY) state = GameState.C_OPPONENT_TURN;
        }
        case C_NEED_OK_AFTER_LOAD -> {
          if (type == MessageType.OK) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        default -> {}
      }
      return;
    }

    switch (state) {
      case S_CAN_SEND_SIZE_OR_LOAD -> {
        if (type == MessageType.SIZE) state = GameState.S_WAIT_DONE_AFTER_SIZE;
        else if (type == MessageType.LOAD) state = GameState.S_WAIT_OK_AFTER_LOAD;
      }
      case S_CAN_SEND_SHIPS -> {
        if (type == MessageType.SHIPS) state = GameState.S_WAIT_DONE_AFTER_SHIPS;
      }
      case S_CAN_SEND_READY -> {
        if (type == MessageType.READY) state = GameState.S_WAIT_READY_FROM_CLIENT;
      }
      default -> {}
    }
  }
}
