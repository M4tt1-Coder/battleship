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

        // Konstruktor bekommt nen Socket
        public SocketConnector(Socket socket) throws IOException {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        // Callback
        public void setMessageListener(MessageListener listener) {
            this.listener = listener;
        }

        // Sendet ne Nachricht zum Server/Client als Text mit \n am ende
        public synchronized void sendMessage(String message) throws IOException {
            writer.write(message);
            writer.newLine();
            writer.flush();
        }

        // Macht nen Thread, der vom Socket liest
        public void startListening() {
            listeningThread = new Thread(() -> {
                try {
                    String line;
                    // zum Lesen
                    while ((line = reader.readLine()) != null) {
                        if (listener != null) {
                            listener.onMessageReceived(line);
                        }
                    }
                // Wenn ein Fehler kommt/Verbindung abbricht
                } catch (Exception e) {
                    if (listener != null) listener.onConnectionClosed(e);
                //Socket schließen
                } finally {
                    try { socket.close(); } catch (Exception ignored) {}   
                }
            });

            listeningThread.setDaemon(true);
            listeningThread.start();
        }

        // Verbindung schließen
        public void close (){
            try {
                socket.close();
            } catch (IOException ignored) {}
        }

        //Schaut ob die Verbindung noch besteht
        public boolean isConnected() {

            return socket != null && socket.isConnected() && !socket.isClosed();
        }
    }
    