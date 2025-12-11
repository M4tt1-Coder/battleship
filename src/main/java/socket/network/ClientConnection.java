package socket.network;

import socket.config.EnvConfig;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

    private SocketConnector connector;

    // Verbindung zu dem Server
    public void connectToServer(String host, MessageListener listener) throws IOException {
        int port = EnvConfig.getPort();
        Socket socket = new Socket(host, port);
        System.out.println("Mit Server verbunden");

        // Initialisiere den SocketConnector
        connector = new SocketConnector(socket);
        connector.setMessageListener(listener);
        connector.startListening();
    }

    // Sendet ne Nachricht an den Server
    public void send(String message) throws IOException {
        if (connector != null) connector.sendMessage(message);
    }

    // Verbindung trennen
    public void disconnect() {
        if (connector != null) connector.close();
    }
}
