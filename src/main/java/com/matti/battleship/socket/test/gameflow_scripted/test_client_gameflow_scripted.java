package com.matti.battleship.socket.test.gameflow_scripted;

import com.matti.battleship.socket.network.ClientConnection;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.protocol.MessageBuilder;

import java.util.concurrent.CountDownLatch;


public class test_client_gameflow_scripted {

  private enum Phase {
    WAIT_SERVER_SHOT, // Client wartet auf server shots
    WAIT_SERVER_PASS, // Client wartet auf pass nach server Wasser
    CLIENT_SHOOTING, // Client schießt 2x
    WAIT_ANSWER, // Client wartet auf answer
    DONE
  }

  public static void main(String[] args) {
    CountDownLatch keepAlive = new CountDownLatch(1);

    try {
      ClientConnection client = new ClientConnection();

      final Phase[] phase = {Phase.WAIT_SERVER_SHOT};
      final int[] serverShotSeen = {0};
      final int[] clientShotSent = {0};

      System.out.println("[CLIENT] verbindet zu localhost...");
      client.connect(
          "localhost",
          new MessageListener() {

            @Override
            public void onMessageReceived(String msg) {

              try {
                String cmd = msg.split("\\s+")[0].toLowerCase();

                // ======= Server schießt: Client antwortet =======
                if ("shot".equals(cmd) && phase[0] == Phase.WAIT_SERVER_SHOT) {

                  serverShotSeen[0]++;

                  // Server Shot 1-3 -> Treffer
                  if (serverShotSeen[0] <= 3) {
                    client.send(MessageBuilder.answer(1));
                    return;
                  }

                  // Server Shot 4 -> Wasser
                  if (serverShotSeen[0] == 4) {
                    client.send(MessageBuilder.answer(0));
                    // jetzt MUSS Server pass schicken
                    phase[0] = Phase.WAIT_SERVER_PASS;
                    return;
                  }

                  return;
                }

                // ======= Server schickt pass -> jetzt ist Client dran =======
                if ("pass".equals(cmd) && phase[0] == Phase.WAIT_SERVER_PASS) {
                  phase[0] = Phase.CLIENT_SHOOTING;
                  sendNextClientShot(client, clientShotSent);
                  phase[0] = Phase.WAIT_ANSWER;
                  return;
                }

                // ======= Client wartet auf answer nach eigenem shot =======
                if ("answer".equals(cmd) && phase[0] == Phase.WAIT_ANSWER) {

                  int a = Integer.parseInt(msg.split("\\s+")[1]);

                  if (a == 1 || a == 2) {
                    // Treffer -> Client darf noch mal schießen
                    if (clientShotSent[0] < 2) {
                      phase[0] = Phase.CLIENT_SHOOTING;
                      sendNextClientShot(client, clientShotSent);
                      phase[0] = Phase.WAIT_ANSWER;
                    }
                    return;
                  }

                  if (a == 0) {
                    // Wasser -> Client muss pass senden -> Turn zurück zum Server
                    client.send(MessageBuilder.pass());
                    phase[0] = Phase.DONE;
                    System.out.println("[CLIENT] Gameflow-Test abgeschlossen ✅");
                    keepAlive.countDown();
                  }
                }

              } catch (Exception e) {
                e.printStackTrace();
                keepAlive.countDown();
              }
            }

            @Override
            public void onConnectionClosed(Exception e) {
              System.out.println("[CLIENT] Verbindung geschlossen: " + e);
              keepAlive.countDown();
            }
          });

      keepAlive.await();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sendNextClientShot(ClientConnection client, int[] clientShotSent)
      throws Exception {
    clientShotSent[0]++;

    // Client Shot 1..2
    int row = 2;
    int col = clientShotSent[0];

    client.send(MessageBuilder.shot(row, col));
  }
}
