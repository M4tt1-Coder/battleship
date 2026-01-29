package com.matti.battleship.computer;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;
import com.matti.battleship.utils.BoardUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The PlacementAlgorithm class provides a framework for placing ships on a game board.
 *
 * <p>Currently, this class contains a placeholder for future ship placement algorithms. The static
 * method {@link #placeShips(Board, ShipLength[])} implements a basic random placement strategy.
 */
public class PlacementAlgorithm {
  private static final Logger logger = LogManager.getLogger(PlacementAlgorithm.class);

  private static final Random random = new Random();

  /**
   * Places a set of ships on the given board based on the provided ship setup.
   *
   * <p>For each ship length in the setup, the method attempts to randomly place the ship on the
   * board without overlapping existing ships or repeating tried coordinates. It randomly selects
   * coordinates and orientations until a valid placement is found.
   *
   * @param board the game board where ships will be placed
   * @param shipSetup an array of {@link ShipLength} specifying the sizes of ships to place
   */
  public static void placeShips(Board board, ShipLength[] shipSetup) {
    logger.info("Starting ship placement algorithm.");

    ArrayList<Coordinates> alreadyTriedCoordinates = new ArrayList<>();

    // loop through each ship length in the setup
    for (ShipLength shipLength : shipSetup) {
      boolean placed = false;

      while (!placed) {
        // get random field on the board
        Coordinates coordinates = BoardUtils.randomCoordinatesOnField(board.getSize(), random);

        // if field is occupied, try again
        if (board.getFieldOnBoardByCoordinates(coordinates).isOccupied()
            || alreadyTriedCoordinates.contains(coordinates)) continue;

        alreadyTriedCoordinates.add(coordinates);

        Ship ship;

        // get random orientation
        for (Direction direction : Direction.values()) {
          // create ship
          ship = new Ship(coordinates, direction, shipLength);

          // check if ship can be placed
          if (board.addShip(ship)) {
            placed = true;
            break;
          }
        }
      }
    }
  }

  /**
   * Attempts to place all ships on the board using backtracking.
   *
   * @param board the game board
   * @param ships array of ship lengths to place
   * @param index the current ship index to place
   * @return true if all ships are successfully placed; false otherwise
   */
  private static boolean placeShipsBacktracking(Board board, ShipLength[] ships, int index) {
    if (index >= ships.length) {
      // All ships placed successfully
      return true;
    }

    ShipLength currentShipLength = ships[index];
    List<Ship> possiblePlacements = new ArrayList<>();

    // Generate all valid placements for the current ship
    for (int x = 0; x < board.getSize(); x++) {
      for (int y = 0; y < board.getSize(); y++) {
        Coordinates start = new Coordinates(x, y);
        for (Direction direction : Direction.values()) {
          Ship ship = new Ship(start, direction, currentShipLength);
          if (BoardUtils.canShipBePlacedOnBoard(board, ship)) {
            possiblePlacements.add(ship);
          }
        }
      }
    }

    // Shuffle to add randomness
    Collections.shuffle(possiblePlacements, new Random());

    // Try placing each possible ship
    for (Ship candidateShip : possiblePlacements) {
      if (board.addShip(candidateShip)) {
        // Recursively place next ship
        if (placeShipsBacktracking(board, ships, index + 1)) {
          return true; // Successful placement
        }
        // Backtrack: remove the ship
        board.removeShip(candidateShip.getStartCoordinates());
      }
    }

    // No valid placement found for this ship
    return false;
  }

  /**
   * Initiates the ship placement process using backtracking.
   *
   * @param board the game board
   * @param shipSetup array of ship lengths to be placed
   */
  public static void placeShipsWithBacktracking(Board board, ShipLength[] shipSetup) {
    logger.info("Starting backtracking ship placement.");
    boolean success = placeShipsBacktracking(board, shipSetup, 0);
    if (!success) {
      logger.warn("Failed to place all ships with backtracking!");

    } else {
      logger.info("Successfully placed all ships.");
      BoardUtils.logBoardToConsole(board);
    }
  }
}
