package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ServerDiscoveryResponder;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the server-side network lifecycle (TCP accept + discovery integration).
 *
 * <p>This class opens a TCP {@link ServerSocket} and blocks on {@code accept()} until a client
 * connects. After a connection exists, it wires a {@link SocketConnector} to forward incoming
 * protocol lines to a {@link MessageListener}.
 *
 * <p>LOGIC-IMPORTANT: The {@code busy} flag is shared with UDP discovery. Once a client is
 * connected, {@code busy == true} so the server will stop responding to discovery requests. When
 * the connection closes, {@code busy} is reset so the server becomes discoverable again.
 *
 * <p>GUI-IMPORTANT: {@link #acceptClient(MessageListener)} blocks on {@code accept()}. Do not call
 * it on the JavaFX/Swing UI thread. Run it in a background thread/task.
 *
 * @author WoFabian
 */
public class ServerConnection {

  /** Handles the actual TCP send/receive logic and turn logging. */
  private SocketConnector connector;

  /** Server-side TCP socket that waits for exactly one client connection via {@code accept()}. */
  private ServerSocket serverSocket;

  /**
   * Busy flag for discovery: true means a client is connected, so we should not respond to
   * discovery. {@link AtomicBoolean} is used because discovery runs in a separate thread.
   */
  private final AtomicBoolean busy = new AtomicBoolean(false);

  /** Readable server name used in UDP discovery responses. */
  private String serverName = "Battleship-Server";

  /**
   * Exposes the busy flag so discovery can share the same connection state.
   *
   * <p>LOGIC-IMPORTANT: This flag is read by the discovery responder thread to decide whether the
   * server should be visible in LAN discovery results.
   *
   * @return busy flag (true = a client is connected)
   * @author WoFabian
   */
  public AtomicBoolean getBusyFlag() {
    return busy;
  }

  /**
   * Sets the server name used for discovery.
   *
   * <p>LOGIC-IMPORTANT: Empty/blank names are ignored so clients always receive a usable server
   * list entry.
   *
   * @param serverName new server name (must not be blank)
   * @author WoFabian
   */
  public void setServerName(String serverName) {
    if (serverName == null || serverName.isBlank()) return;
    this.serverName = serverName.trim();
  }

  /**
   * Opens the TCP {@link ServerSocket} on the configured port.
   *
   * <p>The port is loaded via {@link EnvConfig#getPort()} so the server and discovery responder
   * share the same configuration entry.
   *
   * @throws Exception if binding the port fails
   * @author WoFabian
   */
  public void openServerSocket() throws Exception {
    int port = EnvConfig.getPort();
    serverSocket = new ServerSocket(port);
    System.out.println("[SERVER] waiting for connection... (TCP " + port + ")");
  }

  /**
   * Blocks until a client connects and wires the {@link SocketConnector} to the given listener.
   *
   * <p>LOGIC-IMPORTANT: {@code busy} is set to {@code true} after {@code accept()} so the UDP
   * discovery responder stops advertising this server while a game is active.
   *
   * <p>LOGIC-IMPORTANT: The passed listener is wrapped so we can reset {@code busy} on disconnect
   * without duplicating that cleanup logic in the GUI/controller layer.
   *
   * <p>GUI-IMPORTANT: This method blocks on {@code accept()} and must be executed in a background
   * thread/task to avoid freezing the UI.
   *
   * @param listener callback for incoming protocol lines and connection close events
   * @throws Exception if accepting the client fails
   * @author WoFabian
   */
  public void acceptClient(MessageListener listener) throws Exception {
    if (serverSocket == null) throw new IllegalStateException("ServerSocket not opened.");

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

  /**
   * Starts the TCP receive loop on the connected client socket.
   *
   * <p>LOGIC-IMPORTANT: This only works after {@link #acceptClient(MessageListener)} created the
   * {@link SocketConnector}.
   *
   * @author WoFabian
   */
  public void listenLoop() {
    if (connector != null) connector.listenLoop();
  }

  /**
   * Sends a protocol line to the connected client.
   *
   * @param msg one full protocol command line (without newline)
   * @throws Exception if writing to the socket fails
   * @author WoFabian
   */
  public void send(String msg) throws Exception {
    if (connector != null) connector.sendMessage(msg);
  }

  /**
   * Closes the active client connection and the server socket.
   *
   * <p>LOGIC-IMPORTANT: Exceptions are intentionally ignored to keep shutdown paths simple (e.g. UI
   * "Stop Host" or emergency cleanup on errors).
   *
   * @author WoFabian
   */
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

  /**
   * Creates the UDP discovery responder using the configured port, busy flag and server name.
   *
   * <p>GUI-IMPORTANT: The GUI/controller typically runs the responder in a separate thread while
   * waiting in {@link #acceptClient(MessageListener)}.
   *
   * @return new discovery responder instance
   * @author WoFabian
   */
  public ServerDiscoveryResponder createDiscoveryResponder() {
    return new ServerDiscoveryResponder(EnvConfig.getPort(), busy, serverName);
  }
}
