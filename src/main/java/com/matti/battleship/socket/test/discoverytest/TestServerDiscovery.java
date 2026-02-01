package com.matti.battleship.socket.test.discoverytest;

import com.matti.battleship.socket.GlobalConnector;
import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ServerDiscoveryResponder;
import com.matti.battleship.socket.network.IMessageListener;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manual CLI test server for UDP discovery + basic TCP connectivity.
 *
 * <p>This test starts a server using {@link GlobalConnector}, runs a {@link
 * ServerDiscoveryResponder} in a background thread and accepts a TCP client in another background
 * thread so the main thread can still process console input.
 *
 * <p>LOGIC-IMPORTANT: The discovery responder only answers DISCOVER requests while {@code busy ==
 * false}. As soon as a TCP client connects, {@code busy} becomes true and the server disappears
 * from the discovery results.
 *
 * <p>LOGIC-IMPORTANT: This is a minimal echo/ack test (ready -> ready, otherwise -> ok). It is not
 * the full Battleship setup handshake (size/ships/done).
 *
 * @author WoFabian
 */
public class TestServerDiscovery {

  /**
   * Starts the CLI discovery server and blocks for interactive user input.
   *
   * <p>GUI-OPTIONAL: Development utility for validating discovery visibility, accept lifecycle, and
   * listening start/stop behavior.
   *
   * @param args unused
   * @throws Exception if server setup fails
   * @author WoFabian
   */
  public static void main(String[] args) throws Exception {
    GlobalConnector global = new GlobalConnector();
    global.setServerName("Fubi Bubi" + "");

    // Listener must be installed BEFORE acceptClient so the internal forwarding/busy handling is
    // active immediately.
    global.setMessageListener(
        new IMessageListener() {
          @Override
          public void onMessageReceived(String message) {
            String m = (message == null) ? "" : message.trim();
            System.out.println("[SERVER] recv: " + m);

            try {
              // Minimal proof: respond to ready with ready; any other non-empty input gets ok.
              if ("ready".equalsIgnoreCase(m)) {
                global.sendMessage("ready");
                System.out.println("[SERVER] sent: ready");
              } else if (!m.isEmpty()) {
                global.sendMessage("ok");
                System.out.println("[SERVER] sent: ok");
              }
            } catch (Exception e) {
              System.out.println("[SERVER] send failed: " + e.getMessage());
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            System.out.println(
                "[SERVER] connection closed: " + (e != null ? e.getMessage() : "null"));
          }
        });

    // Start server socket (accept is the blocking part).
    global.startAsServer(global.getServerName());

    // Start discovery responder (runLoop is blocking -> thread is fine in a test).
    ServerDiscoveryResponder responder = global.createDiscoveryResponder();
    Thread discoveryThread = new Thread(responder::runLoop, "discovery-responder");
    discoveryThread.setDaemon(true);
    discoveryThread.start();

    System.out.println(
        "[DISCOVERY] Responder läuft (UDP " + EnvConfig.getPort() + ") – solange busy=false.");

    // Accept + listen in background so main thread can still read console input.
    AtomicReference<Thread> listenThreadRef = new AtomicReference<>();

    Thread acceptThread =
        new Thread(
            () -> {
              try {
                System.out.println("[SERVER] acceptClient... (blocking)");
                global.acceptClient();
                System.out.println("[SERVER] Client verbunden.");
                startListeningIfNeeded(global, listenThreadRef);
              } catch (Exception e) {
                System.out.println("[SERVER] accept failed: " + e.getMessage());
              }
            },
            "server-accept");
    acceptThread.setDaemon(true);
    acceptThread.start();

    // CLI chat loop.
    System.out.println();
    System.out.println("[SERVER] Befehle:");
    System.out.println("- status");
    System.out.println("- listen start");
    System.out.println("- listen stop");
    System.out.println("- exit");
    System.out.println("- any text -> send raw (wenn verbunden)");
    System.out.println();

    Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String raw = in.nextLine();
      if (raw == null) continue;

      final String cmd = raw.trim();

      if (cmd.equalsIgnoreCase("exit")) {
        System.out.println("[SERVER] exit.");
        responder.stop(); // daemon thread anyway, but cleaner shutdown
        global.close();
        return;
      }

      if (cmd.equalsIgnoreCase("status")) {
        System.out.println("[SERVER] busy=" + global.getBusyFlag().get());
        continue;
      }

      if (cmd.equalsIgnoreCase("listen start")) {
        startListeningIfNeeded(global, listenThreadRef);
        continue;
      }

      if (cmd.equalsIgnoreCase("listen stop")) {
        try {
          global.requestStopListening();
          System.out.println("[SERVER] listening STOPPED");
        } catch (Exception e) {
          System.out.println("[SERVER] cannot stop listening: " + e.getMessage());
        }
        continue;
      }

      if (cmd.isEmpty()) continue;

      try {
        global.sendMessage(cmd);
        System.out.println("[SERVER] sent: " + cmd);
      } catch (Exception e) {
        // Most common cause: no client connected yet => connector not available.
        System.out.println("[SERVER] send failed (not connected yet?): " + e.getMessage());
      }
    }
  }

  /**
   * Starts the listening thread if none is currently running.
   *
   * <p>LOGIC-IMPORTANT: We keep a reference to the thread so multiple listen loops are not started.
   * Before starting, we call {@link GlobalConnector#requestStartListening()} so the connector loop
   * is enabled (Task 2).
   *
   * @param global global connector instance used for listenLoop and start/stop flags
   * @param ref reference that stores the currently running listen thread
   * @author WoFabian
   */
  private static void startListeningIfNeeded(GlobalConnector global, AtomicReference<Thread> ref) {
    Thread t = ref.get();
    if (t != null && t.isAlive()) {
      System.out.println("[SERVER] listening already running");
      return;
    }

    try {
      global.requestStartListening();
    } catch (Exception e) {
      System.out.println("[SERVER] cannot start listening: " + e.getMessage());
      return;
    }

    Thread nt = new Thread(global::listenLoop, "server-listenLoop");
    nt.setDaemon(true);
    ref.set(nt);
    nt.start();

    System.out.println("[SERVER] listening STARTED");
  }
}
