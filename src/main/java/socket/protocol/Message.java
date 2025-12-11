package socket.protocol;

import java.util.Arrays;

// Message-Objekt ist ne empfangene Nachricht
//zerlegt ne nachricht auf type=... und args = [...,...]
public class Message {

    private final MessageType type;
    private final String[] args;

    public Message(MessageType type, String... args) {
        this.type = type;
        this.args = args;
    }

    public MessageType getType() {
        return type;
    }

    public String[] getArgs() {
        return args;
    }
    // Konventiert es zu int
    public int getArgInt(int index) {
        return Integer.parseInt(args[index]);
    }
    // Konventiert es zu long
    public long getArgLong(int index) {
        return Long.parseLong(args[index]);
    }

    @Override
    // Gibt ne lesbare Darstellung der Nachricht zurück
    public String toString() {
        return "Message{" + 
                "type=" + type + 
                ", args=" + Arrays.toString(args) 
                +'}';
    }
}
