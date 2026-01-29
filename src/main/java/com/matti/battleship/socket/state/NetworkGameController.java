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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class NetworkGameController implements MessageListener {

  private static final Logger log = LogManager.getLogger(NetworkGameController.class);

  public interface NetworkSender {
    void send(String msg) throws Exception;
  }

  private final boolean isServer;
  private final NetworkSender sender;
  private final NetworkStateMachine sm;

  // Optional: game integration (not used)
  private Game game;
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

    sm.onMessageReceived(msg);

    // switch (msg.getType()) {
    //   case SHOT -> handleIncomingShot(msg);
    //   case ANSWER -> handleIncomingAnswer(msg);
    //   case PASS -> handleIncomingPass();
    //   default -> { }
    // }
  }

  @Override
  public void onConnectionClosed(Exception e) {
    log.warn("[NET] connection closed: {}", (e != null ? e.getMessage() : "null"));
  }

  // =========================================================
  // Manual sending API (GUI/logic must call these)
  // =========================================================

  public void sendSize(int size) throws Exception {
    sendTyped(MessageType.SIZE, MessageBuilder.size(size));
  }

  public void sendShips(int... ships) throws Exception {
    sendTyped(MessageType.SHIPS, MessageBuilder.ships(ships));
  }

  public void sendLoad(long id) throws Exception {
    sendTyped(MessageType.LOAD, MessageBuilder.load(id));
  }

  public void sendDone() throws Exception {
    sendTyped(MessageType.DONE, MessageBuilder.done());
  }

  public void sendOk() throws Exception {
    sendTyped(MessageType.OK, MessageBuilder.ok());
  }

  public void sendReady() throws Exception {
    sendTyped(MessageType.READY, MessageBuilder.ready());
  }

  public void sendShot0Based(int x, int y) throws Exception {
    Coordinates internal = new Coordinates(x, y);
    this.lastShotInternal = internal;

    int row = x + 1;
    int col = y + 1;

    sendTyped(MessageType.SHOT, MessageBuilder.shot(row, col));
  }

  // =========================================================
  // Internal gated sending (state-machine controlled)
  // =========================================================

  private void sendTyped(MessageType type, String raw) throws Exception {
    if (!sm.canSend(type)) {
      throw new IllegalStateException(
              "Cannot send "
                      + type
                      + " in state "
                      + sm.getState()
                      + " (isServer="
                      + isServer
                      + ")");
    }
    sender.send(raw);
    sm.onMessageSent(type);
  }

  // =========================================================
  // Optional gameplay integration(wurde nicht eingebaut)
  // =========================================================

  private void handleIncomingShot(Message msg) {
    if (game == null) {
      log.warn("[NET] received SHOT but game is null -> cannot evaluate");
      return;
    }
    if (game.getPlayingMode() != PlayingMode.VS_PLAYER) {
      log.warn("[NET] received SHOT but game is not VS_PLAYER");
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
      sender.send(MessageBuilder.answer(answerCode));
    } catch (Exception e) {
      log.warn("[NET] failed to send ANSWER: {}", e.getMessage());
    }
  }

  private void handleIncomingAnswer(Message msg) {
    if (game == null) {
      log.warn("[NET] received ANSWER but game is null");
      return;
    }
    if (game.getPlayingMode() != PlayingMode.VS_PLAYER) {
      log.warn("[NET] received ANSWER but game is not VS_PLAYER");
      return;
    }

    int a;
    try {
      a = msg.getIntArg(0);
    } catch (Exception e) {
      log.warn("[NET] invalid ANSWER format");
      return;
    }

    if (lastShotInternal == null) {
      log.warn("[NET] received ANSWER but lastShotInternal is null");
      return;
    }

    ShotAttemptResult result = fromAnswerCode(a);

    try {
      game.applyOpponentsResponseToPlayersShot(result, lastShotInternal);
    } catch (Exception e) {
      log.warn("[NET] applyOpponentsResponseToPlayersShot failed: {}", e.getMessage());
    }

    // If you want PASS manual later: remove this and let GUI/logic send pass.
    if (a == 0) {
      try {
        sender.send(MessageBuilder.pass());
        game.switchTurn();
      } catch (Exception e) {
        log.warn("[NET] failed to send PASS: {}", e.getMessage());
      }
    }
  }

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
