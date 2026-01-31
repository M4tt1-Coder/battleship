package com.matti.battleship.socket.test.discoverytest;

import com.matti.battleship.socket.GlobalConnector;
import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ClientDiscoveryScanner;
import com.matti.battleship.socket.discovery.DiscoveredServer;
import com.matti.battleship.socket.network.IMessageListener;

import java.util.List;
import java.util.Scanner;

/**
 * Manual CLI test for UDP LAN discovery and a simple TCP "ready/ready" proof handshake.
 *
 * <p>This tool scans the local network via {@link ClientDiscoveryScanner} and prints all discovered
 * servers that are currently not busy. The user can then select an entry and connect using the
 * {@link GlobalConnector}.
 *
 * <p>LOGIC-IMPORTANT: Discovery is UDP-based and returns a host + TCP port. In this project the
 * discovery port is expected to match {@link EnvConfig#getPort()}, because UDP discovery and TCP
 * game connection share the same configured port.
 *
 * <p>GUI-OPTIONAL: This is a development utility. It allows testing discovery and basic connectivity
 * without running the full GUI.
 *
 * @author WoFabian
 */
public class TestClientDiscoveryManuell {

    /** Timeout used for the UDP discovery scan. */
    private static final int DISCOVER_TIMEOUT_MS = 600;

    /**
     * Starts the CLI discovery test and blocks for interactive user input.
     *
     * @param args unused
     * @throws Exception if discovery or connection setup fails
     * @author WoFabian
     */
    public static void main(String[] args) throws Exception {
        final int port = EnvConfig.getPort();

        ClientDiscoveryScanner scanner = new ClientDiscoveryScanner(port);
        Scanner in = new Scanner(System.in);

        List<DiscoveredServer> servers = scanAndPrint(scanner);

        System.out.println();
        System.out.println("Befehle:");
        System.out.println("- \"<index> verbinden\" z.B. \"0 verbinden\"");
        System.out.println("- \"manuell verbinden\" (Fallback localhost:" + port + ")");
        System.out.println("- \"scan\" (neu scannen)");
        System.out.println("- \"exit\"");

        while (true) {
            System.out.print("> ");
            String raw = in.nextLine();
            if (raw == null) continue;

            final String line = raw.trim();

            if (line.equalsIgnoreCase("exit")) return;

            if (line.equalsIgnoreCase("scan")) {
                servers = scanAndPrint(scanner);
                continue;
            }

            if (line.equalsIgnoreCase("manuell verbinden")) {
                connectReadyAndChat("localhost");
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

                    DiscoveredServer chosen = servers.get(idx);

                    // LOGIC-IMPORTANT: GlobalConnector uses EnvConfig.getPort() for TCP connect.
                    // If discovery returns a different port, we warn because the connection will
                    // still use EnvConfig's value (and may therefore fail).
                    if (chosen.port() != port) {
                        System.out.println(
                                "[WARN] Discovered TCP-Port (" + chosen.port()
                                        + ") != EnvConfig PORT (" + port + "). Verbindung nutzt EnvConfig PORT.");
                    }

                    connectReadyAndChat(chosen.host());
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

    /**
     * Runs a single UDP discovery scan and prints the resulting server list.
     *
     * <p>LOGIC-IMPORTANT: The scanner returns only "free" servers (servers that still answer
     * discovery because they are not busy).
     *
     * @param scanner discovery scanner instance (configured with the discovery port)
     * @return list of discovered servers (may be empty)
     * @throws Exception if UDP scanning fails
     * @author WoFabian
     */
    private static List<DiscoveredServer> scanAndPrint(ClientDiscoveryScanner scanner) throws Exception {
        System.out.println();
        System.out.println("[DISCOVERY] Scanne...");

        List<DiscoveredServer> servers = scanner.discover(DISCOVER_TIMEOUT_MS);

        if (servers.isEmpty()) {
            System.out.println("[DISCOVERY] Keine freien Server gefunden.");
        } else {
            System.out.println("[DISCOVERY] Gefundene freie Server:");
            for (int i = 0; i < servers.size(); i++) {
                DiscoveredServer s = servers.get(i);
                System.out.println(" [" + i + "] " + s.name() + " @ " + s.host() + ":" + s.port());
            }
        }
        return servers;
    }

    /**
     * Connects to a server, sends a {@code ready} message, and then starts a simple chat loop.
     *
     * <p>LOGIC-IMPORTANT: This is only a minimal connectivity test. It does not implement the full
     * setup handshake (size/ships/done). It merely checks whether TCP works and whether the peer
     * responds with {@code ready} so we can confirm "ready/ready".
     *
     * <p>GUI-IMPORTANT: The receive loop is started in a background thread because listenLoop()
     * is blocking.
     *
     * @param host target server host
     * @throws Exception if connecting fails
     * @author WoFabian
     */
    private static void connectReadyAndChat(String host) throws Exception {
        System.out.println("[CLIENT] Verbinde zu " + host + ":" + EnvConfig.getPort() + " ...");

        GlobalConnector global = new GlobalConnector();
        final boolean[] readyAck = {false};

        global.setMessageListener(new IMessageListener() {
            @Override
            public void onMessageReceived(String message) {
                String m = (message == null) ? "" : message.trim();
                System.out.println("[CLIENT] recv: " + m);

                // Once we see a ready response, we treat the connection as "established".
                if ("ready".equalsIgnoreCase(m) && !readyAck[0]) {
                    readyAck[0] = true;
                    System.out.println("[CLIENT] ✅ Verbindung steht (ready/ready). Du kannst jetzt Nachrichten senden.");
                }
            }

            @Override
            public void onConnectionClosed(Exception e) {
                System.out.println("[CLIENT] connection closed: " + (e != null ? e.getMessage() : "null"));
            }
        });

        global.connectToServer(host);

        // Start listening in background (listenLoop is blocking).
        global.requestStartListening();
        Thread listenThread = new Thread(global::listenLoop, "client-listenLoop");
        listenThread.setDaemon(true);
        listenThread.start();

        // Proof handshake: send ready once to trigger the peer's response (if implemented).
        try {
            global.sendMessage("ready");
            System.out.println("[CLIENT] sent: ready");
        } catch (Exception e) {
            System.out.println("[CLIENT] send failed: " + e.getMessage());
        }

        // Chat loop: send raw lines until exit.
        Scanner in = new Scanner(System.in);
        System.out.println("Tippe Nachrichten und drücke ENTER. 'exit' beendet.");

        while (true) {
            System.out.print("> ");
            String raw = in.nextLine();
            if (raw == null) continue;

            final String msg = raw.trim();
            if (msg.equalsIgnoreCase("exit")) {
                System.out.println("[CLIENT] exit.");
                global.close();
                return;
            }

            if (msg.isEmpty()) continue;

            try {
                global.sendMessage(msg);
                System.out.println("[CLIENT] sent: " + msg);
            } catch (Exception e) {
                System.out.println("[CLIENT] send failed: " + e.getMessage());
            }
        }
    }
}
