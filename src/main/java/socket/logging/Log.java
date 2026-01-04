package socket.logging;

public class Log {

    public static void serverSent(String msg) {
        System.out.println("[SERVER ZUG]\n  [GESENDET ] " + msg);
    }

    public static void serverReceived(String msg) {
        System.out.println("[SERVER ZUG]\n  [EMPFANGEN] " + msg);
    }

    public static void clientSent(String msg) {
        System.out.println("[SERVER ZUG]\n  [GESENDET ] " + msg);
    }

    public static void clientReceived(String msg) {
        System.out.println("[SERVER ZUG]\n  [EMPFANGEN] " + msg);
    }
}
