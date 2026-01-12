package com.matti.battleship.socket.test.pingpong;


import com.matti.battleship.socket.discovery.ServerDiscoveryBroadcaster;
import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.ServerConnection;
import com.matti.battleship.socket.protocol.MessageBuilder;

public class test_server_ping_pong {

  private enum Stage {
    WAIT_DONE_AFTER_SIZE,
    WAIT_DONE_AFTER_SHIPS
  }

  private static Stage stage = Stage.WAIT_DONE_AFTER_SIZE;

  public static void main(String[] args) {
    try {
      new Thread(new ServerDiscoveryBroadcaster()).start();

      ServerConnection server = new ServerConnection();

      server.startServer(
          new MessageListener() {

            @Override
            public void onMessageReceived(String msg) {
              try {
                if (!msg.startsWith("done")) return;

                if (stage == Stage.WAIT_DONE_AFTER_SIZE) {
                  server.send(MessageBuilder.ships(5, 4, 4, 3));
                  stage = Stage.WAIT_DONE_AFTER_SHIPS;
                } else {
                  // fertig
                  System.out.println("[SERVER] Test fertig.");
                }

              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            @Override
            public void onConnectionClosed(Exception e) {
              System.out.println("[SERVER] Verbindung geschlossen: " + e);
            }
          });

      server.send(MessageBuilder.size(10));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
