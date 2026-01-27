package com.matti.battleship.socket.protocol;

import java.util.Arrays;

/**
 * Represents one parsed protocol message.
 *
 * <p>A message consists of a {@link MessageType} (command) and optional string arguments. This
 * class is typically created by {@link MessageParser} and then used by logic/state machine to react
 * to incoming network commands.
 *
 * @author WoFabian
 */
public class Message {

  /** Parsed command type (e.g. SIZE, SHOT, ANSWER). */
  private final MessageType type;

  /** Raw arguments of the message (already split by whitespace). */
  private final String[] args;

  /**
   * Creates a new message object.
   *
   * @param type message type (command)
   * @param args message arguments as strings
   * @author WoFabian
   */
  public Message(MessageType type, String... args) {
    this.type = type;
    this.args = args;
  }

  /**
   * Returns the message type (command).
   *
   * @return the {@link MessageType} of this message
   * @author WoFabian
   */
  public MessageType getType() {
    return type;
  }

  /**
   * Returns the raw string arguments of the message.
   *
   * @return arguments array (may be empty)
   * @author WoFabian
   */
  public String[] getArgs() {
    return args;
  }

  /**
   * Parses the argument at the given index as an int.
   *
   * <p>Used for commands like: - "size 10" -> getIntArg(0) == 10 - "shot 3 5" -> getIntArg(0) == 3,
   * getIntArg(1) == 5
   *
   * @param index index in args array
   * @return parsed integer value
   * @throws NumberFormatException if the argument is not a valid integer
   * @throws ArrayIndexOutOfBoundsException if the index is invalid
   * @author WoFabian
   */
  public int getIntArg(int index) {
    return Integer.parseInt(args[index]);
  }

  /**
   * Parses the argument at the given index as a long.
   *
   * <p>Used for commands like: - "save 123456" -> getArglong(0) == 123456 - "load 123456" ->
   * getArglong(0) == 123456
   *
   * @param index index in args array
   * @return parsed long value
   * @throws NumberFormatException if the argument is not a valid long
   * @throws ArrayIndexOutOfBoundsException if the index is invalid
   * @author WoFabian
   */
  public long getArglong(int index) {
    return Long.parseLong(args[index]);
  }

  /**
   * Debug string for logs and troubleshooting.
   *
   * @return string representation of this message
   * @author WoFabian
   */
  @Override
  public String toString() {
    return "Message{" + "type=" + type + ", args=" + Arrays.toString(args) + '}';
  }
}
