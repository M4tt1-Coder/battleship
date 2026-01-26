/**package com.matti.battleship.socket.test.discoverytest.standard;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.SocketConnector;
import java.net.ServerSocket;
import java.net.Socket;

public class StandardServerConnection {

  private SocketConnector connector;

  public void startServer(MessageListener listener) throws Exception {
    int port = EnvConfig.getPort();

    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("[SERVER-STANDARD] wartet auf Verbindung... (TCP " + port + ")");

    Socket client = serverSocket.accept();
    System.out.println("[SERVER-STANDARD] Client verbunden: " + client.getInetAddress());

    connector = new SocketConnector(client, new TurnLog(TurnLog.Side.SERVER));
    connector.setMessageListener(listener);
    connector.startListening();
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }
}
*/
