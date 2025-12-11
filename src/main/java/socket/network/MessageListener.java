package socket.network;

public interface MessageListener {

    //Wird aufgerufen wenn SocketConnector ne volle Zeile bekommt
    //->Nachricht kommt als String
    void onMessageReceived(String message);

    //Aufruf falls Verbindung abbricht/geschlossen
    void onConnectionClosed(Exception e);
}