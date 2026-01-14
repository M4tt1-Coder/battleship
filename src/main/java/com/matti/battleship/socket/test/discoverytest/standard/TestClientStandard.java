package com.matti.battleship.socket.test.discoverytest.standard;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.network.MessageListener;
import java.util.Scanner;

public class TestClientStandard {

  public static void main(String[] args) throws Exception {
    int port = EnvConfig.getPort();
    StandardClientConnection client = new StandardClientConnection();

    final boolean[] readyAck = {false};

    client.connect(
        "localhost",
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            System.out.println("[CLIENT] recv: " + message);

            if ("ready".equalsIgnoreCase(message.trim())) {
              readyAck[0] = true;
              System.out.println(
                  "[CLIENT] ✅ Verbindung steht (ready/ready). Du kannst jetzt Nachrichten senden.");
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            System.out.println(
                "[CLIENT] connection closed: " + (e != null ? e.getMessage() : "null"));
          }
        });

    client.send("ready");
    System.out.println("[CLIENT] sent: ready (to localhost:" + port + ")");

    // Simple Input-Loop
    Scanner in = new Scanner(System.in);
    System.out.println("Tippe Nachrichten und drücke ENTER. 'exit' beendet.");

    while (true) {
      System.out.print("> ");
      String line = in.nextLine();
      if (line == null) continue;

      String trimmed = line.trim();
      if (trimmed.equalsIgnoreCase("exit")) {
        System.out.println("[CLIENT] exit.");
        return;
      }

      // (optional) wenn du willst: erst nach readyAck senden. Ich lasse es frei.
      client.send(trimmed);
      System.out.println("[CLIENT] sent: " + trimmed);
    }
  }
}
