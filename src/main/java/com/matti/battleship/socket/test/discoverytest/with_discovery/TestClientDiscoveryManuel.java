/**package com.matti.battleship.socket.test.discoverytest.with_discovery;

import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ClientDiscoveryScanner;
import com.matti.battleship.socket.discovery.DiscoveredServer;
import com.matti.battleship.socket.network.ClientConnection;
import com.matti.battleship.socket.network.MessageListener;
import java.util.List;
import java.util.Scanner;

 * GUI-ORIENTATION (Discovery Manual Variant)
 *
 * <p>What to copy into GUI: - Discovery scan: ClientDiscoveryScanner scanner = new
 * ClientDiscoveryScanner(EnvConfig.getPort()); List<DiscoveredServer> servers =
 * scanner.discover(600); - Building a server list UI: Use the returned List<DiscoveredServer> as
 * ListView/TableView items. - Connect after user selection: DiscoveredServer selected =
 * servers.get(index) (GUI: selectedItem from ListView) client.connect(selected.host(), listener); -
 * Fallback when list is empty: Manual option: connect to "localhost" using EnvConfig.getPort()
 *
 * <p>What NOT to copy (CLI-only): - Scanner(System.in), while-loops, command parsing like "0
 * verbinden", "scan", "exit" - The "chat typing" loop after connection (GUI will handle this
 * differently)
 *
 * <p>Purpose of this test: - Demonstrates the "manual selection" user flow which matches a GUI list
 * + connect button.
 *
 * @author WoFabian

public class TestClientDiscoveryManuel {

     How long to wait for discovery responses. GUI: choose a short value (e.g., 300-800ms).
  private static final int DISCOVER_TIMEOUT_MS = 600;

  public static void main(String[] args) throws Exception {
    int port = EnvConfig.getPort();

    // GUI: This replaces the "Search servers" button -> run scan and update the list UI.
    ClientDiscoveryScanner scanner = new ClientDiscoveryScanner(port);

    Scanner in = new Scanner(System.in);

    // GUI: Put this result into a ListView/TableView (servers become UI items).
    List<DiscoveredServer> servers = scanAndPrint(scanner);

    System.out.println();
    System.out.println("Befehle:");
    System.out.println("- \"<index> verbinden\"  z.B. \"0 verbinden\"");
    System.out.println("- \"manuell verbinden\"  (Fallback localhost:" + port + ")");
    System.out.println("- \"scan\"  (neu scannen)");
    System.out.println("- \"exit\"");

    while (true) {
      System.out.print("> ");
      String line = in.nextLine().trim();

      if (line.equalsIgnoreCase("exit")) return;

      if (line.equalsIgnoreCase("scan")) {
        // GUI: Refresh list -> run discover again and update UI list.
        servers = scanAndPrint(scanner);
        continue;
      }

      if (line.equalsIgnoreCase("manuell verbinden")) {
        // GUI: If list is empty -> offer a "Connect to localhost" default button.
        connectReadyAndChat("localhost", port);
        return;
      }

      // "<index> verbinden"
      String[] parts = line.split("\\s+");
      if (parts.length == 2 && parts[1].equalsIgnoreCase("verbinden")) {
        try {
          int idx = Integer.parseInt(parts[0]);
          if (idx < 0 || idx >= servers.size()) {
            System.out.println("Ungültiger Index. (0.." + (servers.size() - 1) + ")");
            continue;
          }
          // GUI: selected server comes from ListView selection instead of parsing an index.
          DiscoveredServer chosen = servers.get(idx);

          // GUI: Connect button -> client.connect(chosen.host(), listener)
          connectReadyAndChat(chosen.host(), chosen.port());
          return;

        } catch (NumberFormatException e) {
          System.out.println("Index muss eine Zahl sein, z.B. \"0 verbinden\".");
        }
        continue;
      }

      if (servers.isEmpty()) {
        System.out.println("Liste ist leer. Nutze \"manuell verbinden\" oder \"scan\".");
      } else {
        System.out.println("Unbekannter Befehl. Nutze \"<index> verbinden\", \"scan\", \"exit\".");
      }
    }
  }

   * Runs discovery and prints the list to console.
   *
   * <p>GUI: Replace the printing with updating the ListView/TableView.
   *
   * @param scanner discovery scanner
   * @return list of found servers
   * @throws Exception if discovery fails
   * @author WoFabian

  private static List<DiscoveredServer> scanAndPrint(ClientDiscoveryScanner scanner)
      throws Exception {
    System.out.println();
    System.out.println("[DISCOVERY] Scanne...");

    // GUI: Core logic for discovery -> this returns the list for the UI.
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

   * Connects via TCP and performs a small "ready" handshake to prove the connection works.
   *
   * <p>GUI: After connecting you would usually switch screens and pass the connection to your
   * GameFlow.
   *
   * @param host target host (selected server or fallback localhost)
   * @param port target port (usually EnvConfig.getPort())
   * @throws Exception if connection fails
   * @author WoFabian

  private static void connectReadyAndChat(String host, int port) throws Exception {
    System.out.println("[CLIENT] Verbinde zu " + host + ":" + port + " ...");

    // GUI: The connect action for the selected server.
    ClientConnection client = new ClientConnection();
    final boolean[] readyAck = {false};

    client.connect(
        host,
        new MessageListener() {
          @Override
          public void onMessageReceived(String message) {
            System.out.println("[CLIENT] recv: " + message);

            // GUI: This can be used to set "connected/ready" status in the UI.
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
            // GUI: Show disconnect message and navigate back to connect screen.
            System.out.println(
                "[CLIENT] connection closed: " + (e != null ? e.getMessage() : "null"));
          }
        });

    // GUI: Optional "proof of connection" handshake after connect.
    client.send("ready");
    System.out.println("[CLIENT] sent: ready");

    // CLI-only: chat loop for testing
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
