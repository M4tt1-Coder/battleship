package com.matti.battleship.utils;

import com.matti.battleship.enums.AIDifficulty;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameUtils {
  private static final Logger logger = LogManager.getLogger(GameUtils.class);

  /**
   * Validates the game setup before starting a match. Checks the validity of both players and the
   * initial ship setup.
   *
   * @param player the primary {@link Player} participating in the game
   * @param opponent the opponent {@link Player}
   * @param initialShipSetup an array of {@link ShipLength} representing the initial ship positions
   * @return {@code true} if the entire game setup is valid; {@code false} otherwise
   */
  public static boolean validateGameSetup(
      Player player, Player opponent, ShipLength[] initialShipSetup) {
    // validate player
    if (!validatePlayerInstance(player)) return false;
    // validate opponent
    if (!validatePlayerInstance(opponent)) return false;
    // validate initial ship setup
    return !validateShipSetup(initialShipSetup, player);
  }

  /**
   * Converts a string representation of difficulty level to the corresponding {@link AIDifficulty}
   * enum.
   *
   * @param val the string representing the difficulty level; expected to be "Easy", "Medium", or
   *     "Hard"
   * @return the corresponding {@link AIDifficulty} enum value
   * @throws IllegalArgumentException if the input string does not match any of the expected
   *     difficulty levels
   */
  public static AIDifficulty getDifficultyFromString(String val) {
    switch (val) {
      case "Easy":
        return AIDifficulty.EASY;
      case "Medium":
        return AIDifficulty.MEDIUM;
      case "Hard":
        return AIDifficulty.HARD;
      default:
        throw new IllegalArgumentException("An invalid argument as AIDifficulty was passed!");
    }
  }

  /**
   * Validates whether the given ship setup matches the board's requirements for the specified
   * player. Checks if the total length of all ships equals the number of mandatory occupied fields.
   *
   * @param shipSetup the array of {@link ShipLength} representing the ship configuration to
   *     validate
   * @param player the {@link Player} whose board is used for validation
   * @return {@code true} if the ship setup is valid; {@code false} otherwise
   */
  private static boolean validateShipSetup(ShipLength[] shipSetup, Player player) {
    // Calculate the sum of all ship lengths
    int totalShipLength = 0;
    for (ShipLength shipLength : shipSetup) {
      totalShipLength += shipLength.getValue();
    }

    // Verify if total ship length matches the board's required occupied fields
    int requiredOccupiedFields =
        BoardUtils.getNumberForExactNumberOfMandatoryOccupiedFields(player.board.getSize());

    if (totalShipLength != requiredOccupiedFields) {
      logger.error(
          "Invalid ship setup! Total occupied fields: {}; Board size: {}; Required occupied fields: {}",
          totalShipLength,
          player.board.getSize(),
          requiredOccupiedFields);
      return false;
    }

    return true;
  }

  /**
   * Validates the integrity of a {@link Player} instance at the start of the game. Checks if the
   * player's name is not empty, the initial number of ships on the board is zero, and the board is
   * empty.
   *
   * @param player the {@link Player} instance to validate
   * @return {@code true} if the player instance is valid; {@code false} otherwise
   */
  private static boolean validatePlayerInstance(Player player) {
    // make sure the name is longer then 0 characters
    if (player.getName().isEmpty()) {
      logger.error("The name of the player with the ID {} is empty!", player.getID());
      return false;
    }

    // validate the initial board
    // number of ships should be 0; field should be empty
    if (player.board.numberOfShips != 0) {
      logger.error(
          "The board of the player {} with ID {} doesn't have an initial number of 0 at the start of the game!",
          player.getName(),
          player.getID());
      return false;
    }
    if (!player.board.isBoardEmpty()) {
      logger.error("The board of the player with the ID {} is not empty!", player.getID());
      return false;
    }

    return true;
  }
}
