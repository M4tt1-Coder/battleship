package com.matti.battleship.socket.config;

import java.io.FileInputStream;
import java.util.Properties;

public class EnvConfig {

  private static final int DEFAULT_PORT = 50000;
  private static final Properties props = new Properties();

  static {
    try (FileInputStream fis = new FileInputStream(".env")) {
      props.load(fis);
    } catch (Exception e) {
    }
  }

  /**
   *
   * @return DEFAULT_Port -> gives the private static final int DEFAULT_PORT = 50000,
   *         if it couldn't find the variable in .env
   */
  public static int getPort() {
    String p = props.getProperty("PORT");
    if (p == null) return DEFAULT_PORT;

    try {
      return Integer.parseInt(p.trim());
    } catch (NumberFormatException e) {
      return DEFAULT_PORT;
    }
  }
}
