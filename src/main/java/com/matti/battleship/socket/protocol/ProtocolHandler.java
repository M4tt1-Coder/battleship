package socket.protocol;

import socket.network.MessageListener;
import socket.network.SocketConnector;

public class ProtocolHandler implements MessageListener {

  private final SocketConnector connector;
  private ProtocolListener listener;

  public ProtocolHandler(SocketConnector connector) {
    this.connector = connector;
    connector.setMessageListener(this);
  }

  public void setProtocolListener(ProtocolListener listener) {
    this.listener = listener;
  }

  public void send(String msg) throws Exception {
    connector.sendMessage(msg);
  }

  @Override
  public void onMessageReceived(String message) {
    if (listener != null) {
      listener.onMessage(MessageParser.parse(message));
    }
  }

  @Override
  public void onConnectionClosed(Exception e) {
    if (listener != null) {
      listener.onClosed(e);
    }
  }
}
