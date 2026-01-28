/**
 * package com.matti.battleship.socket.test.discoverytest.with_discovery;
 *
 * <p>import com.matti.battleship.socket.network.MessageListener; import
 * com.matti.battleship.socket.network.ServerConnection; import java.util.Scanner;
 *
 * <p>GUI-ORIENTATION (Server Hosting + Discovery)
 *
 * <p>What to copy into GUI (only if the GUI has a "Host server" button): - Starting the server:
 * ServerConnection server = new ServerConnection(); server.startServer(listener);
 *
 * <p>Important note for GUI: - startServer(...) blocks internally on accept() until a client
 * connects, therefore it MUST run in a background thread in JavaFX.
 *
 * <p>What this test demonstrates: - The server is discoverable via UDP while waiting for TCP
 * accept(). - After connect, a simple "ready" handshake can be used to show the connection worked.
 *
 * <p>What NOT to copy (CLI-only): - Console typing loop, "exit" input handling
 *
 * @author WoFabian
 *     <p>public class TestServerDiscovery {
 *     <p>public static void main(String[] args) throws Exception { ServerConnection server = new
 *     ServerConnection();
 *     <p>// Startet TCP (und bei euch: Discovery parallel, falls in ServerConnection eingebaut)
 *     server.startServer( new MessageListener() { @Override public void onMessageReceived(String
 *     message) { System.out.println("[SERVER] recv: " + message);
 *     <p>String m = message == null ? "" : message.trim();
 *     <p>try { if ("ready".equalsIgnoreCase(m)) { server.send("ready");
 *     System.out.println("[SERVER] sent: ready"); } else if (!m.isEmpty()) { server.send("ok");
 *     System.out.println("[SERVER] sent: ok"); } } catch (Exception e) {
 *     System.out.println("[SERVER] send failed: " + e.getMessage()); } } @Override public void
 *     onConnectionClosed(Exception e) { System.out.println( "[SERVER] connection closed: " + (e !=
 *     null ? e.getMessage() : "null")); } });
 *     <p>// Ab hier kannst du tippen und an den Client senden System.out.println();
 *     System.out.println("[SERVER] ✅ Verbunden. Tippe Nachrichten und drücke ENTER. 'exit'
 *     beendet.");
 *     <p>Scanner in = new Scanner(System.in); while (true) { System.out.print("> "); String line =
 *     in.nextLine(); if (line == null) continue;
 *     <p>String msg = line.trim(); if (msg.equalsIgnoreCase("exit")) { System.out.println("[SERVER]
 *     exit."); return; }
 *     <p>if (msg.isEmpty()) continue;
 *     <p>try { server.send(msg); System.out.println("[SERVER] sent: " + msg); } catch (Exception e)
 *     { System.out.println("[SERVER] send failed: " + e.getMessage()); } } } }
 */
