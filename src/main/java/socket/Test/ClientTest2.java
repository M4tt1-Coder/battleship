package socket.Test;

import socket.network.ClientConnection;
import socket.network.MessageListener;
import socket.protocol.MessageBuilder;

public class ClientTest2 {

    public static void main(String[] args) {

        try {
            ClientConnection client = new ClientConnection();

            System.out.println("[CLIENT] Verbindet...");

            client.connectToServer("localhost", new MessageListener() {

                @Override
                public void onMessageReceived(String msg) {
                    System.out.println("[CLIENT] Empfangen: " + msg);

                    try {
                        String command = msg.split(" ")[0];

                        switch (command) {

                            // Server sendet Spielfeldgröße
                            case "size" -> {
                                System.out.println("[CLIENT] sendet: done");
                                client.send(MessageBuilder.done());
                            }

                            // Server schießt
                            case "shot" -> {
                                System.out.println("[CLIENT] sendet: answer 2 (Treffer-versenkt)");
                                client.send(MessageBuilder.answer(2));
                            }

                            default -> System.out.println("[CLIENT] Unbekannte Nachricht");
                        }

                    } catch (Exception e) {
                        System.out.println("[CLIENT FEHLER] beim Senden: " + e);
                    }
                }

                @Override
                public void onConnectionClosed(Exception e) {
                    System.out.println("[CLIENT] Verbindung geschlossen: " + e);
                }
            });

            // verhindert, dass main() sofort beendet wird
            synchronized (ClientTest2.class) {
                ClientTest2.class.wait();
            }

        } catch (Exception e) {
            System.out.println("[CLIENT FEHLER] " + e);
        }
    }
}

