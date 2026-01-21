package com.matti.battleship.socket.state;

import com.matti.battleship.socket.protocol.Message;
import com.matti.battleship.socket.protocol.MessageType;

/**
 * Protocol state machine matching the official Battleship communication protocol.
 *
 * New game:
 *   Server -> size N
 *   Client -> done
 *   Server -> ships ...
 *   Client -> done
 *   Server -> ready
 *   Client -> ready
 *   Server starts -> gameplay
 *
 * Load game:
 *   Server -> load ID
 *   Client -> ok
 *   Server -> ready
 *   Client -> ready
 *   Server starts -> gameplay
 *
 * This class manages protocol flow only (no hit detection / ship logic).
 *
 * @author WoFabian
 */
public class NetworkStateMachine {

  private final boolean isServer;
  private GameState state;

  /**
   * Creates a state machine for server or client side.
   *
   * @param isServer true if server, false if client
   * @author WoFabian
   */
  public NetworkStateMachine(boolean isServer) {
    this.isServer = isServer;

    if (isServer) {
      // After TCP connect, server may start protocol by sending SIZE or LOAD.
      this.state = GameState.S_CAN_SEND_SIZE_OR_LOAD;
    } else {
      // Client waits for server to start with SIZE or LOAD.
      this.state = GameState.C_WAIT_SIZE_OR_LOAD;
    }
  }

  /**
   * @return current state
   * @author WoFabian
   */
  public GameState getState() {
    return state;
  }

  /**
   * Updates the state based on a received message.
   *
   * @param msg parsed incoming message
   * @author WoFabian
   */
  public void onMessageReceived(Message msg) {
    MessageType t = msg.getType();

    if (!isServer) {
      // ---------------- CLIENT SIDE ----------------
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

        case C_OPPONENT_TURN -> {
          if (t == MessageType.SHOT) state = GameState.MY_TURN;
        }

        case WAIT_FOR_ANSWER -> {
          if (t == MessageType.ANSWER) state = GameState.OPPONENT_TURN;
        }

        case MY_TURN, OPPONENT_TURN, GAME_OVER,
             C_NEED_DONE_AFTER_SIZE, C_NEED_DONE_AFTER_SHIPS, C_NEED_READY, C_NEED_OK_AFTER_LOAD -> {
          // No transition here. Those are handled by onMessageSent for client side.
        }

        default -> {
          // ignore
        }
      }
      return;
    }

    // ---------------- SERVER SIDE ----------------
    switch (state) {
      case S_WAIT_DONE_AFTER_SIZE -> {
        if (t == MessageType.DONE) state = GameState.S_CAN_SEND_SHIPS;
      }

      case S_WAIT_DONE_AFTER_SHIPS -> {
        if (t == MessageType.DONE) state = GameState.S_CAN_SEND_READY;
      }

      case S_WAIT_READY_FROM_CLIENT -> {
        if (t == MessageType.READY) state = GameState.MY_TURN; // server starts
      }

      case S_WAIT_OK_AFTER_LOAD -> {
        if (t == MessageType.OK) state = GameState.S_CAN_SEND_READY;
      }

      case OPPONENT_TURN -> {
        if (t == MessageType.SHOT) state = GameState.MY_TURN;
      }

      case WAIT_FOR_ANSWER -> {
        if (t == MessageType.ANSWER) state = GameState.OPPONENT_TURN;
      }

      case MY_TURN, GAME_OVER,
           S_CAN_SEND_SIZE_OR_LOAD, S_CAN_SEND_SHIPS, S_CAN_SEND_READY -> {
        // Sending transitions handled by onMessageSent.
      }

      default -> {
        // ignore
      }
    }
  }

  /**
   * Checks whether a message can be sent in the current state.
   *
   * @param type message type to send
   * @return true if allowed
   * @author WoFabian
   */
  public boolean canSend(MessageType type) {

    if (!isServer) {
      return switch (state) {
        case C_NEED_DONE_AFTER_SIZE -> type == MessageType.DONE;
        case C_NEED_DONE_AFTER_SHIPS -> type == MessageType.DONE;
        case C_NEED_READY -> type == MessageType.READY;
        case C_NEED_OK_AFTER_LOAD -> type == MessageType.OK;
        case MY_TURN -> type == MessageType.SHOT;
        default -> false;
      };
    }

    return switch (state) {
      case S_CAN_SEND_SIZE_OR_LOAD -> (type == MessageType.SIZE || type == MessageType.LOAD);
      case S_CAN_SEND_SHIPS -> type == MessageType.SHIPS;
      case S_CAN_SEND_READY -> type == MessageType.READY;
      case MY_TURN -> type == MessageType.SHOT;
      default -> false;
    };
  }

  /**
   * Updates the state after sending a message.
   *
   * @param type sent message type
   * @author WoFabian
   */
  public void onMessageSent(MessageType type) {

    if (!isServer) {
      // ---------------- CLIENT SIDE ----------------
      switch (state) {
        case C_NEED_DONE_AFTER_SIZE -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_SHIPS;
        }
        case C_NEED_DONE_AFTER_SHIPS -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        case C_NEED_READY -> {
          if (type == MessageType.READY) state = GameState.C_OPPONENT_TURN; // server starts
        }
        case C_NEED_OK_AFTER_LOAD -> {
          if (type == MessageType.OK) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        case MY_TURN -> {
          if (type == MessageType.SHOT) state = GameState.WAIT_FOR_ANSWER;
        }
        default -> {
          // no change
        }
      }
      return;
    }

    // ---------------- SERVER SIDE ----------------
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

      case MY_TURN -> {
        if (type == MessageType.SHOT) state = GameState.WAIT_FOR_ANSWER;
      }

      default -> {
        // no change
      }
    }
  }
}
