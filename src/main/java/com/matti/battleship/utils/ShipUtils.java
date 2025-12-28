package com.matti.battleship.utils;

import com.matti.battleship.enums.Direction;
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
        case TOP:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y - 1);
          break;
        case BOTTOM:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y + 1);
          break;
        case LEFT:
          output[i] = new Coordinates(shipCoordinates.x - 1, shipCoordinates.y);
          break;
        case RIGHT:
          output[i] = new Coordinates(shipCoordinates.x + 1, shipCoordinates.y - 1);
          break;
      }
    }
    return output;
  }
}
