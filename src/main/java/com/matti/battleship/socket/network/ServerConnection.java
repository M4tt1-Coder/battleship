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
 * This class starts:
 * - a UDP discovery responder (so clients can find this server in the LAN)
 * - a TCP server socket that blocks on accept() until a client connects
 *
 * After a client connects, the server is marked as "busy" so it will stop responding to discovery
 * requests. This prevents other clients from seeing or selecting a server that is already in use.
 *
 * Important methods for GUI integration:
 * - startServer(listener): starts discovery responder and waits for a TCP client connection
 * - send(msg): sends protocol messages to the connected client
 *
 * Note for GUI:
 * startServer(...) blocks internally at accept(). If you call it from JavaFX/Swing UI thread,
 * your UI will freeze. Always run startServer(...) in a background thread/task.
 *
 * @author WoFabian
 */
public class ServerConnection {

    /** Handles the actual TCP send/receive logic and turn logging. */
  private SocketConnector connector;

    /**
     * Busy flag for discovery: true means a client is connected, so we should not respond to discovery.
     * AtomicBoolean is used because discovery runs in a separate thread.
     */
  private final AtomicBoolean busy = new AtomicBoolean(false);

  /** UDP discovery responder instance (runs in its own thread). */
  private ServerDiscoveryResponder discovery;

    /**
     * Starts the server:
     * - reads the port from EnvConfig
     * - starts the UDP discovery responder in a daemon thread
     * - opens a TCP ServerSocket and waits for one client via accept()
     * - wraps the listener so we can reset "busy" when the client disconnects
     *
     * @param listener callback receiving all incoming protocol lines from the connected client
     * @throws Exception if sockets cannot be created or binding fails
     * @author WoFabian
     */
  public void startServer(MessageListener listener) throws Exception {
    int port = EnvConfig.getPort();

      // Start UDP discovery responder in the background so clients can find this server.
    discovery = new ServerDiscoveryResponder(port, busy, "Battleship-Server");
    Thread discoveryThread = new Thread(discovery, "Discovery-Responder");
    discoveryThread.setDaemon(true);
    discoveryThread.start();

    // Start TCP server socket. This will block at accept() until a client connects.
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("[SERVER] wartet auf Verbindung... (TCP " + port + ")");

    // accept() blocks here until a client connects
    Socket client = serverSocket.accept();

    // Mark server as busy, so discovery will no longer respond to DISCOVER requests.
    busy.set(true);

    System.out.println("[SERVER] Client verbunden: " + client.getInetAddress());

    // Wrap the listener so we can reset busy when the client disconnects or an error happens.
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

    // Create the connector which handles send/receive in a background thread.
    connector = new SocketConnector(client, new TurnLog(TurnLog.Side.SERVER));
    connector.setMessageListener(wrapped);
    connector.startListening();
  }

  /**
   * Sends a raw protocol message to the connected client.
   *
   * Typical usage:
   * - server sends setup: "size", "ships", "ready"
   * - server sends gameplay: "answer", "pass"
   *
   * @param msg message line to send (one protocol command)
   * @throws Exception if sending fails or no client is connected
   * @author WoFabian
   */
  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }
}
