package socket.protocol;

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
    UNKNOWN     // bei random sachen
}
