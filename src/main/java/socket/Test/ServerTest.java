package socket.Test;

import socket.network.MessageListener;
import socket.network.ServerConnection;

public class ServerTest {

    public static void main(String[] args) {

        try {
            ServerConnection server = new ServerConnection();

            System.out.println("[SERVER] startet…");
            server.startServer(new MessageListener() {

                @Override
                public void onMessageReceived(String msg) {
                    System.out.println("[SERVER] Empfangene Nachricht: " + msg);

                    // Testantwort senden
                    try {
                        server.send("ok");
                    } catch (Exception e) {
                        System.out.println("[SERVER] Fehler beim Senden: " + e);
                    }
                }

                @Override
                public void onConnectionClosed(Exception e) {
                    System.out.println("[SERVER] Verbindung geschlossen: " + e);
                }
            });

        } catch (Exception e) {
            System.out.println("[SERVER FEHLER] " + e);
        }
    }
}