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
    try (Scanner in = new Scanner(System.in)) {
      while (true) {
        System.out.print("> ");
        String line = in.nextLine();
        if (line == null) continue;

        String cmd = line.trim();

        if (cmd.equalsIgnoreCase("exit")) {
          log.info("[CLIENT] exit.");
          client.disconnect();
          return;
        }

        // --- Task 2 commands ---
        if (cmd.equalsIgnoreCase("listen stop")) {
          client.stopListening();
          log.info("[CLIENT] listening STOPPED");
          continue;
        }

        if (cmd.equalsIgnoreCase("listen start")) {
          startListeningIfNeeded(client, listenThreadRef, listenLock);
          continue;
        }

        if (cmd.equalsIgnoreCase("state")) {
          log.info("[CLIENT] state={}", controller.getStateMachine().getState());
          continue;
        }

        if (cmd.equalsIgnoreCase("done")) {
          try {
            controller.sendDone();
            log.info("[CLIENT] sent: done");
          } catch (Exception e) {
            log.error("[CLIENT] sendDone failed: {}", e.getMessage(), e);
          }
          continue;
        }

        if (cmd.equalsIgnoreCase("ok")) {
          try {
            controller.sendOk();
            log.info("[CLIENT] sent: ok");
          } catch (Exception e) {
            log.error("[CLIENT] sendOk failed: {}", e.getMessage(), e);
          }
          continue;
        }

        if (cmd.equalsIgnoreCase("start")) {
          try {
            controller.sendReady();
            log.info("[CLIENT] sent: ready");
          } catch (Exception e) {
            log.error("[CLIENT] sendReady failed: {}", e.getMessage(), e);
          }
          continue;
        }

        if (cmd.isBlank()) continue;

        // raw debug send
        try {
          client.send(cmd);
          log.info("[CLIENT] sent: {}", cmd);
        } catch (Exception e) {
          log.error("[CLIENT] send failed: {}", e.getMessage(), e);
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
