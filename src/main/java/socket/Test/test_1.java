package socket.Test;

import socket.network.*;
import socket.protocol.*;

public class test_1 {

    public static void main(String[] args) throws Exception {

        int port = 5555;

        // ===========================
        // SERVER THREAD
        // ===========================
        Thread serverThread = new Thread(() -> {
            try {
                ServerConnection server = new ServerConnection(port);

                server.startServer(new MessageListener() {
                    @Override
                    public void onMessageReceived(String msg) {
                        System.out.println("[SERVER] empfing: " + msg);

                        // Test: Antwort senden
                        try {
                            server.send("ok");
                        } catch (Exception e) {
                            System.err.println("[SERVER] Fehler beim Senden:");
                            e.printStackTrace(System.err);
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
        });

        serverThread.start();

        // Server etwas Zeit geben zum Starten
        Thread.sleep(300);

        // ===========================
        // CLIENT THREAD
        // ===========================
        Thread clientThread = new Thread(() -> {
            try {
                ClientConnection client = new ClientConnection();

                client.connectToServer("localhost", port, new MessageListener() {
                    @Override
                    public void onMessageReceived(String msg) {
                        System.out.println("[CLIENT] empfing: " + msg);
                    }

                    @Override
                    public void onConnectionClosed(Exception e) {
                        System.out.println("[CLIENT] Verbindung geschlossen: " + e);
                    }
                });

                // Test-Nachricht an Server senden
                System.out.println("[CLIENT] sendet test-message");
                client.send("size 10");

                // Noch eine Nachricht
                client.send(MessageBuilder.ready());

                // etwas warten, dann schließen
                Thread.sleep(1000);
                client.disconnect();

            } catch (Exception e) {
                System.out.println("[CLIENT FEHLER] " + e);
            }
        });

        clientThread.start();

        // Alles laufen lassen
        Thread.sleep(5000);
        System.out.println("Test beendet.");
    }
}
