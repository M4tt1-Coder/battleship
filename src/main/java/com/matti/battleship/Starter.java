package com.matti.battleship;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Starter {
  private static final Logger logger = LogManager.getLogger(Starter.class);

  public static void main(String[] args) {
    logger.info("Starting Game ... ");
    BattleshipApp.main(args);
  }
}
