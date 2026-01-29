package com.matti.battleship.socket.test.messagelistening_test;

import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.ServerConnection;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * SERVER TEST (Task 2): - Commands printed ONCE at start. - Handshake: server -> size 10 client ->
 * done server -> ships 5 4 3 3 2 client -> done server -> ready client -> ready (client types
 * 'start') - Listening can be started/stopped manually without disconnecting socket
 */
public class MessageListeningServerTest {

  private static final Logger log = LogManager.getLogger(MessageListeningServerTest.class);

  private enum Phase {
    WAIT_DONE_AFTER_SIZE,
    WAIT_DONE_AFTER_SHIPS,
    WAIT_READY_FROM_CLIENT,
    CHAT
  }

  public static void main(String[] args) throws Exception {
    ServerConnection server = new ServerConnection();
    server.setServerName("Battleship-Server");

    AtomicReference<Phase> phase = new AtomicReference<>(Phase.WAIT_DONE_AFTER_SIZE);

    // Track listen thread (avoid multiple)
    AtomicReference<Thread> listenThreadRef = new AtomicReference<>();
    final Object listenLock = new Object();

    // Print commands once
    printCommands();

    // Open + accept (blocking)
    log.info("[SERVER] openServerSocket...");
    server.openServerSocket();

    log.info("[SERVER] acceptClient... (blocking)");
    server.acceptClient(
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            String m = (message == null) ? "" : message.trim();
            log.info("[SERVER] recv: {}", m);

            try {
              // Small Start state machine
              if ("done".equalsIgnoreCase(m)) {
                if (phase.get() == Phase.WAIT_DONE_AFTER_SIZE) {
                  server.send("ships 5 4 3 3 2");
                  phase.set(Phase.WAIT_DONE_AFTER_SHIPS);
                  return;
                }
                if (phase.get() == Phase.WAIT_DONE_AFTER_SHIPS) {
                  server.send("ready");
                  phase.set(Phase.WAIT_READY_FROM_CLIENT);
                  return;
                }
              }

              if ("ready".equalsIgnoreCase(m) && phase.get() == Phase.WAIT_READY_FROM_CLIENT) {
                log.info("[SERVER] Start is done. CHAT mode.");
                phase.set(Phase.CHAT);
                return;
              }

            } catch (Exception e) {
              log.error("[SERVER] send failed: {}", e.getMessage(), e);
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            log.warn("[SERVER] connection closed: {}", (e != null ? e.getMessage() : "null"));
          }
        });

    // Start listening initially
    startListeningIfNeeded(server, listenThreadRef, listenLock);

    // Start handshake
    server.send("size 10");

    // CLI loop
    Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String line = in.nextLine();
      if (line == null) continue;

      String cmd = line.trim();

      if (cmd.equalsIgnoreCase("exit")) {
        log.info("[SERVER] exit.");
        server.close();
        return;
      }

      // --- Task 2 commands ---
      if (cmd.equalsIgnoreCase("listen stop")) {
        server.stopListening();
        log.info("[SERVER] listening STOPPED");
        continue;
      }

      if (cmd.equalsIgnoreCase("listen start")) {
        startListeningIfNeeded(server, listenThreadRef, listenLock);
        continue;
      }

      if (cmd.equalsIgnoreCase("phase")) {
        log.info("[SERVER] phase={}", phase.get());
        continue;
      }

      if (cmd.isBlank()) continue;

      // raw debug send
      try {
        server.send(cmd);
        log.info("[SERVER] sent: {}", cmd);
      } catch (Exception e) {
        log.error("[SERVER] send failed: {}", e.getMessage(), e);
      }
    }
  }

  private static void startListeningIfNeeded(
      ServerConnection server, AtomicReference<Thread> listenThreadRef, Object lock) {

    synchronized (lock) {
      Thread t = listenThreadRef.get();
      if (t != null && t.isAlive()) {
        log.info("[SERVER] listening already running (thread={})", t.getName());
        return;
      }

      server.startListening();

      Thread nt = new Thread(server::listenLoop, "server-listenLoop");
      nt.setDaemon(true);
      listenThreadRef.set(nt);
      nt.start();

      log.info("[SERVER] listening STARTED (thread={})", nt.getName());
    }
  }

  private static void printCommands() {
    log.info("");
    log.info("[SERVER] Commands:");
    log.info("- 'listen start' -> start MessageListening");
    log.info("- 'listen stop'  -> stop MessageListening");
    log.info("- 'phase' -> print current handshake phase");
    log.info("- 'exit'  -> stop server");
    log.info("- any text -> send raw message to client");
    log.info("");
  }
}
