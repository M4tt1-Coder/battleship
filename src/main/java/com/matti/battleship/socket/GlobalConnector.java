package com.matti.battleship.socket;
/**
 * import com.matti.battleship.socket.config.EnvConfig; import
 * com.matti.battleship.socket.discovery.ServerDiscoveryResponder; import
 * com.matti.battleship.socket.logging.TurnLog; import
 * com.matti.battleship.socket.network.MessageListener; import
 * com.matti.battleship.socket.network.SocketConnector;
 *
 * <p>import java.io.EOFException; import java.net.ServerSocket; import java.net.Socket; import
 * java.util.concurrent.BlockingQueue; import java.util.concurrent.LinkedBlockingQueue; import
 * java.util.concurrent.TimeUnit; import java.util.concurrent.atomic.AtomicBoolean;
 *
 * <p>public class GlobalConnector implements MessageListener {
 *
 * <p>public void acceptClient() throws Exception { ensureRole(Role.SERVER); if (serverSocket ==
 * null) throw new IllegalStateException("ServerSocket not opened.");
 *
 * <p>socket = serverSocket.accept(); busy.set(true); System.out.println("[SERVER] client connected:
 * " + socket.getInetAddress());
 *
 * <p>initConnector(socket, TurnLog.Side.SERVER);
 *
 * <p>connected = true; closed = false; closedException = null;
 *
 * <p>state = CommState.SEND_MESSAGE; // Server startet "send" }
 *
 * <p>public void connect(String host) throws Exception { ensureRole(Role.CLIENT); int port =
 * EnvConfig.getPort();
 *
 * <p>socket = new Socket(host, port); System.out.println("[CLIENT] connected to server: " + host +
 * " (TCP " + port + ")");
 *
 * <p>initConnector(socket, TurnLog.Side.CLIENT);
 *
 * <p>connected = true; closed = false; closedException = null;
 *
 * <p>state = CommState.RECEIVE_MESSAGE; // Client startet "receive" }
 *
 * <p>public synchronized void stopListening() { if (!listening) return; listening = false; if
 * (connector != null) connector.stopListening(); }
 */
