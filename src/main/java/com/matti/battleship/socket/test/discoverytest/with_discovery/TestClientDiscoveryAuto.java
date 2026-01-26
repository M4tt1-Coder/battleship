/**package com.matti.battleship.socket.test.discoverytest.with_discovery;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ClientDiscoveryScanner;
import com.matti.battleship.socket.discovery.DiscoveredServer;
import com.matti.battleship.socket.network.ClientConnection;
import com.matti.battleship.socket.network.MessageListener;
import java.util.List;
import java.util.Scanner;

 * GUI-ORIENTATION (Discovery Auto-Fallback Variant)
 *
 * <p>What to copy into GUI: - Discovery scan (same as manual variant): List<DiscoveredServer>
 * servers = new ClientDiscoveryScanner(EnvConfig.getPort()).discover(600); - If list is NOT empty:
 * Show list and connect using selected.host()/selected.port() - If list IS empty: Auto fallback
 * logic: client.connect("localhost", listener); (Port via EnvConfig inside ClientConnection)
 * client.send("ready"); (optional "proof of connection")
 *
 * <p>What NOT to copy (CLI-only): - Console command parsing, while-loops, "exit" handling
 *
 * <p>Purpose of this test: - Shows how the GUI could behave if you want "auto-connect to localhost"
 * when no server is found.
 *
 * @author WoFabian

public class TestClientDiscoveryAuto {

  private static final int DISCOVER_TIMEOUT_MS = 600;

  public static void main(String[] args) throws Exception {
    int port = EnvConfig.getPort();
    ClientDiscoveryScanner scanner = new ClientDiscoveryScanner(port);

    // 1) scan
    List<DiscoveredServer> servers = scanAndPrint(scanner);

    // 2) AUTO: wenn leer -> sofort localhost connect
    if (servers.isEmpty()) {
      System.out.println("[AUTO] Keine Server gefunden -> Auto-Fallback: localhost:" + port);
      connectReadyAndChat("localhost", port);
      return;
    }

    // 3) wenn nicht leer -> Auswahl wie manuell
    Scanner in = new Scanner(System.in);
    System.out.println();
    System.out.println("Befehle:");
    System.out.println("- \"<index> verbinden\"  z.B. \"0 verbinden\"");
    System.out.println("- \"scan\"  (neu scannen)");
    System.out.println("- \"exit\"");

    while (true) {
      System.out.print("> ");
      String line = in.nextLine().trim();

      if (line.equalsIgnoreCase("exit")) return;

      if (line.equalsIgnoreCase("scan")) {
        servers = scanAndPrint(scanner);

        if (servers.isEmpty()) {
          System.out.println("[AUTO] Nach Scan leer -> Auto-Fallback: localhost:" + port);
          connectReadyAndChat("localhost", port);
          return;
        }
        continue;
      }

      String[] parts = line.split("\\s+");
      if (parts.length == 2 && parts[1].equalsIgnoreCase("verbinden")) {
        try {
          int idx = Integer.parseInt(parts[0]);
          if (idx < 0 || idx >= servers.size()) {
            System.out.println("Ungültiger Index. (0.." + (servers.size() - 1) + ")");
            continue;
          }
          DiscoveredServer chosen = servers.get(idx);
          connectReadyAndChat(chosen.host(), chosen.port());
          return;

        } catch (NumberFormatException e) {
          System.out.println("Index muss eine Zahl sein, z.B. \"0 verbinden\".");
        }
        continue;
      }

      System.out.println("Unbekannter Befehl. Nutze \"<index> verbinden\", \"scan\", \"exit\".");
    }
  }

  private static List<DiscoveredServer> scanAndPrint(ClientDiscoveryScanner scanner)
      throws Exception {
    System.out.println();
    System.out.println("[DISCOVERY] Scanne...");
    List<DiscoveredServer> servers = scanner.discover(DISCOVER_TIMEOUT_MS);

    if (servers.isEmpty()) {
      System.out.println("[DISCOVERY] Keine freien Server gefunden.");
    } else {
      System.out.println("[DISCOVERY] Gefundene freie Server:");
      for (int i = 0; i < servers.size(); i++) {
        DiscoveredServer s = servers.get(i);
        System.out.println("  [" + i + "] " + s.name() + " @ " + s.host() + ":" + s.port());
      }
    }
    return servers;
  }

  private static void connectReadyAndChat(String host, int port) throws Exception {
    System.out.println("[CLIENT] Verbinde zu " + host + ":" + port + " ...");

    ClientConnection client = new ClientConnection();
    final boolean[] readyAck = {false};

    client.connect(
        host,
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            System.out.println("[CLIENT] recv: " + message);

            if ("ready".equalsIgnoreCase(message.trim())) {
              if (!readyAck[0]) {
                readyAck[0] = true;
                System.out.println(
                    "[CLIENT] ✅ Verbindung steht (ready/ready). Du kannst jetzt Nachrichten senden.");
              }
            }
          }

          @Override
          public void onConnectionClosed(Exception e) {
            System.out.println(
                "[CLIENT] connection closed: " + (e != null ? e.getMessage() : "null"));
          }
        });

    client.send("ready");
    System.out.println("[CLIENT] sent: ready");

    // Chat loop
    Scanner in = new Scanner(System.in);
    System.out.println("Tippe Nachrichten und drücke ENTER. 'exit' beendet.");

    while (true) {
      System.out.print("> ");
      String msg = in.nextLine();
      if (msg == null) continue;

      String trimmed = msg.trim();
      if (trimmed.equalsIgnoreCase("exit")) {
        System.out.println("[CLIENT] exit.");
        return;
      }

      client.send(trimmed);
      System.out.println("[CLIENT] sent: " + trimmed);
    }
  }
}
*/
