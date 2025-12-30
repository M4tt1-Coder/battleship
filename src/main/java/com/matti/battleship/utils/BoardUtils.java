package com.matti.battleship.utils;

import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the helper functions to execute checks like if a some fields are already occupied etc.
 *
 * @author m4tt1
 */
public class BoardUtils {
  private static final Logger logger = LogManager.getLogger(BoardUtils.class);

  /**
   * Determines if ship can be placed on to the board.
   *
   * @param board Board of the game
   * @param ship Ship which the user / generator wants to place on to the field.
   * @return TRUE, if the ship can't be placed on the board.
   */
  public static boolean AreFieldsOfShipAlreadyOccupied(Board board, Ship ship) {
    // first get all occupied fields of the board
    var occupiedFields = board.getCoordinatesOfOccupiedFields();

    // get supposed occupied fields by the ship
    var intended_fields = ShipUtils.getFieldsOfShip(board, ship);

    // validate if the ship can be placed in that field
    for (Coordinates occupiedField : occupiedFields) {
      for (Coordinates intendedField : intended_fields) {
        if (occupiedField.x == intendedField.x && occupiedField.y == intendedField.y) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Validates the provided data and determines if some coordinates are on the board according to
   * its size.
   *
   * @param coordinates Coordinates of a ship or any field
   * @param boardSize Size of the board
   * @return TRUE, when the coordinates are on the field.
   */
  public static boolean isCoordinateOnBoard(Coordinates coordinates, int boardSize) {
    if (boardSize < 1) {
      logger.error("Board size must be greater than zero");
      return false;
    }
    // when the X or Y ordinate are negative
    if (coordinates.x < 0 || coordinates.y < 0) {
      logger.error(
          "Coordinate coordinates can not be negative numbers! X: {} , Y: {}",
          coordinates.x,
          coordinates.y);
      return false;
    }

    // check that the coordinates are on the board
    return coordinates.x <= boardSize && coordinates.y <= boardSize;
  }

  /**
   * Retrieves all the current occupied fields of a sunken ship on the given board.
   *
   * <p>This method analyzes the board to identify the set of coordinates that belong to the most
   * recently sunken ship. It does so by grouping neighboring occupied fields, validating the ship
   * size, and ensuring the ship hasn't been previously registered.
   *
   * @param board the game board to analyze, which contains information about occupied fields and
   *     ships
   * @return an Array of Coordinates representing all fields occupied by the sunken ship; returns an
   *     empty array if no new sunken ship is found
   * @throws IllegalStateException if a found ship has an invalid size (not between 2 and 5)
   */
  public static Coordinates[] getAllCurrentOccupiedFieldsOfSunkenShip(Board board) {
    ArrayList<Coordinates> output = new ArrayList<>();
    // get all occupied fields
    ArrayList<Coordinates> allOccupiedFields = board.getCoordinatesOfOccupiedFields();
    for (Coordinates c : allOccupiedFields) {
      var field = board.getFieldOnBoardByCoordinates(c);
      allOccupiedFields.remove(c);
      ArrayList<Coordinates> temp = new ArrayList<>();
      temp.add(c);
      for (Coordinates k : allOccupiedFields) {
        if (k == c) continue;
        // check if the field 'k' is an occupied field of the current ship
        // remove it from the 'allOccupiedFields' list -> fasten future iteration cycles
        // check if the 'k' is a neighbor field of a field of the current ship
        for (Coordinates z : temp) {
          if (z.isNeighbour(k)) {
            allOccupiedFields.remove(k);
            temp.add(k);
          }
        }
      }
      // check if the potential ship has a valid length
      if (!ShipUtils.validLength(temp.size())) {
        logger.error("A ship of an invalid size is on the board of the opponent!");
        throw new IllegalStateException("A ship can only be of the size from 2 to 5!");
      }
      // check if a ship has been registered -> if not return 'temp' since that's the ship that was
      // freshly sunken
      boolean shipAlreadyRegistered = false;
      for (Coordinates coordinates : temp) {
        if (board.getFieldOnBoardByCoordinates(coordinates).getShip() != null) {
          shipAlreadyRegistered = true;
          break;
        }
      }
      if (shipAlreadyRegistered) {
        continue;
      } else {
        output = temp;
        break;
      }
    }
    return output.toArray(new Coordinates[0]);
  }

  // ----- private methods ------
}
