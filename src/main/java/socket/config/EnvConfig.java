package socket.config;

import java.io.FileInputStream;
import java.util.Properties;

public class EnvConfig {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream(".env")) {
            props.load(fis);
        } catch (Exception e) {
            throw new RuntimeException(".env Datei nicht gefunden");
        }
    }

    public static int getPort() {
        return Integer.parseInt(props.getProperty("PORT", "50000"));
    }

    public static String getDiscoveryMessage() {
        return props.getProperty("DISCOVERY_MESSAGE", "BATTLESHIP_SERVER");
    }
}
