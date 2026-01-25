package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ServerDiscoveryResponder;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GUI-OPTIONAL (Host-Button): Used if the GUI allows starting a server locally. Important methods:
 * - startServer(listener): starts UDP discovery responder + TCP accept (blocks internally)
 *
 * <p>This class starts: - a UDP discovery responder (so clients can find this server in the LAN) -
 * a TCP server socket that blocks on accept() until a client connects
 *
 * <p>After a client connects, the server is marked as "busy" so it will stop responding to
 * discovery requests. This prevents other clients from seeing or selecting a server that is already
 * in use.
 *
 * <p>Important methods for GUI integration: - startServer(listener): starts discovery responder and
 * waits for a TCP client connection - send(msg): sends protocol messages to the connected client
 *
 * <p>Note for GUI: startServer(...) blocks internally at accept(). If you call it from JavaFX/Swing
 * UI thread, your UI will freeze. Always run startServer(...) in a background thread/task.
 *
 * @author WoFabian
 */
public class ServerConnection {

  /** Handles the actual TCP send/receive logic and turn logging. */
  private SocketConnector connector;

  private ServerSocket serverSocket;
  /**
   * Busy flag for discovery: true means a client is connected, so we should not respond to
   * discovery. AtomicBoolean is used because discovery runs in a separate thread.
   */
  private final AtomicBoolean busy = new AtomicBoolean(false);

  public AtomicBoolean getBusyFlag() {
    return busy;
  }

  public void openServerSocket() throws Exception {
    int port = EnvConfig.getPort();
    serverSocket = new ServerSocket(port);
    System.out.println("[SERVER] wartet auf Verbindung... (TCP " + port + ")");
  }

  public void acceptClient(MessageListener listener) throws Exception {
    if (serverSocket == null) throw new IllegalStateException("ServerSocket nicht geöffnet.");

    Socket client = serverSocket.accept();
    busy.set(true);

    System.out.println("[SERVER] Client verbunden: " + client.getInetAddress());

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
  }

  public void listenLoop() {
    if (connector != null) connector.listenLoop();
  }

  public void send(String msg) throws Exception {
    if (connector != null) connector.sendMessage(msg);
  }

  public void close() {
    try {
      if (connector != null) connector.close();
    } catch (Exception ignored) {}

    try {
      if (serverSocket != null) serverSocket.close();
    } catch (Exception ignored) {}
  }

  public ServerDiscoveryResponder createDiscoveryResponder(String serverName) {
    return new ServerDiscoveryResponder(EnvConfig.getPort(), busy, serverName);
  }
}
