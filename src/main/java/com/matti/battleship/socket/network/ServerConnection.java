package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ServerDiscoveryResponder;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerConnection {

  private SocketConnector connector;

  // Busy-Flag: true sobald ein Client wirklich verbunden ist
  private final AtomicBoolean busy = new AtomicBoolean(false);

  // Discovery nebenher laufen lassen
  private ServerDiscoveryResponder discovery;

  public void startServer(MessageListener listener) throws Exception {
    int port = EnvConfig.getPort();

    // 1) Discovery starten (parallel)
    discovery = new ServerDiscoveryResponder(port, busy, "Battleship-Server");
    Thread discoveryThread = new Thread(discovery, "Discovery-Responder");
    discoveryThread.setDaemon(true);
    discoveryThread.start();

    // 2) TCP-Server normal starten (blockierend)
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("[SERVER] wartet auf Verbindung... (TCP " + port + ")");

    Socket client = serverSocket.accept();
    busy.set(true); // ab jetzt ist Server belegt -> verschwindet aus Discovery-Liste

    System.out.println("[SERVER] Client verbunden: " + client.getInetAddress());

    // Listener wrappen, damit busy zurückgesetzt wird, wenn Verbindung endet
    MessageListener wrapped =
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            listener.onMessageReceived(message);
          }

          @Override
          public void onConnectionClosed(Exception e) {
            busy.set(false);
            listener.onConnectionClosed(e);
          }
        };

    connector = new SocketConnector(client, new TurnLog(TurnLog.Side.SERVER));
    connector.setMessageListener(wrapped);
    connector.startListening();
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }
}
