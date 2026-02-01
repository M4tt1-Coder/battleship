package com.matti.battleship.socket.state;

import com.matti.battleship.socket.network.IMessageListener;
import com.matti.battleship.socket.protocol.Message;
import com.matti.battleship.socket.protocol.MessageBuilder;
import com.matti.battleship.socket.protocol.MessageParser;
import com.matti.battleship.socket.protocol.MessageType;

/**
 * Bridge between raw socket lines and the {@link NetworkStateMachine}.
 *
 * <p>This controller implements {@link IMessageListener} so it can receive raw protocol lines from
 * the network layer. Incoming lines are parsed into {@link Message} objects and forwarded to the
 * {@link NetworkStateMachine}.
 *
 * <p>GUI-IMPORTANT: There are no automatic acknowledgements anymore. The GUI / game logic must
 * explicitly call {@link #sendDone()}, {@link #sendOk()} and {@link #sendReady()} when appropriate.
 *
 * <p>LOGIC-IMPORTANT: Gameplay actions are intentionally NOT implemented here. This class only
 * validates and advances the protocol/setup phase. The actual gameflow (shots, answers, pass, etc.)
 * belongs to your gameplay/controller layer.
 *
 * @author WoFabian
 */
public class NetworkGameController implements IMessageListener {

  /**
   * Minimal sending abstraction used by this controller.
   *
   * <p>LOGIC-IMPORTANT: We keep this interface tiny so the controller can be used with different
   * networking implementations (client/server, tests, mocks) without depending on a concrete
   * connector class.
   */
  public interface NetworkSender {
    void send(String msg) throws Exception;
  }

  /** True if this controller runs on the server side (affects state machine rules). */
  private final boolean isServer;

  /** Sender used to write raw protocol lines to the network. */
  private final NetworkSender sender;

  /** Protocol state machine that validates message order and tracks current state. */
  private final NetworkStateMachine sm;

  /**
   * Creates a new controller for the given side and sender.
   *
   * @param isServer true if this instance controls the server side
   * @param sender abstraction used to send raw protocol lines
   * @author WoFabian
   */
  public NetworkGameController(boolean isServer, NetworkSender sender) {
    this.isServer = isServer;
    this.sender = sender;
    this.sm = new NetworkStateMachine(isServer);
  }

  /**
   * Exposes the underlying state machine for UI/debugging.
   *
   * <p>GUI-OPTIONAL: The GUI may use this to show connection/protocol progress to the user.
   *
   * @return state machine instance used by this controller
   * @author WoFabian
   */
  public NetworkStateMachine getStateMachine() {
    return sm;
  }

  /**
   * Receives a raw protocol line from the network and forwards it to the state machine.
   *
   * <p>LOGIC-IMPORTANT: No auto-acks and no gameplay actions are executed here. Higher layers
   * decide what to do after the state machine accepted the message.
   *
   * @param raw raw received protocol line
   * @author WoFabian
   */
  @Override
  public void onMessageReceived(String raw) {
    Message msg = MessageParser.parse(raw);
    sm.onMessageReceived(msg);
    // No auto-acks, no gameplay actions.
  }

  /**
   * Called when the connection closes or an error occurs.
   *
   * <p>GUI-OPTIONAL: In a GUI application, this should typically trigger a user-visible error
   * dialog or a transition back to a "main menu" state.
   *
   * @param e error cause (may be null depending on implementation)
   * @author WoFabian
   */
  @Override
  public void onConnectionClosed(Exception e) {
    System.out.println("[NET] connection closed: " + (e != null ? e.getMessage() : "null"));
  }

  // ===== GUI/Logic sending =====

  /**
   * Sends {@code size <n>} (server-driven setup step).
   *
   * @param size board size
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendSize(int size) throws Exception {
    sendTyped(MessageType.SIZE, MessageBuilder.size(size));
  }

  /**
   * Sends {@code ships ...} (server-driven setup step).
   *
   * @param ships list of ship lengths
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendShips(int... ships) throws Exception {
    sendTyped(MessageType.SHIPS, MessageBuilder.ships(ships));
  }

  /**
   * Sends {@code load <id>} (server-driven load path).
   *
   * @param id load identifier
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendLoad(long id) throws Exception {
    sendTyped(MessageType.LOAD, MessageBuilder.load(id));
  }

  /**
   * Sends {@code done} as acknowledgement for the last setup command.
   *
   * <p>GUI-IMPORTANT: Call this when the GUI/game logic completed the requested step (e.g. after
   * applying size/ships).
   *
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendDone() throws Exception {
    sendTyped(MessageType.DONE, MessageBuilder.done());
  }

  /**
   * Sends {@code ok} as acknowledgement for the load path.
   *
   * <p>GUI-IMPORTANT: Call this after applying the loaded state / confirming the load request.
   *
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendOk() throws Exception {
    sendTyped(MessageType.OK, MessageBuilder.ok());
  }

  /**
   * Sends {@code ready} to finish the setup handshake.
   *
   * <p>GUI-IMPORTANT: Call this when the local player is ready to start gameplay.
   *
   * @throws Exception if sending fails or the protocol state does not allow it
   * @author WoFabian
   */
  public void sendReady() throws Exception {
    sendTyped(MessageType.READY, MessageBuilder.ready());
  }

  /**
   * Sends a typed protocol command after validating it against the current state.
   *
   * <p>LOGIC-IMPORTANT: The state machine is updated in two steps:
   *
   * <p>1) validate with {@link NetworkStateMachine#canSend(MessageType)}
   *
   * <p>2) after sending, advance via {@link NetworkStateMachine#onMessageSent(MessageType)}
   *
   * @param type message type used for protocol validation
   * @param raw raw protocol line to send
   * @throws Exception if sending fails or the protocol state does not allow this message
   * @author WoFabian
   */
  private void sendTyped(MessageType type, String raw) throws Exception {
    if (!sm.canSend(type)) {
      throw new IllegalStateException(
          "Cannot send " + type + " in state " + sm.getState() + " (isServer=" + isServer + ")");
    }

    sender.send(raw);
    sm.onMessageSent(type);
  }
}
