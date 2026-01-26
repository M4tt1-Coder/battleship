/**package com.matti.battleship.socket.test.discoverytest.standard;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.SocketConnector;
import java.net.Socket;

public class StandardClientConnection {

  private SocketConnector connector;

  public void connect(String host, MessageListener listener) throws Exception {
    int port = EnvConfig.getPort();

    Socket socket = new Socket(host, port);
    System.out.println("[CLIENT-STANDARD] verbunden mit Server: " + host + " (TCP " + port + ")");

    connector = new SocketConnector(socket, new TurnLog(TurnLog.Side.CLIENT));
    connector.setMessageListener(listener);
    connector.startListening();
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }
}
*/