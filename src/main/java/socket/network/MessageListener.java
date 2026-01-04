package socket.network;

public interface MessageListener {

    void onMessageReceived(String message);

    void onConnectionClosed(Exception e);
}
