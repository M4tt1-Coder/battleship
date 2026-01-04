package socket.test;

import socket.discovery.ServerDiscoveryListener;
import socket.logging.Log;
import socket.network.ClientConnection;
import socket.protocol.MessageBuilder;

public class ClientTest {

    public static void main(String[] args) throws Exception {

        String ip = ServerDiscoveryListener.listen();

        ClientConnection client = new ClientConnection();
        client.connect(ip, msg -> {
            Log.clientReceived(msg);
            client.send(MessageBuilder.done());
            Log.clientSent("done");
        });
    }
}

