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
 * <p>Hybrid Auto-Acks: - auto: DONE after SIZE/SHIPS, OK after LOAD - NOT auto: READY (GUI decides
 * when player is actually ready)
 *
 * <p>Gameplay: - On SHOT: evaluate and send ANSWER (0/1/2) state-aware - On ANSWER: apply to
 * opponent board; if MISS (0) send PASS state-aware + switch local turn - On PASS: opponent gives
 * us turn -> switch local turn
 */
public class NetworkGameController implements MessageListener {

  /** Minimal sender abstraction so controller does not care whether it's client or server. */
  public interface NetworkSender {
    void send(String msg) throws Exception;
  }

  private final boolean isServer;
  private final NetworkSender sender;
  private final NetworkStateMachine sm;

  private Game game;

  /** Remember last shot we sent (internal 0-based) so we can apply incoming ANSWER to that cell. */
  private Coordinates lastShotInternal = null;

  public NetworkGameController(boolean isServer, NetworkSender sender) {
    this.isServer = isServer;
    this.sender = sender;
    this.sm = new NetworkStateMachine(isServer);
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public NetworkStateMachine getStateMachine() {
    return sm;
  }

  // =========================================================
  // MessageListener
  // =========================================================

  @Override
  public void onMessageReceived(String raw) {
    Message msg = MessageParser.parse(raw);

    // 1) Update protocol state machine first
    sm.onMessageReceived(msg);

    // 2) Auto-ack only DONE/OK (HYBRID)
    tryAutoAck();

    // 3) Gameplay integration
    switch (msg.getType()) {
      case SHOT -> handleIncomingShot(msg);
      case ANSWER -> handleIncomingAnswer(msg);
      case PASS -> handleIncomingPass();
      default -> {
        // other setup messages are handled by your GUI/logic
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

  /** READY is NOT auto-sent. GUI should call this when player really is ready. */
  public void sendReady() throws Exception {
    sendTyped(MessageType.READY, MessageBuilder.ready());
  }

  public void sendLoad(long id) throws Exception {
    sendTyped(MessageType.LOAD, MessageBuilder.load(id));
  }

  /** Sends a shot using INTERNAL (0-based) coordinates. Protocol is 1-based, so we convert +1. */
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
   * HYBRID Auto-ack (client side only): - after SIZE -> DONE - after SHIPS -> DONE - after LOAD ->
   * OK
   *
   * <p>READY is NOT auto-sent; GUI triggers sendReady().
   */
  private void tryAutoAck() {
    if (isServer) return;

    try {
      switch (sm.getState()) {
        case C_NEED_DONE_AFTER_SIZE -> sendTyped(MessageType.DONE, MessageBuilder.done());
        case C_NEED_DONE_AFTER_SHIPS -> sendTyped(MessageType.DONE, MessageBuilder.done());
        case C_NEED_OK_AFTER_LOAD -> sendTyped(MessageType.OK, MessageBuilder.ok());
        default -> {
          // no auto-ack needed
        }
      }
    } catch (Exception e) {
      System.out.println("[NET] auto-ack failed: " + e.getMessage());
    }
  }

  /**
   * Handles an incoming SHOT from the opponent. Protocol uses 1-based coordinates -> convert to
   * 0-based and evaluate on local board. Then send ANSWER (0/1/2) via state-aware sendTyped.
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

    int row1 = msg.getIntArg(0);
    int col1 = msg.getIntArg(1);

    Coordinates target = new Coordinates(row1 - 1, col1 - 1);

    ShotAttemptResult result = game.player.board.shotAtField(target);
    if (result == ShotAttemptResult.HIT && game.player.board.checkIfShipWasSunk()) {
      result = ShotAttemptResult.SUNK;
    }

    int answerCode = toAnswerCode(result);

    try {
      // MUST be state-aware (NEED_SEND_ANSWER -> ANSWER)
      sendTyped(MessageType.ANSWER, MessageBuilder.answer(answerCode));
    } catch (Exception e) {
      System.out.println("[NET] failed to send ANSWER: " + e.getMessage());
    }
  }

  /**
   * Handles an incoming ANSWER to our previously sent SHOT. Applies result to opponent board. If
   * MISS (0), shooter MUST send PASS.
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

    try {
      game.applyOpponentsResponseToPlayersShot(result, lastShotInternal);
    } catch (Exception e) {
      System.out.println("[NET] applyOpponentsResponseToPlayersShot failed: " + e.getMessage());
    }

    // MISS -> send PASS and switch local game turn
    if (a == 0) {
      try {
        sendTyped(MessageType.PASS, MessageBuilder.pass());
        game.switchTurn();
      } catch (Exception e) {
        System.out.println("[NET] failed to send PASS: " + e.getMessage());
      }
    }
    // HIT/SUNK: no pass, shooter continues (state machine keeps MY_TURN)
  }

  /** Handles an incoming PASS from opponent -> opponent missed and gives us the turn. */
  private void handleIncomingPass() {
    if (game != null) {
      game.switchTurn();
    }
  }

  private int toAnswerCode(ShotAttemptResult r) {
    return switch (r) {
      case MISS -> 0;
      case HIT -> 1;
      case SUNK -> 2;
      case INVALID -> 0;
    };
  }

  private ShotAttemptResult fromAnswerCode(int a) {
    return switch (a) {
      case 0 -> ShotAttemptResult.MISS;
      case 1 -> ShotAttemptResult.HIT;
      case 2 -> ShotAttemptResult.SUNK;
      default -> ShotAttemptResult.INVALID;
    };
  }
}
