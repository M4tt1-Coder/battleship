package com.matti.battleship.computer;

import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Game;
import com.matti.battleship.utils.BoardUtils;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// uses a probabilistic approach where the algorithm calculates a most likely coordinates where the
// next field of the ship must be
public class HardAlgorithm implements Algorithm {
  private static final Logger logger = LogManager.getLogger(HardAlgorithm.class);

  private final int artificialWeight = 100;

  private HashMap<Coordinates, Integer> heatMap;
  private final DocumentaryShotResult[][] shotResultMap;
  private final int boardSize;
  private final Random rand;

  private enum DocumentaryShotResult {
    NOT_SET,
    HIT,
    MISS,
    SUNK
  }

  HardAlgorithm(int boardSize) {
    this.heatMap = new HashMap<>();
    this.shotResultMap = new DocumentaryShotResult[boardSize][boardSize];
    this.boardSize = boardSize;
    this.rand = new Random();
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        shotResultMap[i][j] = DocumentaryShotResult.NOT_SET;
      }
    }
  }

  @Override
  public Coordinates takeAShot(Game game) {
    Coordinates guessedCoordinates;
    do {
      if (this.heatMap.isEmpty()) {
        guessedCoordinates = BoardUtils.randomCoordinatesOnField(this.boardSize, this.rand);
      } else {
        guessedCoordinates = getNextPotentialTarget();
      }
    } while (checkIfFieldsWasAlreadyShotAt(guessedCoordinates));

    // fire on the board
    ShotAttemptResult attemptResult = game.player.board.shotAtField(guessedCoordinates);

    // according to the result the field(s) need to be marked as such HIT, etc
    if (attemptResult == ShotAttemptResult.HIT) {
      if (game.player.board.checkIfShipWasSunk()) { // when a ship was sunken
        // mark all fields that were marked as DocumentaryShotResult.HIT to SUNK
        markAllFieldsOfShipAsSunk(guessedCoordinates);
        // TODO: mark the fields around the ship as MISS

        logger.info("Sunk ship at {} by the opponent (computer)", guessedCoordinates);
      } else { // when it was only hit
        shotResultMap[guessedCoordinates.x][guessedCoordinates.y] = DocumentaryShotResult.HIT;
      }
    } else if (attemptResult == ShotAttemptResult.MISS) {
      shotResultMap[guessedCoordinates.x][guessedCoordinates.y] = DocumentaryShotResult.MISS;
    }

    calculateHeatMap();
    
    return guessedCoordinates;
  }

  // ----- private methods -----

  private void markAllFieldsAroundShipAsMiss() {}
  
  /**
   * Marks all connected ship fields as sunk starting from the given coordinates.
   *
   * <p>This method performs a breadth-first search (BFS) to find all neighboring fields that are
   * part of the same ship (i.e., connected via straight neighbors with a HIT result), and updates
   * their status to SUNK in the {@link #shotResultMap}.
   *
   * @param coordinates The starting {@link Coordinates} from which to begin marking connected ship
   *     parts.
   */
  private void markAllFieldsOfShipAsSunk(Coordinates coordinates) {
    // have a queue where every next found neighbor HIT field is added -> and check if there are
    // further field of the ship
    Queue<Coordinates> queue = new PriorityQueue<>();
    queue.add(coordinates);

    while (!queue.isEmpty()) {
      Coordinates coordinate = queue.poll();
      for (int i = 0; i < this.boardSize; i++) {
        for (int j = 0; j < this.boardSize; j++) {
          Coordinates temp = new Coordinates(i, j);
          if (coordinate.isNeighbourStraight(temp)
              && shotResultMap[i][j] == DocumentaryShotResult.HIT) {
            queue.add(temp);
            shotResultMap[i][j] = DocumentaryShotResult.SUNK;
          }
        }
      }
    }
  }

  /**
   * Determines the next optimal target coordinate based on the current heat map.
   *
   * <p>The method scans the {@link #heatMap} to identify the coordinate with the highest likelihood
   * of containing a ship segment, according to the computed probabilities.
   *
   * <p>It initializes the output coordinate to (0, 0) and updates it as it finds higher probability
   * values during iteration.
   *
   * @return The {@link Coordinates} object representing the position with the highest probability.
   */
  private Coordinates getNextPotentialTarget() {
    // iterate over the heat map and find the field with to the highest likelihood for a ship to be
    // on it
    Coordinates output = new Coordinates(0, 0);
    int biggestProbability = -1;
    for (Map.Entry<Coordinates, Integer> entry : this.heatMap.entrySet()) {
      if (entry.getValue() > biggestProbability) {
        output = entry.getKey();
        biggestProbability = entry.getValue();
      }
    }
    return output;
  }

  /**
   * Checks whether the specified coordinates have already been targeted in a shot.
   *
   * <p>Returns {@code true} if the cell at the given coordinates has not been shot at yet,
   * indicated by {@link DocumentaryShotResult#NOT_SET}. Returns {@code false} if the cell has
   * already been shot, either resulting in a hit, miss, or sunk.
   *
   * @param coordinates The coordinates to check on the game board.
   * @return {@code true} if the cell has not been shot at; {@code false} otherwise.
   */
  private boolean checkIfFieldsWasAlreadyShotAt(Coordinates coordinates) {
    return this.shotResultMap[coordinates.x][coordinates.y] == DocumentaryShotResult.NOT_SET;
  }

  /**
   * Computes a heat map representing the probability distribution of where remaining ships might be
   * located on the game board.
   *
   * <p>The method analyzes all shot results recorded in {@link #shotResultMap} and evaluates
   * potential ship placements based on current hits and misses. It updates the {@code heatMap} with
   * scores indicating the likelihood of each coordinate containing a segment of a ship, which can
   * be used to inform strategic targeting.
   *
   * <p>The process involves:
   *
   * <ul>
   *   <li>Iterating over all ship lengths defined in {@link ShipLength}.
   *   <li>Scanning each cell of the game board to examine shot results.
   *   <li>For cells with no shot result ({@link DocumentaryShotResult#NOT_SET}), generating
   *       potential placements for ships starting from that coordinate.
   *   <li>For cells with a hit result ({@link DocumentaryShotResult#HIT}), applying artificial
   *       weights to surrounding unshot cells, then generating potential placements based on the
   *       hit coordinate.
   *   <li>Validating each potential placement to ensure it fits within the board and aligns with
   *       known hit/miss data.
   *   <li>Updating the heat map scores for all valid placement coordinates to reflect increased
   *       likelihood.
   * </ul>
   *
   * <p>After processing all cells and ship lengths, the resulting {@code heatMap} provides a
   * probability distribution for the next optimal attack position.
   */
  private void calculateHeatMap() {
    // store the returned result when shooting
    HashMap<Coordinates, Integer> map = new HashMap<>();

    for (ShipLength length : ShipLength.values()) {
      int length_value = length.getValue();

      for (int i = 0; i < this.boardSize; i++) {
        for (int j = 0; j < this.boardSize; j++) {
          DocumentaryShotResult result = this.shotResultMap[i][j];
          Coordinates coordinates = new Coordinates(i, j);
          // after a ship was sunken mark all associated fields of the sunken ship as sunken
          // (DocumentaryShotResult.SUNK) and the surrounded fields as DocumentaryShotResult.MISS
          switch (result) {
            case NOT_SET -> {
              // get the potential placements for the ship
              Coordinates[][] placements = getPlacementFields(length_value, coordinates);
              considerPlacementsForHeatMap(map, placements);
            }
            case HIT -> {
              // apply an artificial weight to the surrounding fields if they were not shot at
              applyArtificialWeightToSurroundingFields(map, coordinates);
              // get the potential placements for the ship
              Coordinates[][] placements = getPlacementFields(length_value, coordinates);
              considerPlacementsForHeatMap(map, placements);
            }
          }
        }
      }
    }

    this.heatMap = map;
  }

  /**
   * Evaluates and updates the heat map with valid ship placements.
   *
   * <p>For each provided ship placement configuration, the method checks whether all coordinates
   * are within the board boundaries and whether each coordinate is either unshot (NOT_SET) or a hit
   * (HIT). Valid placements are then used to increment the corresponding entries in the heat map,
   * indicating the likelihood of these positions containing parts of a ship.
   *
   * @param map The heat map storing scores for each coordinate, where counts are incremented for
   *     valid placements.
   * @param placements An array of coordinate sequences representing potential ship placements to
   *     consider.
   */
  private void considerPlacementsForHeatMap(
      HashMap<Coordinates, Integer> map, Coordinates[][] placements) {
    // validate all placements -> need to be on the board and all fields must either be a
    // DocumentaryShotResult.HIT or NOT_SET
    for (Coordinates[] placement : placements) {
      boolean isPlacementValid = true;
      for (Coordinates placementField : placement) {
        if (!BoardUtils.isCoordinateOnBoard(placementField, this.boardSize)
            || (shotResultMap[placementField.x][placementField.y] != DocumentaryShotResult.NOT_SET
                && shotResultMap[placementField.x][placementField.y]
                    != DocumentaryShotResult.HIT)) {
          isPlacementValid = false;
          break;
        }
      }
      if (isPlacementValid) {
        for (Coordinates placementField : placement) {
          map.put(placementField, 1 + map.get(placementField));
        }
      }
    }
  }

  /**
   * Calculates potential placement coordinates for a ship of a given length starting from the
   * specified coordinates. Checks only the downward and rightward directions.
   *
   * @param shipLength The length of the ship to be placed.
   * @param coordinates The starting coordinates for placement.
   * @return A 2D array of Coordinates where: - The first row (index 0) contains coordinates for
   *     placement downward (Direction.DOWN). - The second row (index 1) contains coordinates for
   *     placement to the right (Direction.RIGHT).
   */
  private Coordinates[][] getPlacementFields(int shipLength, Coordinates coordinates) {
    // only the Direction.DOWN and Direction.RIGHT need to be checked
    // calculate the coordinates
    Coordinates[][] placementFields = new Coordinates[2][shipLength];
    for (int i = 0; i < shipLength; i++) {
      placementFields[0][i] = new Coordinates(coordinates.x, coordinates.y + i);
      placementFields[1][i] = new Coordinates(coordinates.x + i, coordinates.y);
    }

    return placementFields;
  }

  /**
   * Applies an artificial weight to all surrounding fields of the given coordinate by adding a
   * predefined weight to their current values in the provided map, but only if the corresponding
   * position in {@code shotResultMap} is not set.
   *
   * @param map A HashMap mapping Coordinates to their associated weight values.
   * @param coordinates The central coordinate whose surrounding fields will be weighted.
   */
  private void applyArtificialWeightToSurroundingFields(
      HashMap<Coordinates, Integer> map, Coordinates coordinates) {
    ArrayList<Coordinates> surroundingFields = surroundingFields(coordinates);

    for (Coordinates surroundingField : surroundingFields) {
      if (this.shotResultMap[surroundingField.x][surroundingField.y]
          == DocumentaryShotResult.NOT_SET) {
        map.put(surroundingField, artificialWeight + map.get(surroundingField));
      }
    }
  }

  /**
   * Retrieves the list of valid surrounding coordinates adjacent to the given coordinate. Considers
   * the four orthogonal directions: up, right, down, and left.
   *
   * @param coordinates The reference coordinate for which surrounding fields are sought.
   * @return An ArrayList of Coordinates representing the neighboring fields within the board
   *     boundaries.
   */
  private ArrayList<Coordinates> surroundingFields(Coordinates coordinates) {
    ArrayList<Coordinates> surroundingFields = new ArrayList<>();
    Coordinates[] potentialFieldCoordinates = {
      new Coordinates(coordinates.x, coordinates.y - 1),
      new Coordinates(coordinates.x + 1, coordinates.y),
      new Coordinates(coordinates.x - 1, coordinates.y),
      new Coordinates(coordinates.x, coordinates.y + 1)
    };

    for (Coordinates coordinate : potentialFieldCoordinates) {
      if (coordinate.y >= 0
          && coordinate.y < this.boardSize
          && coordinate.x >= 0
          && coordinate.x < this.boardSize) {
        surroundingFields.add(coordinate);
      }
    }

    return surroundingFields;
  }
}
