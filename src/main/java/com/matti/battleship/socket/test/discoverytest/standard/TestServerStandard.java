/**package com.matti.battleship.socket.test.discoverytest.standard;

import com.matti.battleship.socket.network.MessageListener;
import java.util.Scanner;

public class TestServerStandard {

  public static void main(String[] args) throws Exception {
    StandardServerConnection server = new StandardServerConnection();

    server.startServer(
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            System.out.println("[SERVER-STANDARD] recv: " + message);

            String m = message == null ? "" : message.trim();

            try {
              // Proof: ready -> ready
              if ("ready".equalsIgnoreCase(m)) {
                server.send("ready");
                System.out.println("[SERVER-STANDARD] sent: ready");
              } else if (!m.isEmpty()) {
                server.send("ok");
                System.out.println("[SERVER-STANDARD] sent: ok");
              }
            } catch (Exception e) {
              System.out.println("[SERVER-STANDARD] send failed: " + e.getMessage());
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            System.out.println(
                "[SERVER-STANDARD] connection closed: " + (e != null ? e.getMessage() : "null"));
          }
        });

    // Server-Konsoleingaben senden
    System.out.println();
    System.out.println(
        "[SERVER-STANDARD] ✅ Verbunden. Tippe Nachrichten und drücke ENTER. 'exit' beendet.");

    Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String line = in.nextLine();
      if (line == null) continue;

      String msg = line.trim();
      if (msg.equalsIgnoreCase("exit")) {
        System.out.println("[SERVER-STANDARD] exit.");
        return;
      }

      if (msg.isEmpty()) continue;

      try {
        server.send(msg);
        System.out.println("[SERVER-STANDARD] sent: " + msg);
      } catch (Exception e) {
        System.out.println("[SERVER-STANDARD] send failed: " + e.getMessage());
      }
    }
  }
}
*/