package com.matti.battleship.socket.protocol;

import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.SocketConnector;

/**
 * High-level protocol bridge between the low-level socket connector and the game/protocol logic.
 *
 * <p>This class implements {@link MessageListener} so it can receive raw text lines from {@link
 * SocketConnector}. Every received line is parsed using {@link MessageParser} and forwarded as a
 * {@link Message} object to a {@link ProtocolListener}.
 *
 * <p>This is useful if you want to separate responsibilities: - SocketConnector: raw TCP
 * send/receive + debug logging - ProtocolHandler: parsing strings into Message objects + forwarding
 * events - ProtocolListener: reacts to parsed messages (state machine / gameflow / GUI)
 *
 * @author WoFabian
 */
public class ProtocolHandler implements MessageListener {

  /** Low-level connector used to send/receive raw protocol lines. */
  private final SocketConnector connector;

  /** Listener that receives parsed Message objects and close events. */
  private ProtocolListener listener;

  /**
   * Creates a new ProtocolHandler and registers itself on the given connector.
   *
   * <p>After construction, the handler automatically receives all incoming messages from the
   * socket.
   *
   * @param connector connected socket wrapper used for send/receive
   * @author WoFabian
   */
  public ProtocolHandler(SocketConnector connector) {
    this.connector = connector;

    // Register this handler so we receive raw lines and can parse them.
    connector.setMessageListener(this);
  }

  /**
   * Sets the listener that will receive parsed protocol messages.
   *
   * @param listener protocol listener (may be null to disable forwarding)
   * @author WoFabian
   */
  public void setProtocolListener(ProtocolListener listener) {
    this.listener = listener;
  }

  /**
   * Sends a raw protocol message line to the other side via the connector.
   *
   * @param msg message line to send (one protocol command)
   * @throws Exception if sending fails
   * @author WoFabian
   */
  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  /**
   * Called by {@link SocketConnector} whenever a raw line is received.
   *
   * <p>This method parses the line into a {@link Message} and forwards it to the {@link
   * ProtocolListener}.
   *
   * @param message raw received protocol line
   * @author WoFabian
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
   * @param e the exception that caused the close (may be null depending on implementation)
   * @author WoFabian
   */
  @Override
  public void onConnectionClosed(Exception e) {
    if (listener != null) {
      listener.onClosed(e);
    }
  }
}
