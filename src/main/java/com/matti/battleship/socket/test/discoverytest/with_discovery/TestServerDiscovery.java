package com.matti.battleship.socket.test.discoverytest.with_discovery;

import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.ServerConnection;
import java.util.Scanner;

public class TestServerDiscovery {

  public static void main(String[] args) throws Exception {
    ServerConnection server = new ServerConnection();

    // Startet TCP (und bei euch: Discovery parallel, falls in ServerConnection eingebaut)
    server.startServer(
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            System.out.println("[SERVER] recv: " + message);

            String m = message == null ? "" : message.trim();

            try {
              if ("ready".equalsIgnoreCase(m)) {
                server.send("ready");
                System.out.println("[SERVER] sent: ready");
              } else if (!m.isEmpty()) {
                server.send("ok");
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

    // Ab hier kannst du tippen und an den Client senden
    System.out.println();
    System.out.println("[SERVER] ✅ Verbunden. Tippe Nachrichten und drücke ENTER. 'exit' beendet.");

    Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String line = in.nextLine();
      if (line == null) continue;

      String msg = line.trim();
      if (msg.equalsIgnoreCase("exit")) {
        System.out.println("[SERVER] exit.");
        return;
      }

      if (msg.isEmpty()) continue;

      try {
        server.send(msg);
        System.out.println("[SERVER] sent: " + msg);
      } catch (Exception e) {
        System.out.println("[SERVER] send failed: " + e.getMessage());
      }
    }
  }
}
