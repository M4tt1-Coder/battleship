package socket.network;

import socket.config.EnvConfig;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnection {

    private ServerSocket serverSocket;
    private SocketConnector connector;

    public void startServer(MessageListener listener) throws Exception {
        serverSocket = new ServerSocket(EnvConfig.getPort());
        Socket client = serverSocket.accept();

        connector = new SocketConnector(client);
        connector.setMessageListener(listener);
        connector.startListening();
    }

    public void send(String msg) throws Exception {
        connector.sendMessage(msg);
    }
}
