package com.matti.battleship.socket.state;

import com.matti.battleship.socket.protocol.Message;
import com.matti.battleship.socket.protocol.MessageType;

/**
 * Protocol state machine matching the Battleship communication protocol.
 *
 * <p>Key gameplay rules: - Shooter sends SHOT - Defender must send ANSWER - If ANSWER == 0 (MISS),
 * shooter must send PASS, then opponent gets MY_TURN - If ANSWER == 1/2 (HIT/SUNK), shooter keeps
 * MY_TURN (may shoot again)
 */
public class NetworkStateMachine {

  private final boolean isServer;
  private GameState state;

  public NetworkStateMachine(boolean isServer) {
    this.isServer = isServer;
    this.state = isServer ? GameState.S_CAN_SEND_SIZE_OR_LOAD : GameState.C_WAIT_SIZE_OR_LOAD;
  }

  public GameState getState() {
    return state;
  }

  public void onMessageReceived(Message msg) {
    MessageType t = msg.getType();

    // =========================
    // SETUP (CLIENT)
    // =========================
    if (!isServer) {
      switch (state) {
        case C_WAIT_SIZE_OR_LOAD -> {
          if (t == MessageType.SIZE) state = GameState.C_NEED_DONE_AFTER_SIZE;
          else if (t == MessageType.LOAD) state = GameState.C_NEED_OK_AFTER_LOAD;
          return;
        }
        case C_WAIT_SHIPS -> {
          if (t == MessageType.SHIPS) state = GameState.C_NEED_DONE_AFTER_SHIPS;
          return;
        }
        case C_WAIT_READY_FROM_SERVER -> {
          if (t == MessageType.READY) state = GameState.C_NEED_READY;
          return;
        }
        default -> {
          // fall through to gameplay
        }
      }
    }

    // =========================
    // SETUP (SERVER)
    // =========================
    if (isServer) {
      switch (state) {
        case S_WAIT_DONE_AFTER_SIZE -> {
          if (t == MessageType.DONE) state = GameState.S_CAN_SEND_SHIPS;
          return;
        }
        case S_WAIT_DONE_AFTER_SHIPS -> {
          if (t == MessageType.DONE) state = GameState.S_CAN_SEND_READY;
          return;
        }
        case S_WAIT_OK_AFTER_LOAD -> {
          if (t == MessageType.OK) state = GameState.S_CAN_SEND_READY;
          return;
        }
        case S_WAIT_READY_FROM_CLIENT -> {
          if (t == MessageType.READY) state = GameState.MY_TURN; // server starts
          return;
        }
        default -> {
          // fall through to gameplay
        }
      }
    }

    // =========================
    // GAMEPLAY
    // =========================
    switch (state) {
      case OPPONENT_TURN -> {
        if (t == MessageType.SHOT) {
          state = GameState.NEED_SEND_ANSWER;
        } else if (t == MessageType.PASS) {
          state = GameState.MY_TURN;
        }
      }

      case WAIT_FOR_ANSWER -> {
        if (t == MessageType.ANSWER) {
          int a = safeIntArg(msg, 0, -1);
          if (a == 0) {
            state = GameState.NEED_SEND_PASS; // miss -> must pass
          } else if (a == 1 || a == 2) {
            state = GameState.MY_TURN; // hit/sunk -> keep turn
          }
        }
      }

      default -> {
        // NEED_SEND_ANSWER / NEED_SEND_PASS are advanced via onMessageSent(...)
      }
    }
  }

  public boolean canSend(MessageType type) {
    if (!isServer) {
      return switch (state) {
        // client setup acks
        case C_NEED_DONE_AFTER_SIZE -> type == MessageType.DONE;
        case C_NEED_DONE_AFTER_SHIPS -> type == MessageType.DONE;
        case C_NEED_OK_AFTER_LOAD -> type == MessageType.OK;
        case C_NEED_READY -> type == MessageType.READY;

        // gameplay
        case MY_TURN -> type == MessageType.SHOT;
        case NEED_SEND_ANSWER -> type == MessageType.ANSWER;
        case NEED_SEND_PASS -> type == MessageType.PASS;

        default -> false;
      };
    }

    return switch (state) {
      // server setup
      case S_CAN_SEND_SIZE_OR_LOAD -> (type == MessageType.SIZE || type == MessageType.LOAD);
      case S_CAN_SEND_SHIPS -> type == MessageType.SHIPS;
      case S_CAN_SEND_READY -> type == MessageType.READY;

      // gameplay
      case MY_TURN -> type == MessageType.SHOT;
      case NEED_SEND_ANSWER -> type == MessageType.ANSWER;
      case NEED_SEND_PASS -> type == MessageType.PASS;

      default -> false;
    };
  }

  public void onMessageSent(MessageType type) {
    if (!isServer) {
      switch (state) {
        // client setup
        case C_NEED_DONE_AFTER_SIZE -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_SHIPS;
        }
        case C_NEED_DONE_AFTER_SHIPS -> {
          if (type == MessageType.DONE) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        case C_NEED_OK_AFTER_LOAD -> {
          if (type == MessageType.OK) state = GameState.C_WAIT_READY_FROM_SERVER;
        }
        case C_NEED_READY -> {
          if (type == MessageType.READY) state = GameState.OPPONENT_TURN; // server starts
        }

        // gameplay
        case MY_TURN -> {
          if (type == MessageType.SHOT) state = GameState.WAIT_FOR_ANSWER;
        }
        case NEED_SEND_ANSWER -> {
          if (type == MessageType.ANSWER) state = GameState.OPPONENT_TURN;
        }
        case NEED_SEND_PASS -> {
          if (type == MessageType.PASS) state = GameState.OPPONENT_TURN;
        }

        default -> {
          // no change
        }
      }
      return;
    }

    // server
    switch (state) {
      // server setup
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

      // gameplay
      case MY_TURN -> {
        if (type == MessageType.SHOT) state = GameState.WAIT_FOR_ANSWER;
      }
      case NEED_SEND_ANSWER -> {
        if (type == MessageType.ANSWER) state = GameState.OPPONENT_TURN;
      }
      case NEED_SEND_PASS -> {
        if (type == MessageType.PASS) state = GameState.OPPONENT_TURN;
      }

      default -> {
        // no change
      }
    }
  }

  private int safeIntArg(Message msg, int idx, int fallback) {
    if (msg == null) {
      return fallback;
    }

    String[] args = msg.getArgs();
    if (args == null || idx < 0 || idx >= args.length || args[idx] == null) {
      return fallback;
    }

    try {
      return Integer.parseInt(args[idx]);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }
}
