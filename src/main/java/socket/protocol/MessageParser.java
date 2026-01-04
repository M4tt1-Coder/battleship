package socket.protocol;

public class MessageParser {

    public static Message parse(String line) {
        if (line == null || line.isBlank())
            return new Message(MessageType.UNKNOWN, new String[0]);

        String[] parts = line.split("\\s+");
        String cmd = parts[0].toLowerCase();
        String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);

        return switch (cmd) {
            case "size" -> new Message(MessageType.SIZE, args);
            case "ships" -> new Message(MessageType.SHIPS, args);
            case "done" -> new Message(MessageType.DONE, args);
            case "ready" -> new Message(MessageType.READY, args);
            case "shot" -> new Message(MessageType.SHOT, args);
            case "answer" -> new Message(MessageType.ANSWER, args);
            case "pass" -> new Message(MessageType.PASS, args);
            case "save" -> new Message(MessageType.SAVE, args);
            case "load" -> new Message(MessageType.LOAD, args);
            case "ok" -> new Message(MessageType.OK, args);
            default -> new Message(MessageType.UNKNOWN, args);
        };
    }
}
