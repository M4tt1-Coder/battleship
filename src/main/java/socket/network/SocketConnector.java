package socket.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketConnector {

  private final Socket socket;
  private final BufferedReader reader;
  private final BufferedWriter writer;
  private MessageListener listener;
  private Thread listeningThread;

  public SocketConnector(Socket socket) throws IOException {
    this.socket = socket;
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  public void setMessageListener(MessageListener listener) {
    this.listener = listener;
  }

  // Senden einer Nachricht laut beispiel protokolls mit \n am Ende
  public synchronized void sendMessage(String message) throws IOException {
    writer.write(message);
    writer.newLine();
    writer.flush();
  }

  // Start einen Thread, der kontinuierlich readLine macht
  public void startListening() {
    listeningThread =
        new Thread(
            () -> {
              try {
                String line;
                while ((line = reader.readLine()) != null) {
                  if (listener != null) {
                    listener.onMessageReceived(line);
                  }
                }

              } catch (Exception e) {
                if (listener != null) listener.onConnectionClosed(e);
              } finally {
                try {
                  socket.close();
                } catch (Exception ignored) {
                }
              }
            });

    listeningThread.setDaemon(true);
    listeningThread.start();
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }
}
