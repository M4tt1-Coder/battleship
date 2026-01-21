package com.matti.battleship.computer;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;
import com.matti.battleship.utils.BoardUtils;
import java.util.ArrayList;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlacementAlgorithm {
  private static final Logger logger = LogManager.getLogger(PlacementAlgorithm.class);

  private static final Random random = new Random();

  // Placeholder for future implementation of ship placement algorithms

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

    logger.info("Ship placement algorithm completed.");
  }
}
