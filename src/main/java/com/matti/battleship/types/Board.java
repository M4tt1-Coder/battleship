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

  /**
   * Defines the exact percentage of all fields that need to be occupied by ships. There can't be
   * less or more ships on the field!
   *
   * <p>Here 30% of the fields need to occupied by a ship.
   */
  private static final float shareOfShipsOnTheBoard = 0.3f;

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

  // ----- methods -----

  /**
   * Provides the size of a 'Board' instance.
   *
   * @author m4tt1
   * @return The size of a board
   */
  public int getSize() {
    return size;
  }

  /**
   * Attempts to add a ship to the game board at its starting coordinates. Checks if the ship's
   * occupied fields are free and places the ship if possible.
   *
   * <p>If the ship cannot be placed due to occupied fields, logs an error and returns {@code
   * false}. If the placement is successful, increments the number of ships, sets the ship on the
   * starting field, and marks its fields as occupied.
   *
   * @param ship the {@link Ship} instance to be added to the board
   * @return {@code true} if the ship was successfully added; {@code false} otherwise
   */
  public boolean addShip(Ship ship) {
    // when the maximum capacity of ships is reached it automatically shouldn't be possible to add a
    // ship anymore
    if (isTheMaxCapacityForShipsReached()) return false;
    var canBePlaced = BoardUtils.canShipBePlacedOnBoard(this, ship);
    if (!canBePlaced) {
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

    logger.debug("Added ship to the board!");
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
            markFieldsAroundSunkenShipAsShotAt(shipFields);
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
   * Retrieves all coordinates surrounding the specified ship's coordinates. This includes all
   * neighboring fields (adjacent horizontally, vertically, or diagonally) around each coordinate of
   * the ship.
   *
   * @param shipCoordinates an array of {@link Coordinates} representing the positions of the ship's
   *     fields
   * @return an array of {@link Coordinates} representing the neighboring fields around the ship
   */
  public Coordinates[] getFieldsAroundShip(Coordinates[] shipCoordinates) {
    ArrayList<Coordinates> output_coordinates = new ArrayList<>();
    for (Field[] rows : this.board) {
      for (Field field : rows) {
        for (Coordinates coord : shipCoordinates) {
          if (coord.isNeighbour(field.getCoordinates())) {
            output_coordinates.add(coord);
          }
        }
      }
    }
    return output_coordinates.toArray(new Coordinates[0]);
  }

  /**
   * Calculates the number of ships needed to exactly occupy the specified share of the board.
   *
   * @return the number of ships required.
   */
  public int getNumberForExactNumberOfMandatoryOccupiedFields() {
    int totalCells = this.size * this.size;
    double requiredShips = totalCells / shareOfShipsOnTheBoard;
    return (int) Math.ceil(requiredShips);
  }

  /**
   * Counts the total number of fields on the board that are currently occupied.
   *
   * <p>Iterates through each field in the 2D board array and increments a counter for every field
   * that is marked as occupied.
   *
   * @return the total count of occupied fields.
   */
  public int getNumberOfOccupiedFields() {
    int output = 0;
    for (Field[] row : this.board) {
      for (Field field : row) {
        if (field.isOccupied()) output++;
      }
    }
    return output;
  }

  /**
   * Checks whether the game board is completely empty. A board is considered empty if none of the
   * fields are occupied or contain a ship.
   *
   * @return {@code true} if the board has no occupied fields or ships; {@code false} otherwise
   */
  public boolean isBoardEmpty() {
    for (Field[] row : this.board) {
      for (Field field : row) {
        if (field.isOccupied() || field.getShip() != null) {
          return false;
        }
      }
    }
    return true;
  }

  // ----- Private Methods -----

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

  /**
   * Marks all fields surrounding the specified sunken ship's coordinates as shot, if they have not
   * been shot already. This typically reflects the game rule that fields adjacent to a sunken ship
   * are considered safe to target.
   *
   * @param fieldCoordinates an array of {@link Coordinates} representing the positions of the
   *     sunken ship's fields
   */
  private void markFieldsAroundSunkenShipAsShotAt(Coordinates[] fieldCoordinates) {
    for (Field[] rows : this.board) {
      for (Field field : rows) {
        for (Coordinates coord : fieldCoordinates) {
          if (coord.isNeighbour(field.getCoordinates()) && !field.wasShotAt()) {
            // mark as 'shotAt'
            field.markAsShotAt();
          }
        }
      }
    }
  }

  /**
   * Checks whether the maximum capacity for ships on the board has been reached.
   *
   * <p>Compares the current number of occupied fields to the required number of occupied fields
   * based on the maximum ship capacity. If the number of occupied fields is greater than or equal
   * to the maximum required, it indicates that the capacity has been reached or exceeded.
   *
   * @return true if the maximum capacity for ships has been reached or exceeded; false otherwise.
   */
  private boolean isTheMaxCapacityForShipsReached() {
    int numberOfMandatoryOccupiedFields = getNumberForExactNumberOfMandatoryOccupiedFields();
    int numberOfOccupiedFields = getNumberOfOccupiedFields();
    // count the occupied fields on the field
    return numberOfOccupiedFields >= numberOfMandatoryOccupiedFields;
  }
}
