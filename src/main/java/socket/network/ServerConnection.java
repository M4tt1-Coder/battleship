package socket.network;

import socket.config.EnvConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Verwaltet ne ServerSocket
public class ServerConnection {

    private final int port;  //port vom Server
    private ServerSocket serverSocket;
    private SocketConnector connector;

    public ServerConnection() {
        this.port = EnvConfig.getPort();
    }
    //Startet den Server und Wartet auf ne Verbindung
    // -> blockiert bis Cleint kommt
    public void startServer(MessageListener listener) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server wartet auf Verbindung ...");

        // Verbindung mit dem ersten Client
        Socket client = serverSocket.accept();
        System.out.println("Client verbunden: " + client.getInetAddress());

        connector = new SocketConnector(client);  // Initialisiere den SocketConnector
        connector.setMessageListener(listener);
        connector.startListening();
    }

    //Sendet ne Nachricht and den Client
    public void send(String message) throws IOException {
        if (connector != null) connector.sendMessage(message);
    }

    //Stoppt die Verbindung
    public void stop() {
        try {
            if (connector != null) connector.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {
        }
    }
}