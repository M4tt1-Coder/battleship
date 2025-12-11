package socket.protocol;

//gültige Protokoll-Kommandos
public enum MessageType {

    SIZE,       // size rows
    SHIPS,      // ships length ...
    DONE,       // done
    READY,      // ready
    SHOT,       // shot row col
    ANSWER,     // answer a
    PASS,       // pass
    SAVE,       // save id
    LOAD,       // load id
    OK,         // ok
    UNKNOWN     // bei random sachen, die nicht bestimmt sind
}
