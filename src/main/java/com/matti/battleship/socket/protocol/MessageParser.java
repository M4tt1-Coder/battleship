package socket.protocol;

public class MessageParser {

  /**
   * Parst eine empfangene Textzeile aus dem Socket und wandelt sie in ein Message-Objekt um.
   *
   * @param line komplette Zeile (z.B. "shot 3 4")
   * @return Message mit Typ + Argumenten
   */
  public static Message parse(String line) {

    // Sicherheitscheck (sollte eigentlich nie passieren)
    if (line == null || line.isBlank()) {
      return new Message(MessageType.UNKNOWN);
    }

    // Zerlege die Zeile an Leerzeichen
    // Beispiel: "shot 3 4" → ["shot", "3", "4"]
    String[] parts = line.trim().split("\\s+");

    // Erstes Wort ist immer das Befehlswort
    String command = parts[0].toLowerCase();

    // Rest sind die Parameter
    String[] args = new String[parts.length - 1];
    System.arraycopy(parts, 1, args, 0, args.length);

    // Befehlswort auf MessageType abbilden
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

      // Falls etwas Unerwartetes kommt
      default -> new Message(MessageType.UNKNOWN, args);
    };
  }
}
