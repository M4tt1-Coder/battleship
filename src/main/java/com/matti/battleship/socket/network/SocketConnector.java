package socket.network;

import java.io.*;
import java.net.Socket;
import socket.logging.TurnLog;

public class SocketConnector {

  private final Socket socket;
  private final BufferedReader reader;
  private final BufferedWriter writer;
  private final TurnLog log;

  private String currentTurn = "SERVER"; // Server beginnt immer
  private final String self;
  private final String other;

  private MessageListener listener;

  // ===== Duo-Tracking =====
  private enum PairState {
    NONE,
    SAW_SENT,
    SAW_RECEIVED
  }

  private PairState pairState = PairState.NONE;

  // 🔑 NEU: verzögerter Turnwechsel (für pass)
  private boolean pendingTurnSwitch = false;

  public SocketConnector(Socket socket, TurnLog log) throws IOException {
    this.socket = socket;
    this.log = log;

    this.self = (log.getSide() == TurnLog.Side.SERVER) ? "SERVER" : "CLIENT";
    this.other = self.equals("SERVER") ? "CLIENT" : "SERVER";

    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

    log.beginTurn(currentTurn);
  }

  public void setMessageListener(MessageListener listener) {
    this.listener = listener;
  }

  /* ===================== SEND ===================== */

  public synchronized void sendMessage(String msg) throws IOException {

    handleTurnOnSend(msg);

    log.sent(msg);
    onSentForPair();

    writer.write(msg);
    writer.newLine();
    writer.flush();
  }

  /* ===================== RECEIVE ===================== */

  public void startListening() {
    Thread t =
        new Thread(
            () -> {
              try {
                String line;
                while ((line = reader.readLine()) != null) {

                  handleTurnOnReceive(line);

                  log.received(line);
                  onReceivedForPair();

                  if (listener != null) {
                    listener.onMessageReceived(line);
                  }
                }
              } catch (Exception e) {
                if (listener != null) listener.onConnectionClosed(e);
              }
            });

    t.setDaemon(true);
    t.start();
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  /* ===================== PAIR LOGIC ===================== */

  private void onSentForPair() {
    if (pairState == PairState.SAW_RECEIVED) {
      finishPair();
    } else {
      pairState = PairState.SAW_SENT;
    }
  }

  private void onReceivedForPair() {
    if (pairState == PairState.SAW_SENT) {
      finishPair();
    } else {
      pairState = PairState.SAW_RECEIVED;
    }
  }

  private void finishPair() {
    pairState = PairState.NONE;

    log.separator();
    log.repeatTurnHeader();

    // 🔑 verzögerter Turnwechsel (z. B. nach pass)
    if (pendingTurnSwitch) {
      currentTurn = other;
      pendingTurnSwitch = false;
      log.repeatTurnHeader();
    }
  }

  /* ===================== TURN LOGIC ===================== */

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
        // pass gehört noch zum Gegner, Wechsel folgt nach Duo
        log.beginTurn(currentTurn);
        pendingTurnSwitch = true;
      }
      case "ok", "done" -> log.beginTurn(currentTurn);
    }
  }

  /* ===================== HELPERS ===================== */

  private String firstWord(String msg) {
    if (msg == null) return "";
    String s = msg.trim();
    int idx = s.indexOf(' ');
    return (idx < 0 ? s : s.substring(0, idx)).toLowerCase();
  }

  private int parseAnswer(String msg) {
    try {
      return Integer.parseInt(msg.trim().split("\\s+")[1]);
    } catch (Exception e) {
      return -1;
    }
  }
}
