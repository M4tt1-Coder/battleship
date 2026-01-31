package com.matti.battleship.socket.test.messagelistening_test;

import com.matti.battleship.socket.GlobalConnector;
import com.matti.battleship.socket.network.IMessageListener;
import com.matti.battleship.socket.state.NetworkGameController;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CLI test client for validating the "start/stop listening without closing the socket" feature.
 *
 * <p>This test connects to a locally running server and starts a background thread that runs {@link
 * GlobalConnector#listenLoop()}.
 *
 * <p>LOGIC-IMPORTANT: The listening loop can be stopped and started again via {@link
 * GlobalConnector#requestStopListening()} / {@link GlobalConnector#requestStartListening()}. This
 * is used to verify Task 2 behavior (stop receiving without closing the TCP connection).
 *
 * <p>LOGIC-IMPORTANT: Incoming raw lines are forwarded to {@link NetworkGameController} so the
 * {@link com.matti.battleship.socket.state.NetworkStateMachine} can be tested interactively without
 * automatic acknowledgements.
 *
 * @author WoFabian
 */
public class MessageListeningClientTest {

  private static final Logger log = LogManager.getLogger(MessageListeningClientTest.class);

  /**
   * Starts the CLI test client and blocks for interactive user input.
   *
   * <p>GUI-OPTIONAL: This is a pure console tool intended for development/testing only.
   *
   * @param args unused
   * @throws Exception if connection setup fails
   * @author WoFabian
   */
  public static void main(String[] args) throws Exception {

    GlobalConnector global = new GlobalConnector();

    // Controller (Client): no auto-acks; we send done/ok/ready manually from CLI commands.
    NetworkGameController controller = new NetworkGameController(false, global::sendMessage);

    // Listener: incoming -> controller + logging
    global.setMessageListener(
        new IMessageListener() {
          @Override
          public void onMessageReceived(String message) {
            controller.onMessageReceived(message);
          }

          @Override
          public void onConnectionClosed(Exception e) {
            log.warn("[CLIENT] connection closed: {}", (e != null ? e.getMessage() : "null"));
          }
        });

    // Connect to local server instance.
    global.connectToServer("localhost");

    printCommands();
    AtomicReference<Thread> listenThreadRef = new AtomicReference<>();

    // Start listening initially.
    startListeningIfNeeded(global, listenThreadRef);

    Scanner in = new Scanner(System.in);
    while (true) {
      String raw = in.nextLine();
      if (raw == null) continue;

      final String cmd = raw.trim(); // effectively final -> safe for lambdas

      switch (cmd) {
        case "listen start" -> startListeningIfNeeded(global, listenThreadRef);

        case "listen stop" -> {
          global.requestStopListening();
          log.info("[CLIENT] listening STOPPED");
        }

        case "state" -> log.info("[CLIENT] state={}", controller.getStateMachine().getState());

        case "done" -> safe(controller::sendDone, "[CLIENT] sent: done");
        case "ok" -> safe(controller::sendOk, "[CLIENT] sent: ok");
        case "ready", "start" -> safe(controller::sendReady, "[CLIENT] sent: ready");

        case "exit" -> {
          log.info("[CLIENT] exit");
          global.close();
          return;
        }

        default -> {
          if (cmd.isBlank()) continue;

          final String toSend = cmd; // explicit: the raw line we send over TCP
          safe(() -> global.sendMessage(toSend), "[CLIENT] sent raw: " + toSend);
        }
      }
    }
  }

  /**
   * Starts the listening thread if none is currently running.
   *
   * <p>LOGIC-IMPORTANT: We keep a reference to the thread so we don't accidentally start multiple
   * concurrent listen loops on the same socket.
   *
   * <p>LOGIC-IMPORTANT: Before starting the thread, we call {@link
   * GlobalConnector#requestStartListening()} to ensure the connector loop is enabled (Task 2).
   *
   * @param global global connector instance used for listenLoop and start/stop flags
   * @param ref reference that stores the currently running listen thread
   * @author WoFabian
   */
  private static void startListeningIfNeeded(GlobalConnector global, AtomicReference<Thread> ref) {
    Thread t = ref.get();
    if (t != null && t.isAlive()) {
      log.info("[CLIENT] listening already running");
      return;
    }

    global.requestStartListening();

    Thread nt = new Thread(global::listenLoop, "client-listenLoop");
    nt.setDaemon(true);
    ref.set(nt);
    nt.start();

    log.info("[CLIENT] listening STARTED");
  }

  /**
   * Executes a CLI action and logs either success or failure.
   *
   * <p>LOGIC-IMPORTANT: This keeps the interactive loop robust: a failed send should not crash the
   * entire CLI test.
   *
   * @param r action that may throw
   * @param okMsg log message if the action succeeds
   * @author WoFabian
   */
  private static void safe(ThrowingRunnable r, String okMsg) {
    try {
      r.run();
      log.info(okMsg);
    } catch (Exception e) {
      log.error("[CLIENT] action failed: {}", e.getMessage());
    }
  }

  /**
   * Simple functional interface used by {@link #safe(ThrowingRunnable, String)}.
   *
   * @author WoFabian
   */
  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  /**
   * Prints the available CLI commands to the console.
   *
   * @author WoFabian
   */
  private static void printCommands() {
    log.info("");
    log.info("[CLIENT] Commands:");
    log.info("- listen start");
    log.info("- listen stop");
    log.info("- done | ok | ready");
    log.info("- state");
    log.info("- exit");
    log.info("- any text -> send raw");
    log.info("");
  }
}
