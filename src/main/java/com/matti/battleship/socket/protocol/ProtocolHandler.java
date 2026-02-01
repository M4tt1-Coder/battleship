package com.matti.battleship.socket.protocol;

import com.matti.battleship.socket.network.IMessageListener;
import com.matti.battleship.socket.network.SocketConnector;

/**
 * High-level protocol bridge between raw socket IO and game/protocol logic.
 *
 * <p>This class implements {@link IMessageListener} so it can receive raw text lines from {@link
 * SocketConnector}. Each received line is parsed using {@link MessageParser} and forwarded as a
 * {@link Message} object to an {@link IProtocolListener}.
 *
 * <p>LOGIC-IMPORTANT: This is the separation point between "strings over TCP" and "typed protocol
 * events". Higher layers (state machine / controller / GUI) should not need to manually parse raw
 * strings.
 *
 * <p>Architecture overview (responsibilities):
 *
 * <p>{@link SocketConnector}: raw TCP send/receive + optional debug/turn logging
 *
 * <p>{@link ProtocolHandler}: parse raw lines into {@link Message} objects + forward events
 *
 * <p>{@link IProtocolListener}: reacts to parsed messages (state machine / gameflow / GUI)
 *
 * @author WoFabian
 */
public class ProtocolHandler implements IMessageListener {

  /** Low-level connector used to send/receive raw protocol lines. */
  private final SocketConnector connector;

  /** Listener that receives parsed {@link Message} objects and close events. */
  private IProtocolListener listener;

  /**
   * Creates a new ProtocolHandler and registers itself on the given connector.
   *
   * <p>After construction, the handler automatically receives all incoming messages from the socket
   * because it registers itself as the connector's {@link IMessageListener}.
   *
   * @param connector connected socket wrapper used for send/receive
   */
  public ProtocolHandler(SocketConnector connector) {
    this.connector = connector;

    // Register this handler so we receive raw lines and can parse them centrally.
    connector.setMessageListener(this);
  }

  /**
   * Sets the listener that will receive parsed protocol messages.
   *
   * <p>LOGIC-IMPORTANT: Setting this to {@code null} effectively disables forwarding and can be
   * used during shutdown or when the game logic is not yet ready to process messages.
   *
   * @param listener protocol listener (may be null to disable forwarding)
   */
  public void setProtocolListener(IProtocolListener listener) {
    this.listener = listener;
  }

  /**
   * Sends a raw protocol message line to the other side via the connector.
   *
   * <p>LOGIC-IMPORTANT: This method does not validate the message; it assumes higher layers
   * construct valid protocol lines (e.g. via MessageBuilder).
   *
   * @param msg message line to send (one protocol command)
   * @throws Exception if sending fails
   */
  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  /**
   * Called by {@link SocketConnector} whenever a raw line is received.
   *
   * <p>This method parses the line into a {@link Message} and forwards it to the {@link
   * IProtocolListener}.
   *
   * <p>LOGIC-IMPORTANT: Parsing is centralized here so other layers operate on {@link Message}
   * objects instead of raw strings.
   *
   * @param message raw received protocol line
   */
  @Override
  public void onMessageReceived(String message) {
    if (listener != null) {
      listener.onMessage(MessageParser.parse(message));
    }
  }

  /**
   * Called by {@link SocketConnector} when the connection closes or an error occurs.
   *
   * <p>LOGIC-IMPORTANT: Close events are forwarded unchanged so higher layers can decide whether to
   * show an error dialog, return to a menu, or attempt reconnect logic.
   *
   * @param e the exception that caused the close (may be null depending on implementation)
   */
  @Override
  public void onConnectionClosed(Exception e) {
    if (listener != null) {
      listener.onClosed(e);
    }
  }
}
