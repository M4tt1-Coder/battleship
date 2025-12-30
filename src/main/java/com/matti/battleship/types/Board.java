package com.matti.battleship.types;

import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.ShipUtils;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the game board for Battleship. Contains fields and methods to manage ships and their
 * placement.
 *
 * @author m4tt1
 */
public class Board {
  private static final Logger logger = LogManager.getLogger(Board.class);

  /** Size of the board instance. Maximum size is 15x15 fields. */
  private final int size;

  /** Number of ships currently placed on the board. */
  public int number_of_ships;

  /** 2D array representing the fields on the board. */
  public Field[][] board;

  public Board(int size) {
    // size can only be set during initialization
    // - must be greater than zero and less than 16
    if (size < 1 || size > 15) {
      logger.error("Board size must be between 1 and 15! Provided size: {}", size);
      throw new IllegalArgumentException("Board size must be between 1 and 15!");
    }
    this.size = size;
    this.number_of_ships = 0;
    // creates an empty field with no content
    this.board = new Field[size][size];
  }

  /**
   * Provides the size of a 'Board' instance.
   *
   * @author m4tt1
   * @return The size of a board
   */
  public int getSize() {
    return size;
  }

  // TODO: Maybe use a method which places a single ship on the board instead of an array of ships?

  // TODO: Add validation if the number of ships exceeds a certain limit depending on the board
  // size? -> E.g. 10 ships on a 10x10 board or calculate the maximum number of ships depending on
  // the board

  // TODO: Ships can't be placed directly next to each other. There must be at least one field gap
  // between two ships.

  /**
   * Receives a list of ships to be placed on the board. Then validates the single ships and the
   * whole process. Used for playing against the
   *
   * @param ships Array of 'Ship's selected by the user / generator to be placed on the board of the
   *     'Game'
   * @return TRUE, if the ships were added to the board.
   */
  public boolean addShips(Ship[] ships) {
    // for each ship check if its occupied fields are free
    for (Ship ship : ships) {
      var can_not_be_placed = BoardUtils.AreFieldsOfShipAlreadyOccupied(this, ship);
      if (can_not_be_placed) {
        // log if a ship couldn't be placed on the field
        logger.error("Ship {} can't be placed on the board!", ship.toString());
        return false;
      }
      this.number_of_ships++;
      Field field = this.getFieldOnBoardByCoordinates(ship.getStartCoordinates());
      if (field != null) {
        field.setShip(ship);
        this.markFieldsOfShipAsOccupied(ship);
      } else {
        logger.error(
            "Field at the coordinates {} could not be found and the ship couldn't be placed in consequence!",
            ship.getStartCoordinates());
        return false;
      }
    }

    logger.debug("Added ships on the board!");
    return true;
  }

  /**
   * Since the 'Size' property of can only be set during initialization it isn't mutated. The method
   * clears the 'board' and sets the 'number_of_ships' to null.
   *
   * @author m4tt1
   */
  public void reset() {
    number_of_ships = 0;
    board = new Field[size][size];
  }

  /**
   * Iterates over all fields and looks if a field is occupied or not.
   *
   * <p>Then adds an occupied field to the output list.
   *
   * @return List of occupied fields
   * @author m4tt1
   */
  public ArrayList<Coordinates> getCoordinatesOfOccupiedFields() {
    ArrayList<Coordinates> output_coordinates = new ArrayList<Coordinates>();

    for (Field[] row : this.board) {
      for (Field field : row) {
        if (field.isOccupied()) output_coordinates.add(field.getCoordinates());
      }
    }

    return output_coordinates;
  }

  /**
   * Marks a field on the board as shot at. If the field was already shot at the method returns
   * 'INVALID'. If the field was occupied by a ship it returns 'HIT', otherwise 'MISS'.
   *
   * @param coordinates Coordinates of the field to be shot at
   * @return 'HIT' if the shot landed on a field that is a ship on.
   */
  public ShotAttemptResult shotAtField(Coordinates coordinates) {
    Field field = this.getFieldOnBoardByCoordinates(coordinates);
    if (field == null) {
      logger.error("No field found at the provided coordinates: {}", coordinates);
      return ShotAttemptResult.INVALID;
    }

    if (field.wasShotAt()) {
      logger.debug("Field at {} was already shot at!", coordinates);
      return ShotAttemptResult.INVALID;
    }

    if (field.markAsShotAt() && field.isOccupied()) {
      logger.info("Shot at {} was a HIT!", coordinates);
      return ShotAttemptResult.HIT;
    } else {
      logger.info("Shot at {} was a MISS!", coordinates);
      return ShotAttemptResult.MISS;
    }
  }

  /**
   * Iterates over all fields on the board and checks if a ship has been sunk. There can only be one
   * ship sunk after one shot.
   *
   * @return TRUE, when the ship that was hit with the last shot finally is sunk.
   */
  public boolean checkIfShipWasSunk() {
    for (Field[] row : this.board) {
      for (Field field : row) {
        Ship ship = field.getShip();
        if (ship != null && !ship.getHasSunk()) {
          var shipFields = ShipUtils.getFieldsOfShip(this, ship);
          boolean allFieldsHit = true;
          for (var coord : shipFields) {
            Field shipField = this.getFieldOnBoardByCoordinates(coord);
            if (shipField != null && !shipField.wasShotAt()) {
              allFieldsHit = false;
              break;
            }
          }
          if (allFieldsHit) {
            ship.alterHasSunk();
            logger.info("Ship {} has been sunk!", ship.getId());
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks if all ships on the board have been sunk.
   *
   * @return TRUE, if all ships are sunk; FALSE otherwise
   */
  public boolean areAllShipsSunk() {
    for (Field[] row : this.board) {
      for (Field field : row) {
        Ship ship = field.getShip();
        if (ship != null && !ship.getHasSunk()) {
          return false; // Found a ship that is not sunk
        }
      }
    }
    return true; // All ships are sunk
  }

  // ----- Private Methods -----

  /**
   * Gets the field which is on the coordinates provided.
   *
   * @param coordinates Coordinates of the field
   * @return The 'Field' object if the coordinates are on the board
   */
  public Field getFieldOnBoardByCoordinates(Coordinates coordinates) {
    for (Field[] row : this.board) {
      for (Field field : row) {
        if (field.getCoordinates().equals(coordinates)) {
          return field;
        }
      }
    }
    logger.debug("Could not find field on the board with the coordinates {}!", coordinates);
    return null;
  }

  /**
   * Marks all fields of a ship as occupied on the board.
   *
   * @param ship Ship which fields need to be marked as occupied
   */
  private void markFieldsOfShipAsOccupied(Ship ship) {
    var fields = ShipUtils.getFieldsOfShip(this, ship);
    for (var c : fields) {
      var field = this.getFieldOnBoardByCoordinates(c);
      if (field != null) {
        field.setOccupied(true);
      }
    }
  }
}
