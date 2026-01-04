package socket.protocol;

public interface ProtocolListener {

    // wird gemacht wenn eine nachricht "geparst" wurde
    void onMessage(Message msg);

    //Verbindung wird beendet
    void onClosed(Exception e);
}
