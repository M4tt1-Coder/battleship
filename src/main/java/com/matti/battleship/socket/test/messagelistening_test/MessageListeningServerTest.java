package com.matti.battleship.socket.test.messagelistening_test;

import com.matti.battleship.socket.GlobalConnector;
import com.matti.battleship.socket.network.IMessageListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CLI test server for validating the "start/stop listening without closing the socket" feature.
 *
 * <p>This test hosts a TCP server using {@link GlobalConnector} and performs a minimal handshake:
 * <p>1) send {@code size 10}
 * <p>2) wait for {@code done} -> send {@code ships ...}
 * <p>3) wait for {@code done} -> send {@code ready}
 * <p>4) wait for {@code ready} -> switch into "chat mode" (raw messages)
 *
 * <p>LOGIC-IMPORTANT: Listening can be stopped/started without closing the socket using
 * {@link GlobalConnector#requestStopListening()} / {@link GlobalConnector#requestStartListening()}.
 * This test makes it easy to verify that stop is NOT treated as a disconnect.
 *
 * <p>GUI-OPTIONAL: This is a development tool; it simulates the server side without a real GUI.
 *
 * @author WoFabian
 */
public class MessageListeningServerTest {

  private static final Logger log = LogManager.getLogger(MessageListeningServerTest.class);

  /**
   * Minimal handshake phases used only for this CLI test.
   *
   * <p>LOGIC-IMPORTANT: This is not the project's full state machine. It is intentionally tiny so
   * we can quickly validate send/receive order during Task 2 testing.
   */
  private enum Phase {
    WAIT_DONE_AFTER_SIZE,
    WAIT_DONE_AFTER_SHIPS,
    WAIT_READY_FROM_CLIENT,
    CHAT
  }

  /**
   * Starts the CLI server and blocks for interactive user input.
   *
   * <p>GUI-OPTIONAL: Console tool used for testing network lifecycle and listening control.
   *
   * @param args unused
   * @throws Exception if server setup fails
   * @author WoFabian
   */
  public static void main(String[] args) throws Exception {

    GlobalConnector global = new GlobalConnector();
    global.setServerName("Battleship-Server");

    AtomicReference<Phase> phase = new AtomicReference<>(Phase.WAIT_DONE_AFTER_SIZE);
    AtomicReference<Thread> listenThreadRef = new AtomicReference<>();

    // Optional discovery responder usage:
    // global.createDiscoveryResponder();  // then run responder.runLoop() externally

    printCommands();

    // Start server socket (does not accept yet).
    global.startAsServer(global.getServerName());

    // Install listener BEFORE accept so the connector events are routed correctly once connected.
    global.setMessageListener(
            new IMessageListener() {
              @Override
              public void onMessageReceived(String msg) {
                String m = (msg == null) ? "" : msg.trim();
                log.info("[SERVER] recv: {}", m);

                try {
                  // Minimal handshake driven by test phases.
                  if ("done".equalsIgnoreCase(m)) {
                    if (phase.get() == Phase.WAIT_DONE_AFTER_SIZE) {
                      global.sendMessage("ships 5 4 3 3 2");
                      phase.set(Phase.WAIT_DONE_AFTER_SHIPS);
                      return;
                    }
                    if (phase.get() == Phase.WAIT_DONE_AFTER_SHIPS) {
                      global.sendMessage("ready");
                      phase.set(Phase.WAIT_READY_FROM_CLIENT);
                      return;
                    }
                  }

                  if ("ready".equalsIgnoreCase(m) && phase.get() == Phase.WAIT_READY_FROM_CLIENT) {
                    log.info("[SERVER] ✅ CHAT MODE");
                    phase.set(Phase.CHAT);
                  }
                } catch (Exception e) {
                  log.error("[SERVER] send failed", e);
                }
              }

              @Override
              public void onConnectionClosed(Exception e) {
                log.warn("[SERVER] connection closed: {}", (e != null ? e.getMessage() : "null"));
              }
            });

    // Accept client (blocking).
    log.info("[SERVER] acceptClient... (blocking)");
    global.acceptClient();

    // Start listening initially.
    startListeningIfNeeded(global, listenThreadRef);

    // Kick off handshake.
    global.sendMessage("size 10");

    Scanner in = new Scanner(System.in);
    while (true) {
      String cmd = in.nextLine();
      if (cmd == null) continue;
      cmd = cmd.trim();

      switch (cmd) {
        case "listen start" -> startListeningIfNeeded(global, listenThreadRef);

        case "listen stop" -> {
          global.requestStopListening();
          log.info("[SERVER] listening STOPPED");
        }

        case "phase" -> log.info("[SERVER] phase={}", phase.get());

        case "exit" -> {
          log.info("[SERVER] exit");
          global.close();
          return;
        }

        default -> {
          if (cmd.isBlank()) continue;

          // In CHAT mode we simply send raw lines; for testing this is still useful in any phase.
          global.sendMessage(cmd);
          log.info("[SERVER] sent: {}", cmd);
        }
      }
    }
  }

  /**
   * Starts the listening thread if none is currently running.
   *
   * <p>LOGIC-IMPORTANT: We store the thread reference to avoid multiple listen loops on the same
   * socket. Before starting, we re-enable listening using
   * {@link GlobalConnector#requestStartListening()} (Task 2).
   *
   * @param global global connector instance used for listenLoop and start/stop flags
   * @param ref reference that stores the currently running listen thread
   * @author WoFabian
   */
  private static void startListeningIfNeeded(GlobalConnector global, AtomicReference<Thread> ref) {
    Thread t = ref.get();
    if (t != null && t.isAlive()) {
      log.info("[SERVER] listening already running");
      return;
    }

    global.requestStartListening();

    Thread nt = new Thread(global::listenLoop, "server-listenLoop");
    nt.setDaemon(true);
    ref.set(nt);
    nt.start();

    log.info("[SERVER] listening STARTED");
  }

  /**
   * Prints the available CLI commands to the console.
   *
   * @author WoFabian
   */
  private static void printCommands() {
    log.info("");
    log.info("[SERVER] Commands:");
    log.info("- listen start");
    log.info("- listen stop");
    log.info("- phase");
    log.info("- exit");
    log.info("- any text -> send raw");
    log.info("");
  }
}
