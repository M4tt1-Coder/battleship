package com.matti.battleship.socket.test.discoverytest.standart;

import com.matti.battleship.socket.network.MessageListener;
import com.matti.battleship.socket.network.ServerConnection;

public class TestServerStandart {

    public static void main(String[] args) throws Exception {
        ServerConnection server = new ServerConnection();

        server.startServer(new MessageListener() {
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
                System.out.println("[SERVER] connection closed: " + (e != null ? e.getMessage() : "null"));
            }
        });

        System.out.println("[SERVER] läuft. Beenden mit STRG+C");
        Thread.currentThread().join();
    }
}
