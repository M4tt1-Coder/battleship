package socket.network;

import java.io.*;
import java.net.Socket;

public class SocketConnector {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private MessageListener listener;

    public SocketConnector(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public synchronized void sendMessage(String msg) throws IOException {
        writer.write(msg);
        writer.newLine();
        writer.flush();
    }

    public void startListening() {
        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (listener != null) listener.onMessageReceived(line);
                }
            } catch (Exception e) {
                if (listener != null) listener.onConnectionClosed(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
