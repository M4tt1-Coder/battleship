/**
 * package com.matti.battleship.socket.test.gameflow_scripted;
 *
 * <p>import com.matti.battleship.socket.network.ClientConnection; import
 * com.matti.battleship.socket.network.MessageListener; import
 * com.matti.battleship.socket.protocol.MessageBuilder; import java.util.concurrent.CountDownLatch;
 *
 * <p>public class TestClientGameflowScripted {
 *
 * <p>private enum Phase { WAIT_SERVER_SHOT, // Client wartet auf server shots WAIT_SERVER_PASS, //
 * Client wartet auf pass nach server Wasser CLIENT_SHOOTING, // Client schießt 2x WAIT_ANSWER, //
 * Client wartet auf answer DONE }
 *
 * <p>public static void main(String[] args) { CountDownLatch keepAlive = new CountDownLatch(1);
 *
 * <p>try { ClientConnection client = new ClientConnection();
 *
 * <p>final Phase[] phase = {Phase.WAIT_SERVER_SHOT}; final int[] serverShotSeen = {0}; final int[]
 * clientShotSent = {0};
 *
 * <p>System.out.println("[CLIENT] verbindet zu localhost..."); client.connect( "localhost", new
 * MessageListener() { @Override public void onMessageReceived(String msg) {
 *
 * <p>try { String cmd = msg.split("\\s+")[0].toLowerCase();
 *
 * <p>// ======= Server schießt: Client antwortet ======= if ("shot".equals(cmd) && phase[0] ==
 * Phase.WAIT_SERVER_SHOT) {
 *
 * <p>serverShotSeen[0]++;
 *
 * <p>// Server Shot 1-3 -> Treffer if (serverShotSeen[0] <= 3) {
 * client.send(MessageBuilder.answer(1)); return; }
 *
 * <p>// Server Shot 4 -> Wasser if (serverShotSeen[0] == 4) {
 * client.send(MessageBuilder.answer(0)); // jetzt MUSS Server pass schicken phase[0] =
 * Phase.WAIT_SERVER_PASS; return; }
 *
 * <p>return; }
 *
 * <p>// ======= Server schickt pass -> jetzt ist Client dran ======= if ("pass".equals(cmd) &&
 * phase[0] == Phase.WAIT_SERVER_PASS) { phase[0] = Phase.CLIENT_SHOOTING;
 * sendNextClientShot(client, clientShotSent); phase[0] = Phase.WAIT_ANSWER; return; }
 *
 * <p>// ======= Client wartet auf answer nach eigenem shot ======= if ("answer".equals(cmd) &&
 * phase[0] == Phase.WAIT_ANSWER) {
 *
 * <p>int a = Integer.parseInt(msg.split("\\s+")[1]);
 *
 * <p>if (a == 1 || a == 2) { // Treffer -> Client darf noch mal schießen if (clientShotSent[0] < 2)
 * { phase[0] = Phase.CLIENT_SHOOTING; sendNextClientShot(client, clientShotSent); phase[0] =
 * Phase.WAIT_ANSWER; } return; }
 *
 * <p>if (a == 0) { // Wasser -> Client muss pass senden -> Turn zurück zum Server
 * client.send(MessageBuilder.pass()); phase[0] = Phase.DONE; System.out.println("[CLIENT]
 * Gameflow-Test abgeschlossen ✅"); keepAlive.countDown(); } }
 *
 * <p>} catch (Exception e) { e.printStackTrace(); keepAlive.countDown(); } } @Override public void
 * onConnectionClosed(Exception e) { System.out.println("[CLIENT] Verbindung geschlossen: " + e);
 * keepAlive.countDown(); } });
 *
 * <p>keepAlive.await();
 *
 * <p>} catch (Exception e) { e.printStackTrace(); } }
 *
 * <p>private static void sendNextClientShot(ClientConnection client, int[] clientShotSent) throws
 * Exception { clientShotSent[0]++;
 *
 * <p>// Client Shot 1..2 int row = 2; int col = clientShotSent[0];
 *
 * <p>client.send(MessageBuilder.shot(row, col)); } }
 */
