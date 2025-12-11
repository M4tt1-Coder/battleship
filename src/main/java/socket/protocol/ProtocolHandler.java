package socket.protocol;

import socket.network.SocketConnector;
import socket.network.MessageListener;

// Verbindet String-Ebene mit Message-Ebene
public class ProtocolHandler implements MessageListener {

    private final SocketConnector connector;
    private ProtocolListener listener; // Listener für fertige Message-Obj

    public ProtocolHandler(SocketConnector connector) {
        this.connector = connector;
        connector.setMessageListener(this);
    }

    public void setProtocolListener(ProtocolListener listener) {
        this.listener = listener;
    }

    // Nachrichten senden als String (mit MessageBuilder)
    public void send(String message) throws Exception {
        connector.sendMessage(message);
    }

    // aufgerufen, wenn Nachricht empfangen
    @Override
    public void onMessageReceived(String message) {
        Message msg = MessageParser.parse(message);  //String -> Message
        if (listener != null) listener.onMessage(msg);
    }

    // aufgerufen, wenn Verbindung weg
    @Override
    public void onConnectionClosed(Exception e) {
        if (listener != null) listener.onClosed(e);
    }
}