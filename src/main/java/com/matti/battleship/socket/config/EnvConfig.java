package com.matti.battleship.socket.config;

import java.io.FileInputStream;
import java.util.Properties;


/**
 * Provides environment / configuration values for the socket module.
 * The main purpose of this class is to provide a single source of truth for the port used
 * by both the TCP connection (client/server) and the UDP discovery (if enabled).
 * Configuration is read from a local ".env" file (project root / working directory).
 * If the file or the PORT entry is missing (or invalid), the default port is used.
 *
 * @author WoFabian
 */
public class EnvConfig {

  /** Fallback port if no PORT variable can be read from the .env file. */
  private static final int DEFAULT_PORT = 50000;

  /** Stores all key/value pairs read from the .env file. */
  private static final Properties props = new Properties();

  /*
   * Static initializer: runs once when the class is loaded.
   * It tries to read the ".env" file and load it into {@link #props}.
   * If the file cannot be found or read, it silently falls back to default values.
   * Note: This makes the project more robust in different run-configurations
   * (e.g., different working directories in IDEs).
   *
   * @author WoFabian
   */
  static {
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    } catch (Exception e) {
      // Intentionally ignored: if .env is missing, we use DEFAULT_PORT.
      // If you want debugging here, you could log a warning.
    }
  }

  /**
   * Returns the port configured via the ".env" file.
   * The method looks for a property named "PORT". If it is not set or cannot be parsed as an integer,
   * the {@link #DEFAULT_PORT} value is returned.
   *
   * @return the configured port number or {@link #DEFAULT_PORT} if not available/invalid
   * @author WoFabian
   */
  public static int getPort() {
    // Read PORT from .env properties.
    String p = props.getProperty("PORT");

    // If not present -> use default.
    if (p == null) return DEFAULT_PORT;

    // Try to parse the port value.
    try {
      return Integer.parseInt(p.trim());
    } catch (NumberFormatException e) {

      // Invalid PORT value -> use default.

      return DEFAULT_PORT;
    }
  }
}
