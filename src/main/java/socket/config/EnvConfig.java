package socket.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvConfig {

    // Lade Umgebungsvariablen aus der port.env Datei
    private static final Properties props = new Properties();

    // Statischer Initialisierungsblock zum Laden der Datei
    static {
        try {
            FileInputStream fis = new FileInputStream("port.env");
            props.load(fis);
        } catch (IOException e) {
            System.out.println("port.env Datei nicht gefunden");
        }
    }

    // Gibt den Port zurück, standardmäßig 50000, falls nicht durch .env bekommen
    public static int getPort() {
        try {
            return Integer.parseInt(props.getProperty("PORT", "50000"));
        } catch (Exception e) {
            return 50000; // Fallback
        }
    }
}

