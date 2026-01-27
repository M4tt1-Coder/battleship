/**
 * package com.matti.battleship.socket.test.discoverytest.standard;
 *
 * <p>import com.matti.battleship.socket.config.EnvConfig; import
 * com.matti.battleship.socket.logging.TurnLog; import
 * com.matti.battleship.socket.network.MessageListener; import
 * com.matti.battleship.socket.network.SocketConnector; import java.net.Socket;
 *
 * <p>public class StandardClientConnection {
 *
 * <p>private SocketConnector connector;
 *
 * <p>public void connect(String host, MessageListener listener) throws Exception { int port =
 * EnvConfig.getPort();
 *
 * <p>Socket socket = new Socket(host, port); System.out.println("[CLIENT-STANDARD] verbunden mit
 * Server: " + host + " (TCP " + port + ")");
 *
 * <p>connector = new SocketConnector(socket, new TurnLog(TurnLog.Side.CLIENT));
 * connector.setMessageListener(listener); connector.startListening(); }
 *
 * <p>public void send(String msg) throws Exception { connector.sendMessage(msg); } }
 */
