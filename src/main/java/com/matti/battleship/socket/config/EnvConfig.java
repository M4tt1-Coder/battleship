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
