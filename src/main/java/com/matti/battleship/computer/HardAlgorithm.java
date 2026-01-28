package com.matti.battleship.computer;

import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Field;
import com.matti.battleship.types.Game;
import com.matti.battleship.utils.BoardUtils;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// uses a probabilistic approach where the algorithm calculates a most likely coordinates where the
// next field of the ship must be`

/**
 * Implements a strategic algorithm for targeting in a Battleship game. It uses heat maps,
 * heuristics, and BFS-based methods to decide the next shot based on current game state.
 *
 * <p>Represents the hard level for the 'PLAYER_AI' playing mode.
 */
public class HardAlgorithm implements Algorithm {
  private static final Logger logger = LogManager.getLogger(HardAlgorithm.class);

  /** Represents the artificial weight used in scoring or decision-making processes. */
  private final int artificialWeight = 100;

  /**
   * A map that associates each coordinate with an integer value representing the heat or likelihood
   * of a ship being present at that position.
   */
  private HashMap<Coordinates, Integer> heatMap;

  /**
   * A 2D array representing the result of each shot on the game board. Possible values are defined
   * by the {@link DocumentaryShotResult} enum.
   */
  private final DocumentaryShotResult[][] shotResultMap;

  /** The size of the game board (number of rows and columns). */
  private final int boardSize;

  /**
   * A {@link Random} instance used for generating random numbers, likely for decision-making or
   * simulation.
   */
  private final Random rand;

  /** Enum representing the possible outcomes of a shot. */
  private enum DocumentaryShotResult {
    /** The cell has not been shot at yet. */
    NOT_SET,
    /** The shot hit a part of a ship. */
    HIT,
    /** The shot missed all ships. */
    MISS,
    /** The shot resulted in sinking a ship. */
    SUNK
  }

  public HardAlgorithm(int boardSize) {
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
  public void takeAShot(Game game) {
    Coordinates guessedCoordinates;
    do {
      if (this.heatMap.isEmpty()) {
        guessedCoordinates = BoardUtils.randomCoordinatesOnField(this.boardSize, this.rand);
      } else {
        guessedCoordinates = getNextPotentialTarget();
      }
    } while (checkIfFieldsWasAlreadyShotAt(guessedCoordinates));

    // fire on the board
    ShotAttemptResult attemptResult = game.shotShot(guessedCoordinates);

    // according to the result the field(s) need to be marked as such HIT, etc
    if (attemptResult == ShotAttemptResult.HIT) {
      shotResultMap[guessedCoordinates.y][guessedCoordinates.x] = DocumentaryShotResult.HIT;
    } else if (attemptResult == ShotAttemptResult.MISS) {
      shotResultMap[guessedCoordinates.y][guessedCoordinates.x] = DocumentaryShotResult.MISS;
    } else if (attemptResult == ShotAttemptResult.SUNK) {
      // mark all fields that were marked as DocumentaryShotResult.HIT to SUNK
      markAllFieldsOfShipAsSunk(guessedCoordinates);
      // mark the fields around the ship as MISS
      markAllFieldsAroundSunkenShipAsMiss(guessedCoordinates);
    }

    calculateHeatMap(game.player.board); // can be any board -> contents here irrelevant

    // if we hit a ship -> keep firing
    if (attemptResult != ShotAttemptResult.MISS) {
      takeAShot(game);
    }
    logger.info("Finished firing!");
  }

  // ----- private methods -----

  /**
   * Marks all fields surrounding a sunken ship as MISS in the shot result map.
   *
   * <p>This method performs a breadth-first search (BFS) starting from the provided coordinates of
   * a sunken ship segment. It traverses all connected parts of the sunken ship, and for each
   * neighboring cell that is directly adjacent (up, down, left, right) and has not been processed,
   * it marks neighboring cells that are not yet set as MISS. This indicates that no ship occupies
   * those adjacent cells.
   *
   * <p><strong>Preconditions:</strong>
   *
   * <ul>
   *   <li>The field at the provided {@code coordinates} must have a shot result of {@code
   *       DocumentaryShotResult.SUNK}.
   * </ul>
   *
   * <p><strong>Postconditions:</strong>
   *
   * <ul>
   *   <li>All neighboring fields of the sunken ship that are not yet set are marked as {@code
   *       DocumentaryShotResult.MISS}.
   * </ul>
   *
   * @param coordinates the {@code Coordinates} object representing a position of a sunken ship
   *     segment.
   */
  private void markAllFieldsAroundSunkenShipAsMiss(Coordinates coordinates) {
    // the source field needs to be marked as DocumentaryShotResult.SUNK -> else the
    // ship hasn't
    // been sunk
    if (shotResultMap[coordinates.y][coordinates.x] != DocumentaryShotResult.SUNK) {
      logger.error(
          "Can't mark fields around sunken ship as MISS cause, the fields of it haven't been marked SUNK!");
      return;
    }

    Queue<Coordinates> queue = new PriorityQueue<>();
    ArrayList<Coordinates> alreadyVisited = new ArrayList<>();
    queue.add(coordinates);

    while (!queue.isEmpty()) {
      Coordinates coordinate = queue.poll();
      alreadyVisited.add(coordinate);
      for (int i = 0; i < this.boardSize; i++) {
        for (int j = 0; j < this.boardSize; j++) {
          Coordinates temp = new Coordinates(j, i);
          if (coordinate.isNeighbourStraight(temp)
              && shotResultMap[i][j] == DocumentaryShotResult.SUNK
              && !alreadyVisited.contains(temp)) { // avoid an infinite loop
            queue.add(temp);
          } else if (coordinate.isNeighbourDiagonal(temp)
              && shotResultMap[i][j] == DocumentaryShotResult.NOT_SET) {
            shotResultMap[i][j] = DocumentaryShotResult.MISS;
          }
        }
      }
    }
  }

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
    // have a queue where every next found neighbor HIT field is added -> and check
    // if there are
    // further field of the ship
    Queue<Coordinates> queue = new PriorityQueue<>();
    queue.add(coordinates);

    while (!queue.isEmpty()) {
      Coordinates coordinate = queue.poll();
      shotResultMap[coordinate.y][coordinate.x] = DocumentaryShotResult.SUNK;
      for (int i = 0; i < this.boardSize; i++) {
        for (int j = 0; j < this.boardSize; j++) {
          Coordinates temp = new Coordinates(j, i);
          if (coordinate.isNeighbourStraight(temp)
              && shotResultMap[i][j] == DocumentaryShotResult.HIT) {
            queue.add(temp);
            // shotResultMap[i][j] = DocumentaryShotResult.SUNK;
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
    // iterate over the heat map and find the field with to the highest likelihood
    // for a ship to be
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
   * Checks whether the field at the given {@link Coordinates} has already been shot at.
   *
   * @param coordinates the {@link Coordinates} of the field to check.
   * @return {@code true} if the field has already been shot at; {@code false} otherwise.
   */
  private boolean checkIfFieldsWasAlreadyShotAt(Coordinates coordinates) {
    return this.shotResultMap[coordinates.y][coordinates.x] != DocumentaryShotResult.NOT_SET;
  }

  /**
   * Calculates the heat map for the current game state based on previous shot results and ship
   * placements.
   *
   * <p>This method iterates through all possible ship lengths and analyzes the current board to
   * identify potential locations where ships might be placed. It updates a heat map that indicates
   * the likelihood of each cell containing a ship segment, aiding in strategic decision-making.
   *
   * <p>The process involves:
   *
   * <ul>
   *   <li>Iterating over each cell of the board and examining the result of the previous shot in
   *       that cell.
   *   <li>For cells with no prior shot (NOT_SET), evaluating all possible placements of each ship
   *       length that include this cell.
   *   <li>For cells where a hit was registered (HIT), increasing the weight of surrounding cells to
   *       prioritize areas near hits, as ships tend to be contiguous.
   *   <li>For sunk ships or misses (SUNK, MISS), no further action is taken for that cell.
   *   <li>Using helper methods like {@code getPlacementFields} to generate potential ship
   *       placements, {@code considerPlacementsForHeatMap} to update heat scores based on these
   *       placements, and {@code applyArtificialWeightToSurroundingFields} to emphasize cells
   *       adjacent to hits.
   * </ul>
   *
   * After processing all cells and ship lengths, the resulting heat map reflects the most probable
   * locations for remaining ships, guiding the next move.
   *
   * @param board the current state of the game board, used as a basis for heat map calculation.
   */
  private void calculateHeatMap(Board board) {
    // store the returned result when shooting
    HashMap<Coordinates, Integer> map = prepareNewMap(board);

    for (ShipLength length : ShipLength.values()) {
      int length_value = length.getValue();

      for (int i = 0; i < this.boardSize; i++) {
        for (int j = 0; j < this.boardSize; j++) {
          DocumentaryShotResult result = this.shotResultMap[i][j];
          Coordinates coordinates = new Coordinates(j, i);
          // after a ship was sunken mark all associated fields of the sunken ship as
          // sunken
          // (DocumentaryShotResult.SUNK) and the surrounded fields as
          // DocumentaryShotResult.MISS
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
            case MISS, SUNK -> {}
          }
        }
      }
    }

    this.heatMap = map;
  }

  /**
   * Initializes and returns a new heat map for the current board state.
   *
   * <p>This method creates a HashMap where each key is a set of coordinates corresponding to a cell
   * on the game board, and each value is an integer representing the likelihood or weight of that
   * cell containing a part of a ship. Initially, all cells are assigned a weight of zero.
   *
   * <p>The method iterates through all fields in the board's grid, retrieves their coordinates, and
   * populates the map accordingly.
   *
   * @param board the current game board containing fields with their coordinates.
   * @return a HashMap mapping each coordinate to an initial weight of zero.
   */
  private HashMap<Coordinates, Integer> prepareNewMap(Board board) {
    HashMap<Coordinates, Integer> map = new HashMap<>();
    for (Field[] row : board.board) {
      for (Field field : row) {
        map.put(field.getCoordinates(), 0);
      }
    }
    return map;
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
    // validate all placements -> need to be on the board and all fields must either
    // be a
    // DocumentaryShotResult.HIT or NOT_SET
    for (Coordinates[] placement : placements) {
      boolean isPlacementValid = true;
      for (Coordinates placementField : placement) {
        // needed to check if the coordinates are valid before trying to access the
        // 'shotResultMap' array -> index out of bound
        if (!BoardUtils.isCoordinateOnBoard(placementField, this.boardSize)) {
          isPlacementValid = false;
          break;
        }
        if (shotResultMap[placementField.y][placementField.x] != DocumentaryShotResult.NOT_SET
            && shotResultMap[placementField.y][placementField.x] != DocumentaryShotResult.HIT) {
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
      if (this.shotResultMap[surroundingField.y][surroundingField.x]
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
