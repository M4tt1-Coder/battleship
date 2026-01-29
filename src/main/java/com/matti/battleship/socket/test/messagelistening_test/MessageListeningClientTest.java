package com.matti.battleship.socket.test.messagelistening_test;

import com.matti.battleship.socket.network.ClientConnection;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.state.GameState;
import com.matti.battleship.socket.state.NetworkGameController;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CLIENT TEST (Task 2): - Commands printed ONCE at start - Listening can be started/stopped
 * manually without disconnecting socket - State printed only on "state"
 */
public class MessageListeningClientTest {

  private static final Logger log = LogManager.getLogger(MessageListeningClientTest.class);

  public static void main(String[] args) throws Exception {
    String host = "localhost";

    ClientConnection client = new ClientConnection();
    NetworkGameController controller = new NetworkGameController(false, client::send);

    // Keep track of the current listening thread (so we don't start multiple)
    AtomicReference<Thread> listenThreadRef = new AtomicReference<>();
    final Object listenLock = new Object();

    // Connect first
    client.connect(
        host,
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            controller.onMessageReceived(message);

            // only hints -> what to type next
            GameState st = controller.getStateMachine().getState();
            switch (st) {
              case C_NEED_DONE_AFTER_SIZE ->
                  log.info("[CLIENT] -> next: type 'done' to confirm SIZE");
              case C_NEED_DONE_AFTER_SHIPS ->
                  log.info("[CLIENT] -> next: type 'done' to confirm SHIPS");
              case C_NEED_OK_AFTER_LOAD -> log.info("[CLIENT] -> next: type 'ok' to confirm LOAD");
              case C_NEED_READY -> log.info("[CLIENT] -> next: type 'start' to send READY");
              default -> {}
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            controller.onConnectionClosed(e);
            log.warn("[CLIENT] connection closed: {}", (e != null ? e.getMessage() : "null"));
          }
        });

    // Print command list ONCE
    printCommands();

    // Start listening initially
    startListeningIfNeeded(client, listenThreadRef, listenLock);

    // CLI loop
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

  private static void startListeningIfNeeded(
      ClientConnection client, AtomicReference<Thread> listenThreadRef, Object lock) {

    synchronized (lock) {
      Thread t = listenThreadRef.get();
      if (t != null && t.isAlive()) {
        log.info("[CLIENT] listening already running (thread={})", t.getName());
        return;
      }

      // enable listening flag in connector
      client.startListening();

      Thread nt = new Thread(client::listenLoop, "client-listenLoop");
      nt.setDaemon(true);
      listenThreadRef.set(nt);
      nt.start();

      log.info("[CLIENT] listening STARTED (thread={})", nt.getName());
    }
  }

  private static void printCommands() {
    log.info("");
    log.info("[CLIENT] Commands:");
    log.info("- 'listen start' -> start MessageListening");
    log.info("- 'listen stop'  -> stop MessageListening");
    log.info("- 'done'  -> send DONE");
    log.info("- 'ok'    -> send OK");
    log.info("- 'start' -> send READY");
    log.info("- 'state' -> print current protocol state");
    log.info("- 'exit'  -> disconnect");
    log.info("- any text -> send raw message");
  }
}
