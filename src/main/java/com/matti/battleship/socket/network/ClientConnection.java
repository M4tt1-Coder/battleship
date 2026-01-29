package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;

import java.net.Socket;

/**
 * GUI-IMPORTANT: Client-side TCP connection wrapper used by the GUI to connect to a selected
 * server.
 *
 * <p>This class is responsible for creating the TCP socket connection and starting the background
 * listener thread via {@link SocketConnector}. The GUI typically uses this class when the user
 * selects a server (from discovery list or manual host input) and presses a "Connect" button.
 *
 * <p>Typical GUI flow:
 * <ul>
 *   <li>User selects a server host (e.g. from discovery list) or uses a default host (e.g. localhost)</li>
 *   <li>GUI calls connect(selectedHost, listener)</li>
 *   <li>Then run listenLoop() in a background thread, and send initial protocol messages ("ready", ...)</li>
 * </ul>
 *
 * @author WoFabian
 */
public class ClientConnection {

  /** Handles the actual send/receive logic and turn logging. */
  private SocketConnector connector;

  /**
   * Establishes a TCP connection to the given host.
   *
   * <p>The port is taken from {@link EnvConfig#getPort()} so the GUI does not need to know the
   * port. After connecting, all incoming messages are delivered to the provided {@link
   * MessageListener}.
   *
   * @param host the target server host (e.g. "localhost" or a discovered IP address)
   * @param listener callback that receives raw protocol lines from the server
   * @throws Exception if the TCP connection cannot be established
   * @author WoFabian
   */
  public void connect(String host, MessageListener listener) throws Exception {
    // Create TCP connection to the server on the configured port.
    Socket socket = new Socket(host, EnvConfig.getPort());
    System.out.println("[CLIENT] verbunden mit Server: " + host);

    // Create logging instance for client side (useful for debugging protocol/gameflow).
    TurnLog log = new TurnLog(TurnLog.Side.CLIENT);

    // SocketConnector owns the reader/writer and runs the listening loop.
    connector = new SocketConnector(socket, log);

    // Forward received messages to the given listener.
    connector.setMessageListener(listener);
  }

  /** Enables listening flag (does not start a thread). */
  public void startListening() {
    if (connector != null) connector.startListening();
  }

  /** Stops listenLoop WITHOUT disconnecting. */
  public void stopListening() {
    if (connector != null) connector.stopListening();
  }

  /**
   * BLOCKING; start this in a thread from logic/GUI.
   *
   * <p>GUI-IMPORTANT: Do not run this on the UI thread (JavaFX/Swing).
   */
  public void listenLoop() {
    if (connector != null) connector.listenLoop();
  }

  /**
   * Sends a raw protocol message to the server.
   *
   * <p>GUI typically calls this for actions like:
   * <ul>
   *   <li>sending "shot r c" when user clicks a cell</li>
   *   <li>sending "pass" when needed by gameflow</li>
   *   <li>sending "ready" during setup handshake</li>
   * </ul>
   *
   * @param msg message line to send (one protocol command)
   * @throws Exception if sending fails or the connector is not connected
   * @author WoFabian
   */
  public void send(String msg) throws Exception {
    if (connector != null) connector.sendMessage(msg);
  }

  /**
   * Closes the underlying TCP connection.
   *
   * <p>GUI typically calls this when:
   * <ul>
   *   <li>leaving the online match screen</li>
   *   <li>returning to main menu</li>
   *   <li>the user presses a "Disconnect" button</li>
   * </ul>
   *
   * @author WoFabian
   */
  public void disconnect() {
    if (connector != null) connector.close();
  }
}
