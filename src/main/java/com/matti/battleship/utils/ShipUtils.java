package com.matti.battleship.utils;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains the helper functions for working with the ships.
 *
 * @author m4tt1
 */
public class ShipUtils {
  private static final Logger logger = LogManager.getLogger(ShipUtils.class);

  /**
   * Retrieves the fields of a 'Ship' where it is placed on. Executes main check that the fields are
   * on the board.
   *
   * @param board Board of the 'Game' with the 'Ship's on it
   * @param ship 'Ship' who's fields need to be calculated
   * @return Array of 'Coordinates' of the fields on which the ship is placed on.
   */
  public static Coordinates[] getFieldsOfShip(Board board, Ship ship) {
    // generate the field coordinates of the wanted fields
    Coordinates[] fields =
        calcFieldsOfShip(
            ship.getStartCoordinates(), ship.getDirection(), ship.getLength().getValue());

    // validate fields against the game logic
    for (var c : fields) {
      // on field?
      if (c.x < 0 || c.x > board.getSize() - 1 || c.y < 0 || c.y > board.getSize() - 1) {
        logger.error(
            "Ship coordinates: {} not on the board! Can't determine the fields of a ship!",
            c.toString());
        return new Coordinates[] {};
      }
    }
    return fields;
  }

  // ------------ private helper functions -------------------

  /**
   * Ignores any checks and simply creates reference instances of 'Coordinates' of the ships
   * coordinates.
   *
   * @param length Length of the ship
   * @param shipCoordinates Coordinates of the ship
   * @param direction Direction in which the ship is pointing to relative to its 'starting point'
   * @return Array of coordinates of fields where the ship claims to be.
   */
  private static Coordinates[] calcFieldsOfShip(
      Coordinates shipCoordinates, Direction direction, int length) {
    Coordinates[] output = new Coordinates[length];
    for (int i = 0; i < length; i++) {
      switch (direction) {
        case UP:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y - 1);
          break;
        case DOWN:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y + 1);
          break;
        case LEFT:
          output[i] = new Coordinates(shipCoordinates.x - 1, shipCoordinates.y);
          break;
        case RIGHT:
          output[i] = new Coordinates(shipCoordinates.x + 1, shipCoordinates.y);
          break;
      }
    }
    return output;
  }

  /**
   * Validates an integer if it resembles a valid ship length.
   *
   * @param length Ship length to be tested
   * @return TRUE, if the length is valid;
   */
  public static boolean validLength(int length) {
    return length >= 2 && length <= 5;
  }

  /**
   * Converts an integer value to the corresponding ShipLength enum.
   *
   * @param value the integer value representing a ShipLength
   * @return the corresponding ShipLength enum
   * @throws IndexOutOfBoundsException if the value does not correspond to any ShipLength
   */
  public static ShipLength shipLengthFromInt(int value) {
    if (!ShipUtils.validLength(value)) {
      throw new IndexOutOfBoundsException("Invalid value for ShipLength: " + value);
    }
    return ShipLength.values()[value];
  }

  /**
   * Determines the orientation (direction) of a ship based on its field coordinates.
   *
   * <p>Assumes that the input array contains exactly two Coordinates that represent the positions
   * of two adjacent fields of a ship. The method checks the relative positions to determine whether
   * the ship is oriented horizontally (to the right) or vertically (downwards).
   *
   * @param shipFieldCoordinates an array of Coordinates representing two adjacent fields of a ship
   * @return the Direction indicating the orientation of the ship (Direction.RIGHT or
   *     Direction.DOWN)
   * @throws IllegalArgumentException if the input list has an invalid length, contains duplicate
   *     fields, or does not represent a valid ship orientation
   */
  public static Direction determineShipDirection(Coordinates[] shipFieldCoordinates) {
    // make sure there is a valid number of fields in the list
    if (validLength(shipFieldCoordinates.length)) {
      logger.error("An invalid ship length represented in the list of its fields!");
      throw new IllegalArgumentException("Invalid number coordinates in the list!");
    }
    if (shipFieldCoordinates[0].x != shipFieldCoordinates[1].x) {
      return Direction.RIGHT;
    } else if (shipFieldCoordinates[0].y != shipFieldCoordinates[1].y) {
      return Direction.DOWN;
    } else {
      throw new IllegalArgumentException(
          "There can't be a duplicate of a field inside of the list of fields of a ship!");
    }
  }
}
