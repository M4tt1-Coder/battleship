package socket.Test;

import socket.network.MessageListener;
import socket.network.ServerConnection;
import socket.protocol.MessageBuilder;

public class ServerTest2 {

    public static void main(String[] args) {

        try {
            // Server nutzt Port aus .env
            ServerConnection server = new ServerConnection();

            System.out.println("[SERVER] startet…");

            // Server wartet auf Client
            server.startServer(new MessageListener() {

                @Override
                public void onMessageReceived(String msg) {
                    System.out.println("[SERVER] Empfangen: " + msg);

                    try {
                        // Bestimmt den Nachrichtentyp anhand des ersten Wortes
                        String command = msg.split(" ")[0];

                        switch (command) {

                            // Client bestätigt Spielfeldgröße
                            case "done" -> {
                                System.out.println("[SERVER] sendet: shot 3 4");
                                server.send(MessageBuilder.shot(3, 4));
                            }

                            // Client hat auf einen Schuss geantwortet
                            case "answer" -> {
                                System.out.println("[SERVER] Antwort erhalten → Treffer versenkt.");
                                System.out.println("[SERVER] Test abgeschlossen.");
                            }

                            default -> System.out.println("[SERVER] Unbekannte Nachricht.");
                        }

                    } catch (Exception e) {
                        System.out.println("[SERVER FEHLER] beim Senden: " + e);
                    }
                }

                @Override
                public void onConnectionClosed(Exception e) {
                    System.out.println("[SERVER] Verbindung geschlossen: " + e);
                }
            });

            // Sobald der Client verbunden ist → Startnachricht senden
            System.out.println("[SERVER] sendet: size 10");
            server.send(MessageBuilder.size(10));

            // Verhindert sofortiges Beenden des Programms
            synchronized (ServerTest2.class) {
                ServerTest2.class.wait();
            }

        } catch (Exception e) {
            System.out.println("[SERVER FEHLER] " + e);
        }
    }
}
