package socket.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnvConfig {

    private static final Properties props = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("port.env");
            props.load(fis);
        } catch (IOException e) {
            System.out.println("port.env Datei nicht gefunden");
        }
    }

    public static int getPort() {
        try {
            return Integer.parseInt(props.getProperty("PORT", "50000"));
        } catch (Exception e) {
            return 50000; // Fallback
        }
    }
}

