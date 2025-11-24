package socket.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {

    private final int port;
    private ServerSocket serverSocket;
    private SocketConnector connector;

    public ServerConnection(int port) {
        this.port = port;
    }

    public void startServer(MessageListener listener) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server wartet auf Verbindung ...");

        Socket client = serverSocket.accept();
        System.out.println("Client verbunden: " + client.getInetAddress());

        connector = new SocketConnector(client);
        connector.setMessageListener(listener);
        connector.startListening();
    }

    public void send(String message) throws IOException {
        if (connector != null) connector.sendMessage(message);
    }

    public void stop() {
        try {
            if (connector != null) connector.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) { // ✔️ MUSS Exception sein, nicht IOException
        }
    }
}