package com.matti.battleship.socket.network;

import java.net.Socket;
import socket.config.EnvConfig;
import socket.logging.TurnLog;

public class ClientConnection {

  private SocketConnector connector;

  public void connect(String host, MessageListener listener) throws Exception {
    Socket socket = new Socket(host, EnvConfig.getPort());
    System.out.println("[CLIENT] verbunden mit Server: " + host);

    TurnLog log = new TurnLog(TurnLog.Side.CLIENT);
    connector = new SocketConnector(socket, log);

    connector.setMessageListener(listener);
    connector.startListening();
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  public void disconnect() {
    if (connector != null) connector.close();
  }
}
