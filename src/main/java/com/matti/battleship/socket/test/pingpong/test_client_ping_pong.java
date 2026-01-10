package socket.test.pingpong;

import java.util.concurrent.CountDownLatch;
import socket.discovery.ServerDisListener;
import socket.network.ClientConnection;
import socket.network.MessageListener;
import socket.protocol.MessageBuilder;

public class test_client_ping_pong {

  public static void main(String[] args) {
    CountDownLatch keepAlive = new CountDownLatch(1);

    try {
      String ip = ServerDisListener.listen(); // oder: "localhost"

      ClientConnection client = new ClientConnection();
      client.connect(
          ip,
          new MessageListener() {

            @Override
            public void onMessageReceived(String msg) {
              try {
                // Ping-Pong: sofort antworten
                if (msg.startsWith("size")) {
                  client.send(MessageBuilder.done());
                } else if (msg.startsWith("ships")) {
                  client.send(MessageBuilder.done());
                  // Test optional beenden:
                  // keepAlive.countDown();
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

      // verhindert, dass main() sofort endet
      keepAlive.await();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
