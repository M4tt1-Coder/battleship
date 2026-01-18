package com.matti.battleship.socket.network;

/**
 * GUI / LOGIC IMPORTANT:
 * Callback interface used to receive messages from the network.
 * Important methods:
 * - onMessageReceived(msg): called for every incoming line/message
 * - onConnectionClosed(e): called when the socket closes or an error happens
 * Typical GUI usage:
 * - Update UI state when "ready" is received (connected indicator)
 * - Forward messages to GameFlow/Controller (MessageParser + state machine)
 *
 * @author WoFabian
 */
public interface MessageListener {
  // wird aufgerufen, sobald eine Zeile empfangen wurde
  void onMessageReceived(String message);

  // wenn die verbindung geschlossen wurde
  void onConnectionClosed(Exception e);
}
