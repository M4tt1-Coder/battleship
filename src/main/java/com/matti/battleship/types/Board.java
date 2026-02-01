package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.ShipUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

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
  public static final float shareOfShipsOnTheBoard = 0.3f;

  /** Size of the board instance. Maximum size is 15x15 fields. */
  private final int size;

  /** Number of ships currently placed on the board. */
  public int numberOfShips;

  /** 2D array representing the fields on the board. */
  public Field[][] board;

  public Board(int size) {
    // size can only be set during initialization
    // - must be greater than one and less than 16
    if (size < 5 || size > 30) {
      logger.error("Board size must be between 5 and 30! Provided size: {}", size);
      throw new IllegalArgumentException("Board size must be between 5 and 30!");
    }
    this.size = size;
    this.numberOfShips = 0;

    // creates an empty field with no content
    this.board = new Field[size][size];

    // prepare the single fields
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        board[i][j] = new Field();
        board[i][j].setCoordinates(new Coordinates(i, j));
      }
    }
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
    // when the maximum capacity of ships is reached it automatically shouldn't be
    // possible to add a
    // ship anymore
    if (isTheMaxCapacityForShipsReached()) return false;
    var canBePlaced = BoardUtils.canShipBePlacedOnBoard(this, ship);
    if (!canBePlaced) {
      // log if a ship couldn't be placed on the field
      logger.error("Ship {} can't be placed on the board!", ship.toString());
      return false;
    }
    this.numberOfShips++;
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

    logger.info("Added ship to the board!");
    return true;
  }

  /**
   * Attempts to rotate the specified ship to a new direction on the board.
   *
   * <p>The method first unmarks the current fields occupied by the ship, then updates the ship's
   * direction, and checks if the ship can be placed at its current location with the new
   * orientation. If placement is valid, the ship's fields are updated accordingly; otherwise, the
   * ship's direction and occupied fields are reverted to their original state.
   *
   * @param shipCoordinates the coordinates of the ship's current position on the board
   * @param newDirection the desired new direction to rotate the ship
   * @return {@code true} if the rotation was successful; {@code false} otherwise
   */
  public boolean rotateShip(Coordinates shipCoordinates, Direction newDirection) {
    Field field = this.getFieldOnBoardByCoordinates(shipCoordinates);
    if (field.getShip() == null) {
      logger.error("Ship couldn't be found at {}! Can't rotate it!", shipCoordinates.toString());
      return false;
    }
    Ship ship = field.getShip();
    Direction oldDirection = ship.getDirection();
    // reset all fields of the ship that are occupied by it
    unmarkAllFieldsAsOccupied(ship);
    // update direction of the ship
    ship.setDirection(newDirection);
    field.setShip(null);
    // check if the ship can be placed there
    if (BoardUtils.canShipBePlacedOnBoard(this, ship)) {
      // mark the new fields
      field.setShip(ship);
      markFieldsOfShipAsOccupied(ship);
      return true;
    } else {
      ship.setDirection(oldDirection);
      field.setShip(ship);
      markFieldsOfShipAsOccupied(ship);
      // reset to the old placement if the new one is invalid
      return false;
    }
  }

  /**
   * Removes a ship from the game board based on the provided coordinates.
   *
   * <p>The method locates the ship occupying the specified position, unmarks all fields occupied by
   * the ship, and removes it from the board. If no ship is found at the given coordinates, the
   * method logs an error and returns null.
   *
   * @param shipCoordinates the coordinates where part of the ship is located
   * @return the removed Ship object if removal was successful; null otherwise
   */
  public Ship removeShip(Coordinates shipCoordinates) {
    Field field = this.getFieldOnBoardByCoordinates(shipCoordinates);
    if (field.getShip() == null) {
      logger.error("Ship couldn't be found at {}! Can't remove it!", shipCoordinates.toString());
      return null;
    }
    Ship ship = field.getShip();
    unmarkAllFieldsAsOccupied(ship);
    field.setShip(null);
    this.numberOfShips--;
    logger.debug("Removed ship {} from the board!", ship.toString());
    return ship;
  }

  /**
   * Since the 'Size' property of can only be set during initialization it isn't mutated. The method
   * clears the 'board' and sets the 'number_of_ships' to null.
   *
   * @author m4tt1
   */
  public void reset() {
    numberOfShips = 0;
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
        if (field.getCoordinates().x == coordinates.x
            && field.getCoordinates().y == coordinates.y) {
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
          if (coord.isNeighbourDiagonal(field.getCoordinates())) {
            output_coordinates.add(field.getCoordinates());
          }
        }
      }
    }
    return output_coordinates.toArray(new Coordinates[0]);
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

  /**
   * Retrieves the {@link Ship} located at the specified {@link Coordinates} on the game board.
   *
   * <p>This method first checks if the field at the given coordinates is occupied and contains a
   * ship. If not directly found, it performs a breadth-first search (BFS) to locate connected
   * fields occupied by the same ship. If a ship is found, it is returned; otherwise, {@code null}
   * is returned, and an error is logged.
   *
   * @param coordinates the {@link Coordinates} to search for a ship.
   * @return the {@link Ship} at the specified coordinates, or {@code null} if no ship is found.
   * @throws NullPointerException if no field exists at the given coordinates.
   * @throws IllegalStateException if the starting field at the coordinates is not occupied.
   */
  @Nullable
  public Ship getShipByCoordinates(Coordinates coordinates) {
    Field accordingfield = this.getFieldOnBoardByCoordinates(coordinates);
    if (accordingfield == null) {
      throw new NullPointerException(
          "Didn't find a field at these coordinates: " + coordinates.toString());
    }
    if (!accordingfield.isOccupied()) {
      throw new IllegalStateException("Starting field to retrieve ship is not occupied!");
    }
    if (accordingfield.getShip() != null) {
      return accordingfield.getShip();
    }
    Queue<Coordinates> queue = new ArrayDeque<>();
    queue.add(coordinates);
    while (!queue.isEmpty()) {
      Coordinates curCoordinates = queue.poll();
      // find the field that is at the start coordinates of the ship
      for (Field[] row : this.board) {
        for (Field field : row) {
          if (!field.isOccupied()) {
            continue;
          }
          if (curCoordinates.isNeighbourStraight(field.getCoordinates())
              && field.getShip() != null) {
            return field.getShip();
          } else if (curCoordinates.isNeighbourStraight(field.getCoordinates())) {
            queue.add(field.getCoordinates());
          }
        }
      }
    }
    logger.error("Couldn't find a ship at the required coordinates!");
    return null;
  }

  /**
   * Retrieves the coordinates of all fields occupied by ships that have been sunk on the current
   * board.
   *
   * <p>This method iterates through all fields on the game board, checks if the field has been shot
   * at, and if it belongs to a ship that has sunk. For each such field, it retrieves all
   * coordinates occupied by that ship and adds them to the output list.
   *
   * <p>The method returns an array of {@link Coordinates} representing all fields of sunk ships.
   * Duplicate coordinates are unlikely but possible if multiple fields of the same ship are
   * processed; however, since ships are sunk, this case should not occur.
   *
   * @return An array containing the {@link Coordinates} of all fields occupied by sunk ships.
   */
  public Coordinates[] getAllFieldsOfCurrentSunkenShips() {
    ArrayList<Coordinates> output = new ArrayList<>();

    for (Field[] row : this.board) {
      for (Field field : row) {
        if (field.wasShotAt() && field.getShip() != null && field.getShip().getHasSunk()) {
          Coordinates[] shipCoordinates = ShipUtils.getFieldsOfShip(this, field.getShip());
          for (Coordinates coor : shipCoordinates) {
            output.add(coor);
          }
        }
      }
    }

    return output.toArray(new Coordinates[0]);
  }

  // ----- Private Methods -----

  /**
   * Resets the occupation status of all fields occupied by the specified ship.
   *
   * <p>This method retrieves all the fields associated with the given ship and sets their occupied
   * status to {@code false}, effectively unmarking them as occupied on the board.
   *
   * @param ship the {@link Ship} object whose occupied fields are to be unmarked
   */
  private void unmarkAllFieldsAsOccupied(Ship ship) {
    var fields = ShipUtils.getFieldsOfShip(this, ship);
    for (var c : fields) {
      var field = this.getFieldOnBoardByCoordinates(c);
      if (field != null) {
        field.setOccupied(false);
      }
    }
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
          if (coord.isNeighbourDiagonal(field.getCoordinates()) && !field.wasShotAt()) {
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
    int numberOfMandatoryOccupiedFields =
        BoardUtils.getNumberForExactNumberOfMandatoryOccupiedFields(this.size);
    int numberOfOccupiedFields = getNumberOfOccupiedFields();
    // count the occupied fields on the field
    return numberOfOccupiedFields >= numberOfMandatoryOccupiedFields;
  }
}
