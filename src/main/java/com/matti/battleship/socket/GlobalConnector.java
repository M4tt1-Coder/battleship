package com.matti.battleship.socket;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ClientDiscoveryScanner;
import com.matti.battleship.socket.discovery.ServerDiscoveryResponder;
import com.matti.battleship.socket.logging.TurnLog;
import com.matti.battleship.socket.network.IMessageListener;
import com.matti.battleship.socket.network.SocketConnector;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central network entry point for GUI/logic: client + server lifecycle in one place.
 *
 * <p>This class replaces separate ClientConnection/ServerConnection by exposing a unified interface
 * for:
 *
 * <p>- starting as client (connect to host)
 *
 * <p>- starting as server (open TCP + accept client)
 *
 * <p>- UDP discovery creation (server responder + client scanner)
 *
 * <p>- sending messages and controlling the {@link SocketConnector} listening lifecycle
 *
 * <p>GUI-IMPORTANT: The GUI should only talk to this class for networking. It hides lower-level
 * details such as {@link ServerSocket}, {@link SocketConnector}, and the discovery busy flag.
 *
 * <p>LOGIC-IMPORTANT: The {@code busy} flag is shared with UDP discovery. It is set to {@code true}
 * after a TCP client connects and reset to {@code false} on a real connection close. This prevents
 * clients from seeing/selecting a server that is already in use.
 *
 * @author WoFabian
 */
public final class GlobalConnector {

  /**
   * Current network role of this connector instance.
   *
   * <p>LOGIC-IMPORTANT: Role controls which calls are allowed (e.g. acceptClient() only for
   * SERVER).
   */
  public enum Role {
    NONE,
    CLIENT,
    SERVER
  }

  /** Current role. Volatile because GUI and network threads may read/write this. */
  private volatile Role role = Role.NONE;

  /** Active low-level connector used for TCP send/receive once a socket is connected. */
  private SocketConnector connector;

  // ===== Server-only =====

  /** Server-side TCP socket that blocks on accept() until a client connects. */
  private ServerSocket serverSocket;

  /**
   * Busy flag shared with UDP discovery.
   *
   * <p>LOGIC-IMPORTANT: If true, the discovery responder ignores DISCOVER requests to hide this
   * server while a game connection exists.
   */
  private final AtomicBoolean busy = new AtomicBoolean(false);

  /** Readable server name used in discovery responses. */
  private String serverName = "Battleship-Server";

  /**
   * Optional discovery responder instance.
   *
   * <p>LOGIC-IMPORTANT: Its runLoop is executed externally (thread lifecycle is controlled by the
   * caller).
   */
  private ServerDiscoveryResponder discoveryResponder;

  // ===== Listener wiring =====

  /** Listener provided by GUI/logic (may be null). */
  private volatile IMessageListener externalListener;

  /**
   * Internal listener that is always installed on the {@link SocketConnector}.
   *
   * <p>LOGIC-IMPORTANT: This keeps shared connection state consistent (especially busy flag) and
   * forwards events to the external listener if one is installed.
   */
  private final IMessageListener internalListener =
      new IMessageListener() {
        @Override
        public void onMessageReceived(String message) {
          IMessageListener l = externalListener;
          if (l != null) l.onMessageReceived(message);
        }

        @Override
        public void onConnectionClosed(Exception e) {
          // busy only resets on a real connection close (not on "stop listening").
          busy.set(false);

          IMessageListener l = externalListener;
          if (l != null) l.onConnectionClosed(e);
        }
      };

  /**
   * Makes a GUI/logic listener that receives raw incoming lines and close events.
   *
   * <p>GUI-IMPORTANT: This does not start any networking by itself. It only defines where incoming
   * messages are forwarded once a connector is active.
   *
   * @param listener external listener (may be null)
   */
  public void setMessageListener(IMessageListener listener) {
    this.externalListener = listener;
  }

  /**
   * Returns the current role of this connector.
   *
   * @return current role
   */
  public Role getRole() {
    return role;
  }

  /**
   * Returns whether the server is currently busy (a client is connected).
   *
   * @return true if a client is connected (server-side busy state)
   */
  public boolean isBusy() {
    return busy.get();
  }

  /**
   * Exposes the busy flag so discovery components can share the same state.
   *
   * @return atomic busy flag
   */
  public AtomicBoolean getBusyFlag() {
    return busy;
  }

  /**
   * Returns the current server name used for UDP discovery.
   *
   * @return server name
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Sets the server name used for UDP discovery.
   *
   * <p>LOGIC-IMPORTANT: Empty/blank names are ignored so clients always receive a usable entry.
   *
   * @param name server name (must not be blank)
   */
  public void setServerName(String name) {
    if (name == null || name.isBlank()) return;
    this.serverName = name.trim();
  }

  // ===== Client =====

  /**
   * Starts this connector in CLIENT role and connects to the given host.
   *
   * <p>LOGIC-IMPORTANT: Any existing connector/server socket is cleaned up first. This allows
   * re-connecting without needing a new GlobalConnector instance.
   *
   * @param host target host (IP or hostname)
   * @throws Exception if connecting or initializing the connector fails
   */
  public synchronized void startAsClient(String host) throws Exception {
    Objects.requireNonNull(host, "host");
    resetConnectionOnly();
    role = Role.CLIENT;
    busy.set(false);

    int port = EnvConfig.getPort();
    Socket socket = new Socket(host, port);

    System.out.println("[CLIENT] connected: " + host + ":" + port);

    TurnLog log = new TurnLog(TurnLog.Side.CLIENT);
    connector = new SocketConnector(socket, log);
    connector.setMessageListener(internalListener);
  }

  /**
   * Convenience alias for {@link #startAsClient(String)}.
   *
   * @param host target host
   * @throws Exception if connecting fails
   */
  public void connectToServer(String host) throws Exception {
    startAsClient(host);
  }

  // ===== Server =====

  /**
   * Starts this connector in SERVER role and opens the TCP server socket.
   *
   * <p>GUI-IMPORTANT: This does not accept a client yet. Call {@link #acceptClient()} separately.
   *
   * @param name server name used for discovery (ignored if blank)
   * @throws Exception if the server socket cannot be opened
   */
  public synchronized void startAsServer(String name) throws Exception {
    setServerName(name);
    resetConnectionOnly();
    role = Role.SERVER;
    openServerSocket();
  }

  /**
   * Opens the TCP {@link ServerSocket} on the configured port.
   *
   * <p>LOGIC-IMPORTANT: This resets {@code busy} to false so discovery can advertise the server.
   *
   * @throws Exception if binding the port fails
   */
  public synchronized void openServerSocket() throws Exception {
    ensureRole(Role.SERVER);

    int port = EnvConfig.getPort();
    serverSocket = new ServerSocket(port);
    busy.set(false);

    System.out.println("[SERVER] waiting for connection... (TCP " + port + ")");
  }

  /**
   * Blocks until a TCP client connects and creates the {@link SocketConnector}.
   *
   * <p>GUI-IMPORTANT: This method blocks on {@code accept()} and must be executed in a background
   * task to avoid freezing the UI.
   *
   * <p>LOGIC-IMPORTANT: After accept, {@code busy} is set to true so UDP discovery stops
   * advertising this server while the connection is active.
   *
   * @throws Exception if accepting the client fails
   */
  public synchronized void acceptClient() throws Exception {
    ensureRole(Role.SERVER);
    if (serverSocket == null) throw new IllegalStateException("ServerSocket not opened.");

    Socket client = serverSocket.accept();
    busy.set(true);

    System.out.println("[SERVER] client connected: " + client.getInetAddress());

    TurnLog log = new TurnLog(TurnLog.Side.SERVER);
    connector = new SocketConnector(client, log);
    connector.setMessageListener(internalListener);
  }

  /**
   * Creates a UDP discovery responder for the current server configuration.
   *
   * <p>LOGIC-IMPORTANT: The responder shares the {@code busy} flag and therefore automatically
   * stops answering discovery requests once a client is connected.
   *
   * <p>GUI-IMPORTANT: The responder's {@code runLoop()} must be executed externally (typically in a
   * separate thread).
   *
   * @return newly created discovery responder
   */
  public synchronized ServerDiscoveryResponder createDiscoveryResponder() {
    ensureRole(Role.SERVER);
    discoveryResponder = new ServerDiscoveryResponder(EnvConfig.getPort(), busy, serverName);
    return discoveryResponder;
  }

  /**
   * Returns the current discovery responder instance if one was created.
   *
   * @return discovery responder or null
   */
  public synchronized ServerDiscoveryResponder getDiscoveryResponder() {
    return discoveryResponder;
  }

  /**
   * Creates a client-side UDP discovery scanner for LAN server search.
   *
   * <p>GUI-IMPORTANT: The GUI typically uses this for a "Find Servers" / "Refresh List" action.
   *
   * @return new discovery scanner
   */
  public ClientDiscoveryScanner createDiscoveryScanner() {
    return new ClientDiscoveryScanner(EnvConfig.getPort());
  }

  // ===== Messaging =====

  /**
   * Sends a raw protocol line through the active {@link SocketConnector}.
   *
   * @param msg one full protocol command line (without newline)
   * @throws Exception if no connection exists or sending fails
   */
  public void sendMessage(String msg) throws Exception {
    requireConnector().sendMessage(msg);
  }

  /**
   * Starts the blocking receive loop on the active connector.
   *
   * <p>GUI-IMPORTANT: This is blocking; run it in a background thread/task.
   */
  public void listenLoop() {
    requireConnector().listenLoop();
  }

  /** Enables processing in the connector listen loop without closing the socket (Task 2). */
  public void requestStartListening() {
    requireConnector().requestStartListening();
  }

  /** Requests the connector listen loop to exit without closing the socket (Task 2). */
  public void requestStopListening() {
    requireConnector().requestStopListening();
  }

  // ===== Close / Reset =====

  /**
   * Closes discovery, connector and server socket and resets this instance to {@link Role#NONE}.
   *
   * <p>LOGIC-IMPORTANT: This is a full shutdown. Use {@link #resetConnectionOnly()} for "reconnect"
   * use cases where the role will be set again immediately.
   */
  public synchronized void close() {
    stopDiscovery();
    closeConnector();
    closeServerSocket();
    busy.set(false);
    role = Role.NONE;
  }

  /**
   * Resets the active connection resources without changing the role.
   *
   * <p>LOGIC-IMPORTANT: Used internally before switching roles (client/server) so stale sockets and
   * responder threads do not leak across reconnect attempts.
   */
  private synchronized void resetConnectionOnly() {
    stopDiscovery();
    closeConnector();
    closeServerSocket();
    busy.set(false);
  }

  /**
   * Stops the discovery responder if it exists.
   *
   * <p>LOGIC-IMPORTANT: Only signals stop. The responder will exit its loop after the next wake-up
   * / packet or socket close (depending on its implementation).
   */
  private void stopDiscovery() {
    if (discoveryResponder != null) {
      discoveryResponder.stop();
      discoveryResponder = null;
    }
  }

  /** Closes the active connector if present and clears the reference. */
  private void closeConnector() {
    try {
      if (connector != null) connector.close();
    } catch (Exception ignored) {
    }
    connector = null;
  }

  /** Closes the server socket if present and clears the reference. */
  private void closeServerSocket() {
    try {
      if (serverSocket != null) serverSocket.close();
    } catch (Exception ignored) {
    }
    serverSocket = null;
  }

  /**
   * Ensures the current role matches the expected role.
   *
   * <p>LOGIC-IMPORTANT: This prevents invalid API usage (e.g. calling acceptClient while in CLIENT
   * role).
   *
   * @param expected expected role
   */
  private void ensureRole(Role expected) {
    if (role != expected) {
      throw new IllegalStateException("Invalid role. Expected " + expected + " but was " + role);
    }
  }

  /**
   * Returns the active connector or throws if not connected.
   *
   * <p>GUI-IMPORTANT: If this throws, the GUI should treat it as "not connected" and update UI
   * state.
   *
   * @return active socket connector
   */
  private SocketConnector requireConnector() {
    SocketConnector c = connector;
    if (c == null) throw new IllegalStateException("Not connected.");
    return c;
  }
}
