package com.matti.battleship.socket.network;

import com.matti.battleship.socket.logging.TurnLog;
import java.io.*;
import java.net.Socket;

/**
 * Low-level TCP line IO for the Battleship protocol (no threads inside).
 *
 * <p>This class is responsible for sending and receiving single protocol lines over an existing
 * {@link Socket}. It deliberately does not create threads; the caller decides where/when {@link
 * #listenLoop()} runs.
 *
 * <p>LOGIC-IMPORTANT: Listening can be started/stopped without closing the socket (Task 2). This is
 * implemented by using a socket timeout so {@code readLine()} wakes up regularly and the loop can
 * check stop flags.
 *
 * <p>LOGIC-IMPORTANT: Turn logging is handled here (via {@link TurnLog}). The connector derives a
 * "turn" from protocol commands and writes a readable send/receive pairing to the log.
 *
 * @author WoFabian
 */
public class SocketConnector {

  /** Underlying TCP socket used for sending and receiving protocol lines. */
  private final Socket socket;

  /** Line-based reader for incoming protocol messages. */
  private final BufferedReader reader;

  /** Line-based writer for outgoing protocol messages. */
  private final BufferedWriter writer;

  /** Turn log helper used to create readable turn-based send/receive output. */
  private final TurnLog log;

  /** Current logical turn label used by the log (protocol-derived, not a game-rule engine). */
  private String currentTurn = "SERVER";

  /** Label for this side (SERVER or CLIENT) used only for logging/turn formatting. */
  private final String self;

  /** Label for the other side (SERVER or CLIENT) used only for logging/turn formatting. */
  private final String other;

  /** Optional callback for forwarding received lines to higher layers. */
  private IMessageListener listener;

  /**
   * Pairing state for log formatting.
   *
   * <p>LOGIC-IMPORTANT: We treat communication as send/receive "pairs" to insert separators and
   * repeat headers only after a pair is complete. This keeps the {@link TurnLog} readable even when
   * messages arrive very quickly.
   */
  private enum PairState {
    NONE,
    SAW_SENT,
    SAW_RECEIVED
  }

  /** Current pairing state used to decide when a send/receive pair is complete. */
  private PairState pairState = PairState.NONE;

  /**
   * Turn switch marker used by PASS.
   *
   * <p>LOGIC-IMPORTANT: {@code pass} switches the turn after the current send/receive pair is
   * completed (logging readability). We delay the header switch until {@link #finishPair()}.
   */
  private boolean pendingTurnSwitch = false;

  /**
   * Lock used to guard the listening control flags.
   *
   * <p>LOGIC-IMPORTANT: The listen loop and GUI/controller calls may run on different threads. We
   * guard reads/writes to {@code listeningEnabled} and {@code stopLoopRequested} to keep Task 2
   * behavior deterministic.
   */
  private final Object listenLock = new Object();

  /**
   * Enables/disables message processing inside {@link #listenLoop()} without closing the socket.
   *
   * <p>LOGIC-IMPORTANT: When disabled, the loop stays alive but idles briefly and re-checks flags.
   */
  private boolean listeningEnabled = true;

  /**
   * Requests {@link #listenLoop()} to exit without closing the socket.
   *
   * <p>LOGIC-IMPORTANT: The loop exits "soon" because {@code readLine()} wakes up via {@code
   * SO_TIMEOUT}.
   */
  private boolean stopLoopRequested = false;

  /**
   * Creates a connector for an already connected socket.
   *
   * <p>LOGIC-IMPORTANT: {@code SO_TIMEOUT} is set so {@code readLine()} does not block forever.
   * This is required for Task 2 (stop listening without closing the socket).
   *
   * @param socket connected TCP socket
   * @param log turn log instance for readable send/receive output
   * @throws IOException if socket IO streams cannot be created
   * @author WoFabian
   */
  public SocketConnector(Socket socket, TurnLog log) throws IOException {
    this.socket = socket;
    this.log = log;

    this.self = (log.getSide() == TurnLog.Side.SERVER) ? "SERVER" : "CLIENT";
    this.other = self.equals("SERVER") ? "CLIENT" : "SERVER";

    // Allows read operations to wake up periodically so stopLoopRequested can be checked.
    this.socket.setSoTimeout(750);

    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    log.beginTurn(currentTurn);
  }

  /**
   * Sets the callback that receives incoming protocol lines and disconnect events.
   *
   * @param listener listener to notify (may be null)
   */
  public void setMessageListener(IMessageListener listener) {
    this.listener = listener;
  }

  /**
   * Sends a single protocol line to the peer.
   *
   * <p>LOGIC-IMPORTANT: This updates the {@link TurnLog} and pairing state before writing to the
   * socket so the log ordering always matches the intended protocol flow.
   *
   * @param msg one full protocol command line (without newline)
   * @throws IOException if writing to the socket fails
   */
  public synchronized void sendMessage(String msg) throws IOException {
    handleTurnOnSend(msg);

    log.sent(msg);
    onSentForPair();

    writer.write(msg);
    writer.newLine();
    writer.flush();
  }

  /**
   * Enables processing in {@link #listenLoop()} and clears a previous stop request (Task 2).
   *
   * <p>LOGIC-IMPORTANT: This does not start a thread; it only enables the loop if/when it is
   * running.
   *
   * @author WoFabian
   */
  public void requestStartListening() {
    synchronized (listenLock) {
      listeningEnabled = true;
      stopLoopRequested = false;
    }
  }

  /**
   * Disables processing and requests {@link #listenLoop()} to exit without closing the socket (Task
   * 2).
   *
   * <p>LOGIC-IMPORTANT: This is intentionally not a disconnect. The TCP socket remains open.
   *
   * @author WoFabian
   */
  public void requestStopListening() {
    synchronized (listenLock) {
      listeningEnabled = false;
      stopLoopRequested = true; // exit listenLoop soon
    }
  }

  /**
   * Blocking receive loop that forwards incoming protocol lines to the listener.
   *
   * <p>LOGIC-IMPORTANT: This method is intentionally blocking and does not start threads. The
   * caller must run it in a suitable background context.
   *
   * <p>LOGIC-IMPORTANT: A stop request is NOT treated as a disconnect. Only a real remote close
   * ({@code readLine()} returns null) triggers {@link
   * IMessageListener#onConnectionClosed(Exception)}.
   *
   * @author WoFabian
   */
  public void listenLoop() {
    boolean remoteClosed = false;

    try {
      while (true) {

        boolean enabled;
        boolean stop;
        synchronized (listenLock) {
          enabled = listeningEnabled;
          stop = stopLoopRequested;
        }

        // Stop requested => NOT a connection close.
        if (stop) return;

        if (!enabled) {
          // Pause briefly to avoid a hot busy-loop while listening is disabled.
          try {
            Thread.sleep(50);
          } catch (InterruptedException ignored) {
          }
          continue;
        }

        String line;
        try {
          line = reader.readLine();
        } catch (InterruptedIOException timeout) {
          // Expected due to SO_TIMEOUT: used to periodically re-check stop flags.
          continue;
        }

        if (line == null) {
          remoteClosed = true;
          break;
        }

        handleTurnOnReceive(line);

        log.received(line);
        onReceivedForPair();

        if (listener != null) listener.onMessageReceived(line);
      }

      // Only if remote really closed.
      if (remoteClosed && listener != null) listener.onConnectionClosed(null);

    } catch (Exception e) {
      if (listener != null) listener.onConnectionClosed(e);
    }
  }

  /**
   * Closes the underlying socket.
   *
   * <p>LOGIC-IMPORTANT: This is a hard shutdown and will also end any active {@link #listenLoop()}
   * due to IO errors / stream closure.
   */
  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  /** Updates pair state after sending and finishes a pair once both directions were observed. */
  private void onSentForPair() {
    if (pairState == PairState.SAW_RECEIVED) finishPair();
    else pairState = PairState.SAW_SENT;
  }

  /** Updates pair state after receiving and finishes a pair once both directions were observed. */
  private void onReceivedForPair() {
    if (pairState == PairState.SAW_SENT) finishPair();
    else pairState = PairState.SAW_RECEIVED;
  }

  /**
   * Completes the current send/receive pair and formats the {@link TurnLog} boundary.
   *
   * <p>LOGIC-IMPORTANT: A delayed turn switch (pendingTurnSwitch) is applied here so the log header
   * only changes after the pair is visually separated.
   */
  private void finishPair() {
    pairState = PairState.NONE;

    log.separator();
    log.repeatTurnHeader();

    if (pendingTurnSwitch) {
      currentTurn = other;
      pendingTurnSwitch = false;
      log.repeatTurnHeader();
    }
  }

  /**
   * Applies turn/log rules for an outgoing message.
   *
   * <p>LOGIC-IMPORTANT: This does not enforce game rules. It only derives a readable turn header
   * for the {@link TurnLog} based on the protocol command flow.
   */
  private void handleTurnOnSend(String msg) {
    String cmd = firstWord(msg);

    switch (cmd) {
      case "size", "ships", "load", "ready", "done" -> {
        currentTurn = "SERVER";
        log.beginTurn(currentTurn);
      }
      case "shot" -> {
        currentTurn = self;
        log.beginTurn(currentTurn);
      }
      case "pass" -> {
        log.beginTurn(currentTurn);
        pendingTurnSwitch = true;
      }
      case "save", "ok", "answer" -> log.beginTurn(currentTurn);
    }
  }

  /**
   * Applies turn/log rules for an incoming message.
   *
   * <p>LOGIC-IMPORTANT: Some commands (e.g. {@code answer}) may change the derived "turn" based on
   * their parameters. This is only used for logging readability.
   */
  private void handleTurnOnReceive(String msg) {
    String cmd = firstWord(msg);

    switch (cmd) {
      case "size", "ships", "load", "ready" -> {
        currentTurn = "SERVER";
        log.beginTurn(currentTurn);
      }
      case "shot" -> {
        currentTurn = other;
        log.beginTurn(currentTurn);
      }
      case "answer" -> {
        int a = parseAnswer(msg);
        if (a == 0) currentTurn = other;
        log.beginTurn(currentTurn);
      }
      case "pass" -> {
        log.beginTurn(currentTurn);
        pendingTurnSwitch = true;
      }
      case "ok", "done" -> log.beginTurn(currentTurn);
    }
  }

  /**
   * Extracts the first protocol token (command) from a line.
   *
   * <p>LOGIC-IMPORTANT: Commands are treated case-insensitively to be robust against input
   * variations during testing/CLI usage.
   */
  private String firstWord(String msg) {
    if (msg == null) return "";
    String s = msg.trim();
    int idx = s.indexOf(' ');
    return (idx < 0 ? s : s.substring(0, idx)).toLowerCase();
  }

  /**
   * Parses the numeric argument of an {@code answer} command.
   *
   * <p>LOGIC-IMPORTANT: Returns {@code -1} if the message is malformed so logging logic can fall
   * back to a safe default.
   */
  private int parseAnswer(String msg) {
    try {
      return Integer.parseInt(msg.trim().split("\\s+")[1]);
    } catch (Exception e) {
      return -1;
    }
  }
}
