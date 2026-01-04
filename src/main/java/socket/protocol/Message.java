package socket.protocol;

public record Message(MessageType type, String[] args) {}
