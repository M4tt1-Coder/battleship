package socket.protocol;

// Wandelt ein String zu Message-Objekt
// -> Text -> Message
public class MessageParser {

    public static Message parse(String line) {

        // falls was ungültiges kommt
        if (line == null || line.isBlank())
            return new Message(MessageType.UNKNOWN);

        // Zerlegt Zeile in Woöter
        String[] parts = line.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        // Parameter extrahieren
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        //Befehlzuordnung
        return switch (command) {
            case "size" -> new Message(MessageType.SIZE, args);
            case "ships" -> new Message(MessageType.SHIPS, args);
            case "done" -> new Message(MessageType.DONE);
            case "ready" -> new Message(MessageType.READY);
            case "shot" -> new Message(MessageType.SHOT, args);
            case "answer" -> new Message(MessageType.ANSWER, args);
            case "pass" -> new Message(MessageType.PASS);
            case "save" -> new Message(MessageType.SAVE, args);
            case "load" -> new Message(MessageType.LOAD, args);
            case "ok" -> new Message(MessageType.OK);
            default -> new Message(MessageType.UNKNOWN, args);
        };
    }
}