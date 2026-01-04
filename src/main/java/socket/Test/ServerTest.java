package socket.test;

import socket.discovery.ServerDiscoveryBroadcaster;
import socket.logging.Log;
import socket.network.ServerConnection;
import socket.protocol.MessageBuilder;

public class ServerTest {

    public static void main(String[] args) throws Exception {

        new Thread(new ServerDiscoveryBroadcaster()).start();

        ServerConnection server = new ServerConnection();
        server.startServer(msg -> {
            Log.serverReceived(msg);
            server.send(MessageBuilder.size(10));
            Log.serverSent("size 10");
        });
    }
}
