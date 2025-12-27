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

  // Nachrichten senden = nur MessageBuilder verwenden!
  public void send(String message) throws Exception {
    connector.sendMessage(message);
  }

  @Override
  public void onMessageReceived(String message) {
    Message msg = MessageParser.parse(message);
    if (listener != null) listener.onMessage(msg);
  }

  @Override
  public void onConnectionClosed(Exception e) {
    if (listener != null) listener.onClosed(e);
  }
}
