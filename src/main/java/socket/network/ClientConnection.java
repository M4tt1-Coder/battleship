package socket.network;

import socket.config.EnvConfig;

import java.net.Socket;

public class ClientConnection {

    private SocketConnector connector;

    public void connect(String host, MessageListener listener) throws Exception {
        Socket socket = new Socket(host, EnvConfig.getPort());
        connector = new SocketConnector(socket);
        connector.setMessageListener(listener);
        connector.startListening();
    }

    public void send(String msg) throws Exception {
        connector.sendMessage(msg);
    }
}
