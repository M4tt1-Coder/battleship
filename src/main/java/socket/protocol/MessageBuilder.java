package socket.protocol;

//Baut Nachrichten als String auf -> Protokollformat
//Message -> text
public class MessageBuilder {

    // baut hello nachricht
    public static String size(int rows) {
        return "size " + rows;
    }

    // baut ships nachricht mit variabler anzahl an argumenten
    public static String ships(int... lengths) {
        StringBuilder sb = new StringBuilder("ships");
        for (int l : lengths) sb.append(" ").append(l);
        return sb.toString();
    }

    public static String done() {
        return "done";
    }

    public static String ready() {
        return "ready";
    }

    public static String shot(int row, int col) {
        return "shot " + row + " " + col;
    }

    public static String answer(int a) {
        return "answer " + a;
    }

    public static String pass() {
        return "pass";
    }

    public static String save(long id) {
        return "save " + id;
    }

    public static String load(long id) {
        return "load " + id;
    }

    public static String ok() {
        return "ok";
    }
}