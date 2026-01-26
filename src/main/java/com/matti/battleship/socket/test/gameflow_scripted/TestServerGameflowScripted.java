/**package com.matti.battleship.socket.test.gameflow_scripted;

import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.ServerConnection;
import com.matti.battleship.socket.protocol.MessageBuilder;
import java.util.concurrent.CountDownLatch;

public class TestServerGameflowScripted {

  private enum Phase {
    SERVER_SHOOTING, // Server schießt 4x
    WAIT_CLIENT_SHOT, // Server wartet auf Client shots
    WAIT_CLIENT_PASS, // Server wartet auf Client pass nach Wasser
    DONE
  }

  public static void main(String[] args) {
    CountDownLatch keepAlive = new CountDownLatch(1);

    try {
      ServerConnection server = new ServerConnection();

      final Phase[] phase = {Phase.SERVER_SHOOTING};
      final int[] serverShotCount = {0};
      final int[] clientShotCount = {0};

      System.out.println("[SERVER] startet Gameflow-Test...");
      server.startServer(
          new MessageListener() {

            @Override
            public void onMessageReceived(String msg) {

              try {
                String cmd = msg.split("\\s+")[0].toLowerCase();

                // ======= SERVER bekommt answers auf seine shots =======
                if ("answer".equals(cmd) && phase[0] == Phase.SERVER_SHOOTING) {

                  int a = Integer.parseInt(msg.split("\\s+")[1]);

                  if (a == 1 || a == 2) {
                    // Treffer -> Server bleibt dran
                    if (serverShotCount[0] < 4) {
                      sendNextServerShot(server, serverShotCount);
                    }
                  } else if (a == 0) {
                    // Wasser -> Server muss pass senden -> Client ist dran
                    server.send(MessageBuilder.pass());
                    phase[0] = Phase.WAIT_CLIENT_SHOT;
                  }
                  return;
                }

                // ======= CLIENT schießt (Server muss answer senden) =======
                if ("shot".equals(cmd) && phase[0] == Phase.WAIT_CLIENT_SHOT) {

                  clientShotCount[0]++;

                  // Client Shot 1 -> Treffer (answer 1)
                  if (clientShotCount[0] == 1) {
                    server.send(MessageBuilder.answer(1));
                    // Client bleibt dran, wartet auf shot 2
                    return;
                  }

                  // Client Shot 2 -> Wasser (answer 0)
                  if (clientShotCount[0] == 2) {
                    server.send(MessageBuilder.answer(0));
                    // Jetzt muss der Client PASS schicken
                    phase[0] = Phase.WAIT_CLIENT_PASS;
                    return;
                  }

                  return;
                }

                // ======= Client schickt pass nach Wasser =======
                if ("pass".equals(cmd) && phase[0] == Phase.WAIT_CLIENT_PASS) {
                  phase[0] = Phase.DONE;
                  System.out.println("[SERVER] Gameflow-Test abgeschlossen ✅");
                  keepAlive.countDown();
                }

              } catch (Exception e) {
                e.printStackTrace();
                keepAlive.countDown();
              }
            }

            @Override
            public void onConnectionClosed(Exception e) {
              System.out.println("[SERVER] Verbindung geschlossen: " + e);
              keepAlive.countDown();
            }
          });

      // Sobald Client verbunden ist, startet der Server mit Shot 1
      // Kleiner Delay, damit beide Listening-Threads sauber laufen
      Thread.sleep(200);

      // Start: Server Shot 1
      sendNextServerShot(server, serverShotCount);

      keepAlive.await();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void sendNextServerShot(ServerConnection server, int[] serverShotCount)
      throws Exception {
    serverShotCount[0]++;

    // Shot 1..4 (Koordinaten sind egal, nur zur Unterscheidung)
    int row = 1;
    int col = serverShotCount[0];

    server.send(MessageBuilder.shot(row, col));
  }
}
*/
