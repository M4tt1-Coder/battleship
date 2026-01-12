package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {

  private SocketConnector connector;

  public void startServer(MessageListener listener) throws Exception {
    ServerSocket serverSocket = new ServerSocket(EnvConfig.getPort());
    System.out.println("[SERVER] wartet auf Verbindung...");

    Socket client = serverSocket.accept();
    System.out.println("[SERVER] Client verbunden: " + client.getInetAddress());

    TurnLog log = new TurnLog(TurnLog.Side.SERVER);
    connector = new SocketConnector(client, log);

    connector.setMessageListener(listener);
    connector.startListening();
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  public void stop() {
    if (connector != null) connector.close();
  }
}
