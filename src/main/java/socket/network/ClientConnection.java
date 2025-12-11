package socket.network;

import socket.config.EnvConfig;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

    private SocketConnector connector;

    public void connectToServer(String host, MessageListener listener) throws IOException {
        int port = EnvConfig.getPort();
        Socket socket = new Socket(host, port);
        System.out.println("Mit Server verbunden");

        connector = new SocketConnector(socket);
        connector.setMessageListener(listener);
        connector.startListening();
    }

    public void send(String message) throws IOException {
        if (connector != null) connector.sendMessage(message);
    }

    public void disconnect() {
        if (connector != null) connector.close();
    }
}
