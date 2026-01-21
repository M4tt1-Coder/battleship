package com.matti.battleship.socket.state;

import com.matti.battleship.enums.PlayingMode;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.protocol.Message;
import com.matti.battleship.socket.protocol.MessageBuilder;
import com.matti.battleship.socket.protocol.MessageParser;
import com.matti.battleship.socket.protocol.MessageType;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Game;

/**
 * Bridges socket transport <-> protocol state machine <-> game logic.
 *
 * <p>Responsibilities: - Parse incoming socket lines into Message objects - Drive
 * NetworkStateMachine - Auto-send mandatory setup acknowledgements (DONE/OK/READY) - Integrate
 * gameplay: - On SHOT: evaluate on local board, send ANSWER (0/1/2) - On ANSWER: apply result to
 * opponent board using Game.applyOpponentsResponseToPlayersShot(...) - On MISS (answer 0): shooter
 * sends PASS to switch turns (protocol example)
 *
 * <p>Coordinate handling: - Protocol uses 1-based row/col (starts at 1) - Internal board uses
 * 0-based indices (arrays)
 *
 * @author WoFabian
 */
public class NetworkGameController implements MessageListener {

  /**
   * Minimal sender abstraction so the controller does not care whether it's client or server.
   *
   * @author WoFabian
   */
  public interface NetworkSender {
    void send(String msg) throws Exception;
  }

  private final boolean isServer;
  private final NetworkSender sender;
  private final NetworkStateMachine sm;

  // Your actual game instance (must be provided by GUI/logic once setup is known).
  private Game game;

  // Remember last shot we sent (internal 0-based) so we can apply incoming ANSWER to the correct
  // cell.
  private Coordinates lastShotInternal = null;

  public NetworkGameController(boolean isServer, NetworkSender sender) {
    this.isServer = isServer;
    this.sender = sender;
    this.sm = new NetworkStateMachine(isServer);
  }

  /**
   * Provide the current game instance (created by your logic/GUI).
   *
   * <p>IMPORTANT: - For PvP network play, game.getPlayingMode() should be VS_PLAYER. - The turn
   * should start as: - server side: PlayerTurn.PLAYER - client side: PlayerTurn.OPPONENT
   *
   * @param game game instance
   * @author WoFabian
   */
  public void setGame(Game game) {
    this.game = game;
  }

  /**
   * @return state machine (useful for GUI button enable/disable)
   * @author WoFabian
   */
  public NetworkStateMachine getStateMachine() {
    return sm;
  }

  // =========================================================
  // MessageListener
  // =========================================================

  @Override
  public void onMessageReceived(String raw) {
    Message msg = MessageParser.parse(raw);

    // 1) Update protocol state machine
    sm.onMessageReceived(msg);

    // 2) Auto-ack setup steps (client side)
    tryAutoAck(msg);

    // 3) Gameplay integration
    switch (msg.getType()) {
      case SHOT -> handleIncomingShot(msg);
      case ANSWER -> handleIncomingAnswer(msg);
      case PASS -> handleIncomingPass();
      default -> {
        // ignore (other setup messages are handled by your GUI/logic)
      }
    }
  }

  @Override
  public void onConnectionClosed(Exception e) {
    System.out.println("[NET] connection closed: " + (e != null ? e.getMessage() : "null"));
  }

  // =========================================================
  // Public sending API (for GUI/logic)
  // =========================================================

  public void sendSize(int size) throws Exception {
    sendTyped(MessageType.SIZE, MessageBuilder.size(size));
  }

  public void sendShips(int... ships) throws Exception {
    sendTyped(MessageType.SHIPS, MessageBuilder.ships(ships));
  }

  public void sendReady() throws Exception {
    sendTyped(MessageType.READY, MessageBuilder.ready());
  }

  public void sendLoad(long id) throws Exception {
    sendTyped(MessageType.LOAD, MessageBuilder.load(id));
  }

  /**
   * Sends a shot using INTERNAL (0-based) coordinates.
   *
   * <p>Protocol is 1-based, so we convert (x+1, y+1).
   *
   * @param x internal 0-based row
   * @param y internal 0-based col
   * @throws Exception if not allowed or send fails
   * @author WoFabian
   */
  public void sendShot0Based(int x, int y) throws Exception {
    Coordinates internal = new Coordinates(x, y);
    this.lastShotInternal = internal;

    // convert to protocol 1-based
    int row = x + 1;
    int col = y + 1;

    sendTyped(MessageType.SHOT, MessageBuilder.shot(row, col));
  }

  // =========================================================
  // Internals
  // =========================================================

  private void sendTyped(MessageType type, String raw) throws Exception {
    if (!sm.canSend(type)) {
      throw new IllegalStateException(
          "Cannot send " + type + " in state " + sm.getState() + " (isServer=" + isServer + ")");
    }
    sender.send(raw);
    sm.onMessageSent(type);
  }

  /**
   * Auto-ack required by protocol (client side only): - after SIZE -> DONE - after SHIPS -> DONE -
   * after LOAD -> OK - after READY -> READY
   *
   * @param msg received message
   * @author WoFabian
   */
  private void tryAutoAck(Message msg) {
    if (isServer) return;

    try {
      switch (sm.getState()) {
        case C_NEED_DONE_AFTER_SIZE -> {
          sender.send(MessageBuilder.done());
          sm.onMessageSent(MessageType.DONE);
        }
        case C_NEED_DONE_AFTER_SHIPS -> {
          sender.send(MessageBuilder.done());
          sm.onMessageSent(MessageType.DONE);
        }
        case C_NEED_OK_AFTER_LOAD -> {
          sender.send(MessageBuilder.ok());
          sm.onMessageSent(MessageType.OK);
        }
        case C_NEED_READY -> {
          sender.send(MessageBuilder.ready());
          sm.onMessageSent(MessageType.READY);
        }
        default -> {
          // no ack needed
        }
      }
    } catch (Exception e) {
      System.out.println("[NET] auto-ack failed: " + e.getMessage());
    }
  }

  /**
   * Handles an incoming SHOT from the opponent.
   *
   * <p>Protocol coordinates are 1-based. We convert them to internal 0-based Coordinates.
   *
   * <p>Then we evaluate on our local board: - MISS -> answer 0 - HIT -> answer 1 - SUNK -> answer 2
   *
   * <p>IMPORTANT: - This requires "game" to be set and in VS_PLAYER mode.
   *
   * @param msg parsed SHOT message
   * @author WoFabian
   */
  private void handleIncomingShot(Message msg) {
    if (game == null) {
      System.out.println("[NET] received SHOT but game is null -> cannot evaluate");
      return;
    }

    if (game.getPlayingMode() != PlayingMode.VS_PLAYER) {
      System.out.println("[NET] received SHOT but game is not VS_PLAYER");
      return;
    }

    // Protocol: shot row col (1-based)
    int row1 = msg.getIntArg(0);
    int col1 = msg.getIntArg(1);

    // Convert to internal 0-based
    Coordinates target = new Coordinates(row1 - 1, col1 - 1);

    ShotAttemptResult result = game.player.board.shotAtField(target);

    // If it was a hit, check if ship is sunk
    if (result == ShotAttemptResult.HIT && game.player.board.checkIfShipWasSunk()) {
      result = ShotAttemptResult.SUNK;
    }

    int answerCode = toAnswerCode(result);

    try {
      // ANSWER is a reaction to SHOT -> we send it directly
      sender.send(MessageBuilder.answer(answerCode));

      // We do NOT send PASS here.
      // PASS is sent by the shooter after receiving answer 0 (miss), per protocol example.
    } catch (Exception e) {
      System.out.println("[NET] failed to send ANSWER: " + e.getMessage());
    }
  }

  /**
   * Handles an incoming ANSWER to our previously sent SHOT.
   *
   * <p>- Applies the result to the opponent board (so our local model knows what happened) - If
   * answer == 0 (MISS), we must send PASS (protocol example) and switch turn - If HIT/SUNK, we
   * continue (no pass)
   *
   * @param msg parsed ANSWER message
   * @author WoFabian
   */
  private void handleIncomingAnswer(Message msg) {
    if (game == null) {
      System.out.println("[NET] received ANSWER but game is null");
      return;
    }

    if (game.getPlayingMode() != PlayingMode.VS_PLAYER) {
      System.out.println("[NET] received ANSWER but game is not VS_PLAYER");
      return;
    }

    int a;
    try {
      a = msg.getIntArg(0);
    } catch (Exception e) {
      System.out.println("[NET] invalid ANSWER format");
      return;
    }

    if (lastShotInternal == null) {
      System.out.println("[NET] received ANSWER but lastShotInternal is null");
      return;
    }

    ShotAttemptResult result = fromAnswerCode(a);

    // Apply result to our knowledge about opponent board
    try {
      game.applyOpponentsResponseToPlayersShot(result, lastShotInternal);
    } catch (Exception e) {
      System.out.println("[NET] applyOpponentsResponseToPlayersShot failed: " + e.getMessage());
    }

    // If MISS -> shooter must PASS to switch turn
    if (a == 0) {
      try {
        // Allowed in MY_TURN by the updated state machine
        sendTyped(MessageType.PASS, MessageBuilder.pass());

        // Also switch local game turn
        game.switchTurn();
      } catch (Exception e) {
        System.out.println("[NET] failed to send PASS: " + e.getMessage());
      }
    }

    // If HIT/SUNK: no pass, shooter continues (no turn switch)
  }

  /**
   * Handles an incoming PASS from opponent.
   *
   * <p>PASS means the opponent missed and is giving us the turn.
   *
   * @author WoFabian
   */
  private void handleIncomingPass() {
    if (game != null) {
      game.switchTurn();
    }
  }

  /**
   * Maps internal ShotAttemptResult to protocol answer code.
   *
   * <p>Protocol: - 0 = water (MISS) - 1 = hit - 2 = hit + sunk
   *
   * <p>INVALID is not part of protocol; we treat it as MISS (0).
   *
   * @param r result
   * @return protocol answer code (0/1/2)
   * @author WoFabian
   */
  private int toAnswerCode(ShotAttemptResult r) {
    return switch (r) {
      case MISS -> 0;
      case HIT -> 1;
      case SUNK -> 2;
      case INVALID -> 0;
    };
  }

  /**
   * Maps protocol answer code back to ShotAttemptResult.
   *
   * @param a answer code
   * @return ShotAttemptResult
   * @author WoFabian
   */
  private ShotAttemptResult fromAnswerCode(int a) {
    return switch (a) {
      case 0 -> ShotAttemptResult.MISS;
      case 1 -> ShotAttemptResult.HIT;
      case 2 -> ShotAttemptResult.SUNK;
      default -> ShotAttemptResult.INVALID;
    };
  }
}
