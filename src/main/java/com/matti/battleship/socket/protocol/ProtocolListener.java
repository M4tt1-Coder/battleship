package com.matti.battleship.socket.protocol;

public interface ProtocolListener {

  void onMessage(Message msg);

  void onClosed(Exception e);
}
