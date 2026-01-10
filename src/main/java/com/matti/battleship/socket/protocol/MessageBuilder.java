package socket.protocol;

public class MessageBuilder {

  public static String size(int rows) {
    return "size " + rows;
  }

  public static String ships(int... l) {
    StringBuilder sb = new StringBuilder("ships");
    for (int i : l) sb.append(" ").append(i);
    return sb.toString();
  }

  public static String done() {
    return "done";
  }

  public static String ready() {
    return "ready";
  }

  public static String shot(int r, int c) {
    return "shot " + r + " " + c;
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
