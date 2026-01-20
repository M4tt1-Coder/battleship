package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.Socket;

/**
 * GUI-IMPORTANT: Used by the GUI to connect to a selected server (TCP). Important methods: -
 * connect(host, listener): establishes TCP connection to the host - send(msg): sends protocol
 * messages, e.g. "ready" after connecting Typical GUI flow: - On "Connect" button:
 * client.connect(selected.host(), listener) - After connect: client.send("ready") to verify
 * connection
 *
 * @author WoFabian
 */
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
