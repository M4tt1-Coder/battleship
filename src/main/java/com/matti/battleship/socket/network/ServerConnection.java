package com.matti.battleship.socket.network;
/**
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BooleanSupplier;

public class ServerConnection {

  private ServerSocket serverSocket;
  private SocketConnector connector;

  private String serverName = "Battleship-Server";

  public void setServerName(String name) {
    if (name != null && !name.isBlank()) {
      serverName = name.trim();
    }
  }

  public String getServerName() {
    return serverName;
  }

  public void openServerSocket(int port) throws Exception {
    serverSocket = new ServerSocket(port);
    System.out.println("[SERVER] waiting for connection... (TCP " + port + ")");
  }

   BLOCKING accept
  public void acceptClient(MessageListener listener) throws Exception {
    if (serverSocket == null) throw new IllegalStateException("ServerSocket not opened.");

    Socket client = serverSocket.accept();
    System.out.println("[SERVER] client connected: " + client.getInetAddress());

    connector = new SocketConnector(client);
    connector.setMessageListener(listener);
  }

   BLOCKING (stop via keepRunning)
  public void listenLoop(BooleanSupplier keepRunning) {
    if (connector != null) connector.listenLoop(keepRunning);
  }

  public void send(String msg) throws Exception {
    if (connector != null) connector.sendMessage(msg);
  }

  public void close() {
    try {
      if (connector != null) connector.close();
    } catch (Exception ignored) {
    }

    try {
      if (serverSocket != null) serverSocket.close();
    } catch (Exception ignored) {
    }
  }
}
*/