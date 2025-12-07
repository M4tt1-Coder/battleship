package socket.protocol;

import java.util.Arrays;

public class Message {

    private final MessageType type;
    private final String [] args;

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

    public int getIntArg(int index) {
        return Integer.parseInt(args[index]);
    }

    public long getArglong(int index) {
        return Long.parseLong(args[index]);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    
}