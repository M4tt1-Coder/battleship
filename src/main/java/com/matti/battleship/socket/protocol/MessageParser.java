package com.matti.battleship.socket.protocol;

/**
 * Parses raw protocol lines (received via TCP) into {@link Message} objects.
 *
 * <p>The socket layer receives text lines like: - "size 10" - "ships 5 4 3 3 2" - "shot 3 7" -
 * "answer 0"
 *
 * <p>This parser converts the first token into a {@link MessageType} and stores the remaining
 * tokens as string arguments inside a {@link Message}.
 *
 * @author WoFabian
 */
public class MessageParser {

  /**
   * Parses one received text line from the socket into a {@link Message} object.
   *
   * <p>If the line is null/blank, an UNKNOWN message is returned. Unknown commands are also mapped
   * to UNKNOWN, but the arguments are still attached so debugging is easier.
   *
   * @param line complete raw line (e.g. "shot 3 4")
   * @return parsed {@link Message} containing type and arguments
   */
  public static Message parse(String line) {

    // Guard: empty input means we cannot parse anything meaningful.
    if (line == null || line.isBlank()) {
      return new Message(MessageType.UNKNOWN);
    }

    // Split by whitespace, so multiple spaces are handled correctly.
    String[] parts = line.trim().split("\\s+");

    // First token is the command keyword (lowercase for robustness).
    String command = parts[0].toLowerCase();

    // Remaining tokens are treated as arguments.
    String[] args = new String[parts.length - 1];
    System.arraycopy(parts, 1, args, 0, args.length);

    // Map the command keyword to a MessageType.
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

      // Any unexpected command is treated as UNKNOWN (args still preserved for
      // debugging).
      default -> new Message(MessageType.UNKNOWN, args);
    };
  }
}
