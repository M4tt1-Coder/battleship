package com.matti.battleship.socket.network;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.logging.TurnLog;
import java.net.Socket;

/**
 * GUI-IMPORTANT: Client-side TCP connection wrapper used by the GUI to connect to a selected server.
 *
 * This class is responsible for creating the TCP socket connection and starting the background
 * listener thread via {@link SocketConnector}. The GUI typically uses this class when the user
 * selects a server (from discovery list or manual host input) and presses a "Connect" button.
 *
 * Typical GUI flow:
 * - User selects a server host (e.g. from discovery list) or uses a default host (e.g. localhost)
 * - GUI calls connect(selectedHost, listener)
 * - After the connection is established, the GUI/gameflow sends initial protocol messages
 *   (for example "ready" or waits for server setup messages depending on your protocol flow)
 *
 * Important methods for GUI integration:
 * - connect(host, listener): Establishes the TCP connection and starts listening for messages
 * - send(msg): Sends a protocol message to the server
 * - disconnect(): Closes the connection (useful when leaving the online match screen)
 *
 * @author WoFabian
 */
public class ClientConnection {

  /** Handles the actual send/receive logic and turn logging. */
  private SocketConnector connector;

  /**
   * Establishes a TCP connection to the given host and starts listening for incoming messages.
   *
   * The port is taken from {@link EnvConfig#getPort()} so the GUI does not need to know the port.
   * After connecting, all incoming messages are delivered to the provided {@link MessageListener}.
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

    // SocketConnector owns the reader/writer and runs the listening thread.
    connector = new SocketConnector(socket, log);

    // Forward received messages to the given listener and start reading loop.
    connector.setMessageListener(listener);
    connector.startListening();
  }

  /**
   * Sends a raw protocol message to the server.
   *
   * GUI typically calls this for actions like:
   * - sending "shot r c" when user clicks a cell
   * - sending "pass" when needed by gameflow
   * - sending "ready" during setup handshake
   *
   * @param msg message line to send (one protocol command)
   * @throws Exception if sending fails or the connector is not connected
   * @author WoFabian
   */
  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  /**
   * Closes the underlying TCP connection.
   *
   * GUI typically calls this when:
   * - leaving the online match screen
   * - returning to main menu
   * - the user presses a "Disconnect" button
   *
   * @author WoFabian
   */
  public void disconnect() {
    if (connector != null) connector.close();
  }
}
