package socket.network;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

  private SocketConnector connector;

  public void connectToServer(String host, MessageListener listener) throws IOException {
    Socket socket = new Socket(host, 50000);
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
