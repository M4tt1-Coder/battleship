package socket.protocol;

// Sollte in die GUI später implementiert werden
// bekommt fertige Message-Objekte
public interface ProtocolListener {

    // wird gemacht wenn eine nachricht "geparst" wurde
    void onMessage(Message msg);

    //Verbindung wird beendet
    void onClosed(Exception e);
}