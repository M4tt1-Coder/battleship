package com.matti.battleship.computer;

import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Game;
import com.matti.battleship.utils.BoardUtils;
import java.util.*;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// https://towardsdatascience.com/coding-an-intelligent-battleship-agent-bf0064a4b319/

/**
 * Represents the implementation of the 'normal' or MEDIUM level when the player decides to play
 * against the computer.
 */
public class MediumAlgorithm implements Algorithm {
  private static final Logger logger = LogManager.getLogger(MediumAlgorithm.class);

  /**
   * A priority queue that holds potential target coordinates to shoot at.
   *
   * <p>This queue orders the coordinates based on a specified comparator, such as prioritizing
   * targets based on likelihood, proximity, or other criteria.
   *
   * <p>The priority queue allows efficient retrieval of the most promising target.
   */
  private final PriorityQueue<Coordinates> potentialTargets;

  /**
   * A set that keeps track of all the coordinates where shots have been fired. Ensures that each
   * coordinate is only shot at once by checking for duplicates.
   *
   * <p>This set is initialized once and remains constant in reference (final), but its contents can
   * be modified to add new shot coordinates.
   */
  private final Set<Coordinates> alreadyShotAt;

  private final Random rand;

  public MediumAlgorithm() {
    this.potentialTargets = new PriorityQueue<>();
    this.alreadyShotAt = new HashSet<>();
    this.rand = new Random();
  }

  /**
   * Executes a shot on the game board, either randomly or targeting potential adjacent cells,
   * avoiding previously shot coordinates. Updates the list of potential targets based on hit
   * results.
   *
   * @param game The current game instance containing the player's board.
   */
  @Override
  public void takeAShot(Game game, Pane root) {
    Coordinates guessedCoordinates;
    do {
      if (potentialTargets.isEmpty()) {
        guessedCoordinates = BoardUtils.randomCoordinatesOnField(game.player.board.getSize(), rand);
      } else {
        guessedCoordinates = potentialTargets.poll();
      }
    } while (alreadyShotAt.contains(guessedCoordinates));

    // fire on the board
    ShotAttemptResult attemptResult = game.shotShot(guessedCoordinates, root);
    alreadyShotAt.add(guessedCoordinates);

    if (attemptResult == ShotAttemptResult.HIT) {
      potentialTargets.addAll(
          getNextPotentialTargets(game.player.board.getSize(), guessedCoordinates));

      if (game.player.board.checkIfShipWasSunk()) {
        logger.info("Sunk ship at {} by the opponent (computer)", guessedCoordinates);
      }
    }

    // if we hit a ship -> keep firing
    if (attemptResult != ShotAttemptResult.MISS) {
      takeAShot(game, root);
    }
    logger.info("Finished firing!");
  }

  /**
   * Generates a list of adjacent target coordinates around a given coordinate, filtering out those
   * that are outside the game board boundaries or already shot at.
   *
   * @param boardSize The size of the game board (assumed square).
   * @param coordinates The current coordinate around which to find potential targets.
   * @return An ArrayList of Coordinates representing valid adjacent targets.
   */
  private ArrayList<Coordinates> getNextPotentialTargets(int boardSize, Coordinates coordinates) {
    ArrayList<Coordinates> output = new ArrayList<>();

    // Calculate the four potential neighboring positions
    Coordinates[] potentialTargets = {
      new Coordinates(coordinates.x + 1, coordinates.y), // Right
      new Coordinates(coordinates.x - 1, coordinates.y), // Left
      new Coordinates(coordinates.x, coordinates.y + 1), // Down
      new Coordinates(coordinates.x, coordinates.y - 1), // Up
    };

    // Check each potential target for validity
    for (Coordinates potentialTarget : potentialTargets) {
      // Skip if the target is outside the board boundaries
      if (potentialTarget.y < 0
          || potentialTarget.y >= boardSize
          || potentialTarget.x < 0
          || potentialTarget.x >= boardSize) {
        continue;
      }
      // Skip if the target has already been shot at
      if (alreadyShotAt.contains(potentialTarget)) {
        continue;
      }
      // Add valid target to the output list
      output.add(potentialTarget);
    }

    return output;
  }
}
