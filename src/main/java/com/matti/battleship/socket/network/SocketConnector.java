package com.matti.battleship.socket.network;

import com.matti.battleship.socket.logging.TurnLog;
import java.io.*;
import java.net.Socket;

/**
 * Handles low-level TCP socket communication between client and server. Responsibilities: - Send
 * messages over the socket (line-based protocol) - Receive messages in a background thread -
 * Forward received messages to a {@link MessageListener} - Maintain turn information
 * (SERVER/CLIENT) for logging and debugging - Print separators after complete send/receive pairs
 * (pair/duo tracking) * Note: * This class only transports messages and provides debug logging. *
 * The real protocol/game flow should be handled by higher layers (NetworkGameController +
 * NetworkStateMachine).
 *
 * @author WoFabian
 */
public class SocketConnector {

  /** Underlying TCP socket. */
  private final Socket socket;

  /** Reader for incoming line-based messages. */
  private final BufferedReader reader;

  /** Writer for outgoing line-based messages. */
  private final BufferedWriter writer;

  /** Turn and message logger. */
  private final TurnLog log;

  /** Current turn owner for logging purposes. Server always starts. */
  private String currentTurn = "SERVER";

  /** Name of this side ("SERVER" or "CLIENT") derived from {@link TurnLog.Side}. */
  private final String self;

  /** Name of the remote side ("SERVER" or "CLIENT"). */
  private final String other;

  /** Callback listener for message receive and connection close events. */
  private MessageListener listener;

  // ===== Duo-Tracking =====

  /**
   * Pair tracking state. Used to group a send and a receive into one "communication unit" so logs
   * stay readable.
   */
  private enum PairState {
    NONE,
    SAW_SENT,
    SAW_RECEIVED
  }

  /** Current pair state. */
  private PairState pairState = PairState.NONE;

  /**
   * Used for delayed turn switch after a "pass". The turn switch is applied only after the current
   * pair has been completed.
   */
  private boolean pendingTurnSwitch = false;

  /**
   * Creates a new SocketConnector for an already connected TCP socket.
   *
   * @param socket the connected socket (client or accepted server socket)
   * @param log logger instance to track turns and message flow
   * @throws IOException if socket streams cannot be created
   * @author WoFabian
   */
  public SocketConnector(Socket socket, TurnLog log) throws IOException {
    this.socket = socket;
    this.log = log;

    // Determine who "we" are based on the TurnLog side.
    this.self = (log.getSide() == TurnLog.Side.SERVER) ? "SERVER" : "CLIENT";
    this.other = self.equals("SERVER") ? "CLIENT" : "SERVER";

    // Create buffered stream wrappers for line-based communication.
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    // Server always starts -> initialize logging with server turn.
    log.beginTurn(currentTurn);
  }

  /**
   * Registers a listener that will be notified when messages are received or when the connection
   * closes due to an error.
   *
   * @param listener callback receiver
   * @author WoFabian
   */
  public void setMessageListener(MessageListener listener) {
    this.listener = listener;
  }

  /* ===================== SEND ===================== */

  /**
   * Sends a message over the socket. The message is written as a single line and flushed
   * immediately. Before sending, this method updates the internal turn state (for logging) and
   * updates pair tracking to keep logs structured.
   *
   * @param msg message to send (must be a single-line protocol command)
   * @throws IOException if writing to the socket fails
   * @author WoFabian
   */
  public synchronized void sendMessage(String msg) throws IOException {

    // Turn handling is based on the command keyword.
    handleTurnOnSend(msg);

    // Log outgoing message and update send/receive pairing.
    log.sent(msg);
    onSentForPair();

    // Send message as a line.
    writer.write(msg);
    writer.newLine();
    writer.flush();
  }

  /* ===================== RECEIVE ===================== */

  /**
   * Starts a background thread which continuously listens for incoming messages. For every received
   * line: - turn logic is updated (for readable logging) - the message is logged - pair tracking is
   * updated - the message is forwarded to the {@link MessageListener}
   *
   * @author WoFabian
   */
  public void startListening() {
    Thread t =
        new Thread(
            () -> {
              try {
                String line;
                while ((line = reader.readLine()) != null) {

                  // Update turn state based on incoming command.
                  handleTurnOnReceive(line);

                  // Log incoming message and update pairing.
                  log.received(line);
                  onReceivedForPair();

                  // Forward to game logic / higher layers.
                  if (listener != null) {
                    listener.onMessageReceived(line);
                  }
                }
              } catch (Exception e) {
                if (listener != null) listener.onConnectionClosed(e);
              }
            });
    // Daemon thread: does not block JVM shutdown.
    t.setDaemon(true);
    t.start();
  }

  /**
   * Closes the underlying socket connection.
   *
   * @author WoFabian
   */
  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
      // intentionally ignored; close best-effort
    }
  }

  /* ===================== PAIR LOGIC ===================== */

  /**
   * Updates pair tracking after a send event. If the last event was a receive, the pair is
   * complete.
   *
   * @author WoFabian
   */
  private void onSentForPair() {
    if (pairState == PairState.SAW_RECEIVED) {
      finishPair();
    } else {
      pairState = PairState.SAW_SENT;
    }
  }

  /**
   * Updates pair tracking after a receive event. If the last event was a send, the pair is
   * complete.
   *
   * @author WoFabian
   */
  private void onReceivedForPair() {
    if (pairState == PairState.SAW_SENT) {
      finishPair();
    } else {
      pairState = PairState.SAW_RECEIVED;
    }
  }

  /**
   * Finishes a full send/receive pair and prints separators / headers. Also applies delayed turn
   * switching (used for "pass") after the pair is complete.
   *
   * @author WoFabian
   */
  private void finishPair() {
    pairState = PairState.NONE;

    log.separator();
    log.repeatTurnHeader();

    // Apply delayed turn switch after "pass".
    if (pendingTurnSwitch) {
      currentTurn = other;
      pendingTurnSwitch = false;
      log.repeatTurnHeader();
    }
  }

  /**
   * IMPORTANT: Turn handling inside SocketConnector is only used for readable console output
   * (TurnLog). It does not replace the protocol/game state machine.
   *
   * @author WoFabian
   */
  /* ===================== TURN LOGIC ===================== */

  /**
   * Updates the current turn owner based on an outgoing command.
   *
   * @param msg outgoing message
   * @author WoFabian
   */
  private void handleTurnOnSend(String msg) {
    String cmd = firstWord(msg);

    switch (cmd) {
      case "size", "ships", "load", "ready", "done" -> {
        // Setup messages are considered server-driven.
        currentTurn = "SERVER";
        log.beginTurn(currentTurn);
      }
      case "shot" -> {
        // When we send a shot, it's our active turn.
        currentTurn = self;
        log.beginTurn(currentTurn);
      }
      case "pass" -> {
        // Pass switches turn AFTER the current pair is finished.
        log.beginTurn(currentTurn);
        pendingTurnSwitch = true;
      }
      case "save", "ok", "answer" -> log.beginTurn(currentTurn);
    }
  }

  /**
   * Updates the current turn owner based on an incoming command.
   *
   * @param msg incoming message
   * @author WoFabian
   */
  private void handleTurnOnReceive(String msg) {
    String cmd = firstWord(msg);

    switch (cmd) {
      case "size", "ships", "load", "ready" -> {
        // Setup messages are considered server-driven.
        currentTurn = "SERVER";
        log.beginTurn(currentTurn);
      }
      case "shot" -> {
        // If we receive a shot, the other side is shooting.
        currentTurn = other;
        log.beginTurn(currentTurn);
      }
      case "answer" -> {
        // Turn switching depends on the answer result.
        // Convention here: answer 0 => miss => other side gets turn.
        int a = parseAnswer(msg);
        if (a == 0) currentTurn = other;
        log.beginTurn(currentTurn);
      }
      case "pass" -> {
        // Pass switches after pair completion.
        log.beginTurn(currentTurn);
        pendingTurnSwitch = true;
      }
      case "ok", "done" -> log.beginTurn(currentTurn);
    }
  }

  /* ===================== HELPERS ===================== */

  /**
   * Extracts the first token (command keyword) from a message.
   *
   * @param msg raw message line
   * @return lowercase first word or empty string if msg is null/empty
   * @author WoFabian
   */
  private String firstWord(String msg) {
    if (msg == null) return "";
    String s = msg.trim();
    int idx = s.indexOf(' ');
    return (idx < 0 ? s : s.substring(0, idx)).toLowerCase();
  }

  /**
   * Parses the integer argument from an "answer" message. Expected format: "answer <number>"
   *
   * @param msg full message line
   * @return parsed answer value, or -1 if parsing fails
   * @author WoFabian
   */
  private int parseAnswer(String msg) {
    try {
      return Integer.parseInt(msg.trim().split("\\s+")[1]);
    } catch (Exception e) {
      return -1;
    }
  }
}
