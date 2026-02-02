package com.matti.battleship.socket.protocol;

/**
 * Callback interface for receiving parsed protocol events.
 *
 * <p>Implementations of this interface are typically located in the gameflow/controller layer. A
 * {@link ProtocolHandler} parses raw socket lines into {@link Message} objects and forwards them to
 * this listener.
 *
 * <p>Typical usage: - onMessage(...): update state machine, update game model, trigger GUI updates
 * - onClosed(...): handle disconnect (disable UI, return to menu, show error)
 *
 * @author WoFabian
 */
public interface IProtocolListener {

  /**
   * Called for every parsed protocol message received from the network.
   *
   * @param msg parsed message (type + args)
   */
  void onMessage(Message msg);

  /**
   * Called when the connection closes or an error occurs.
   *
   * @param e exception that caused the close (may be null depending on implementation)
   */
  void onClosed(Exception e);
}
