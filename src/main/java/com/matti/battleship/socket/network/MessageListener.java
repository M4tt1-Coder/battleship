package com.matti.battleship.socket.network;

public interface MessageListener {
  // wird aufgerufen, sobald eine Zeile empfangen wurde
  void onMessageReceived(String message);

  // wenn die verbindung geschlossen wurde
  void onConnectionClosed(Exception e);
}
