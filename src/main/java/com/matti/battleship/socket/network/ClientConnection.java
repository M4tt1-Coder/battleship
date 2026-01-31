package com.matti.battleship.socket.network;
/**
import java.net.Socket;
import java.util.function.BooleanSupplier;

public class ClientConnection {

  private SocketConnector connector;

  public void connect(String host, int port, MessageListener listener) throws Exception {
    Socket socket = new Socket(host, port);
    connector = new SocketConnector(socket);
    connector.setMessageListener(listener);
    System.out.println("[CLIENT] connected: " + host + ":" + port);
  }

   BLOCKING (stop via keepRunning)
  public void listenLoop(BooleanSupplier keepRunning) {
    if (connector != null) connector.listenLoop(keepRunning);
  }

  public void send(String msg) throws Exception {
    if (connector != null) connector.sendMessage(msg);
  }

  public void disconnect() {
    if (connector != null) connector.close();
  }
}
*/