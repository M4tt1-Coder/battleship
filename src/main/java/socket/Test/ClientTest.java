package socket.Test;

import socket.network.ClientConnection;
import socket.network.MessageListener;
import socket.protocol.MessageBuilder;

public class ClientTest {

    public static void main(String[] args) {
        int port = 5555;

        try {
            ClientConnection client = new ClientConnection();

            System.out.println("[CLIENT] Verbindet...");
            client.connectToServer("localhost", port, new MessageListener() {

                @Override
                public void onMessageReceived(String msg) {
                    System.out.println("[CLIENT] Empfangene Nachricht: " + msg);
                }

                @Override
                public void onConnectionClosed(Exception e) {
                    System.out.println("[CLIENT] Verbindung geschlossen: " + e);
                }
            });

            // Testnachrichten senden
            System.out.println("[CLIENT] sendet: size 10");
            client.send("size 10");

            System.out.println("[CLIENT] sendet: ready");
            client.send(MessageBuilder.ready());

            // etwas warten
            Thread.sleep(1000);

            client.disconnect();

        } catch (Exception e) {
            System.out.println("[CLIENT FEHLER] " + e);
        }
    }
}
