package com.matti.battleship.socket.network;

/**
 * GUI / LOGIC IMPORTANT: Callback interface used by the socket layer to deliver incoming messages.
 *
 * <p>Every time the network receives one complete protocol line, {@link #onMessageReceived(String)}
 * is called. If the connection is closed or an error occurs, {@link #onConnectionClosed(Exception)}
 * is called.
 *
 * <p>Typical GUI usage: The GUI or a controller registers an implementation of this interface when
 * connecting. The implementation can forward received lines to the protocol parser and state
 * machine, and update the GUI accordingly (for example enabling buttons when "ready" is reached).
 *
 * <p>Typical logic usage: A GameFlow/NetworkGameController can implement this interface to parse
 * messages and react by sending responses (for example answering shots or sending
 * acknowledgements).
 *
 * @author WoFabian
 */
public interface IMessageListener {

  /**
   * Called whenever a full protocol line/message was received.
   *
   * @param message raw received line (one protocol command)
   * @author WoFabian
   */
  void onMessageReceived(String message);

  /**
   * Called when the connection was closed or an error occurred.
   *
   * @param e exception that caused the close (may be null depending on implementation)
   * @author WoFabian
   */
  void onConnectionClosed(Exception e);
}
