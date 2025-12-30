package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.PlayerTurn;
import com.matti.battleship.enums.PlayingMode;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.ShipUtils;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: Add functionality to check for win conditions -> add method to check if all ships of a
// player have been sunk + winner property

// TODO: Need a ship registry NOT of every ship where it is etc BUT which ships should be placed on
// the field -> validation function

/**
 * Represents a Battleship game instance. Contains information about the playing mode, players, and
 * game status.
 *
 * @author m4tt1
 */
public class Game {
  private static final Logger logger = LogManager.getLogger(Game.class);

  /** TRUE, when the game has ended; FALSE otherwise. */
  private boolean hasEnded = false;

  /** Playing mode of the game. */
  private final PlayingMode playingMode;

  /** Player instance representing the local player. */
  public Player player;

  /**
   * Opponent player instance. Either another human player or an AI, depending on the playing mode.
   */
  public Player opponent;

  /** Indicates whose turn it is in the game. */
  private PlayerTurn whoseTurn;

  public Game(PlayingMode playingMode, Player player, Player opponent, PlayerTurn turn) {
    this.playingMode = playingMode;
    this.player = player;
    this.opponent = opponent;
    this.hasEnded = false;
    this.whoseTurn = turn; // Default starting turn
  }

  // ----- Methods -----

  /**
   * Retrieves whose turn it is in the game.
   *
   * @return PlayerTurn indicating whose turn it is.
   */
  public PlayerTurn getWhoseTurn() {
    return whoseTurn;
  }

  /** Switches the turn to the other player. */
  public void switchTurn() {
    if (whoseTurn == PlayerTurn.PLAYER) {
      whoseTurn = PlayerTurn.OPPONENT;
    } else {
      whoseTurn = PlayerTurn.PLAYER;
    }
  }

  /**
   * Retrieves the playing mode of the game.
   *
   * @return Playing mode of the game.
   */
  public PlayingMode getPlayingMode() {
    return playingMode;
  }

  /**
   * Checks if the game has ended.
   *
   * @return TRUE, when the game has ended; FALSE otherwise.
   */
  public boolean hasEnded() {
    return hasEnded;
  }

  /**
   * Sets the game as ended or not.
   *
   * @param hasEnded TRUE, when the game has ended; FALSE otherwise.
   */
  public void setHasEnded(boolean hasEnded) {
    this.hasEnded = hasEnded;
  }

  /**
   * Applies the opponent's response result to the player's shot, updating the game state
   * accordingly. This method should only be called during the player's turn in a multiplayer (PvP)
   * game mode.
   *
   * <p>Depending on the {@link ShotAttemptResult}:
   *
   * <ul>
   *   <li>{@code HIT}: Marks the targeted field as shot and occupied.
   *   <li>{@code MISS}: Marks the targeted field as shot.
   *   <li>{@code SUNK}: Marks the field as shot and occupied, determines the sunk ship's position,
   *       creates the ship instance, updates the opponent's board, and validates the sinking.
   * </ul>
   *
   * @param result the result of the opponent's shot attempt (HIT, MISS, or SUNK)
   * @param guessed the coordinates of the opponent's shot
   * @throws IllegalStateException if it's not the player's turn or if the game mode is not PvP
   * @throws IllegalArgumentException if {@code result} or {@code guessed} are null, or if the
   *     result is invalid
   * @throws IllegalStateException if the sunken ship's fields do not match the expected occupied
   *     fields, or if sinking validation fails
   */
  public void applyOpponentsResponseToPlayersShot(ShotAttemptResult result, Coordinates guessed) {
    if (guessed == null || result == null) {
      throw new IllegalArgumentException("Guessed coordinates and result must not be null");
    }

    if (whoseTurn != PlayerTurn.PLAYER) {
      throw new IllegalStateException("It's not the player's turn to shoot.");
    }

    if (playingMode == PlayingMode.VS_AI) {
      throw new IllegalStateException(
          "This method is only for handling opponent responses in PvP mode.");
    }

    // don't need to apply any checks here
    Field field = opponent.board.getFieldOnBoardByCoordinates(guessed);
    switch (result) {
      case HIT -> {
        field.markAsShotAt();
        field.setOccupied(true);
      }
      case MISS -> field.markAsShotAt();
      case SUNK -> {
        field.markAsShotAt();
        field.setOccupied(true);
        // set one of the fields as 'starting point' to set the ship instance -> first in the list
        Coordinates[] occupiedFields =
            BoardUtils.getAllCurrentOccupiedFieldsOfSunkenShip(this.opponent.board);
        // make sure the field that was shot at is in the list
        if (Arrays.stream(occupiedFields).noneMatch(x -> x.equals(guessed))) {
          logger.error(
              "The provided coordinates for the last field of ship the previous field are not in the list of fields of the previously sunken ship!");
          throw new IllegalStateException(
              "Coordinates 'guess' need to be in the list of fields of the sunken ship!");
        }
        // determine in which direction the ship is pointing to
        Direction shipDirection = ShipUtils.determineShipDirection(occupiedFields);
        // add ship to the field
        field.setShip(
            new Ship(
                occupiedFields[0],
                shipDirection,
                ShipUtils.shipLengthFromInt(occupiedFields.length),
                this.opponent.getID()));
        this.opponent.board.number_of_ships++;
        if (!this.opponent.board.checkIfShipWasSunk()) {
          logger.error("No ship was marked sunk after one should have been!");
          throw new IllegalStateException(
              "Something happened while marking the recently sunken ship as sunken!");
        }
      }
      default -> throw new IllegalArgumentException("Invalid shot attempt result: " + result);
    }
  }

  /**
   * Processes a shot attempt at the given coordinates. When a shot is made, it checks if it was a
   * hit or miss and updates the boards accordingly. If a ship is sunk as a result of the shot, it
   * processes that as well.
   *
   * <p>Note: This method assumes that the player making the shot is the local player, and the
   * opponent's board is being targeted locally (the information about where the opponents ships are
   * placed needs to be known -> included into opponents board). In case the opponent is another
   * real player on a different device, the actual shot processing is handled differently via
   * network communication.
   *
   * @param guessed Coordinates where the shot is attempted.
   * @return Result of the shot attempt.
   */
  public ShotAttemptResult shotShot(Coordinates guessed) {
    // check if a ship has been sunk after the shot
    // in case two players on different devices are playing, you don't know about where the
    // opponents ships are placed -> only when playing against AI
    if (whoseTurn == PlayerTurn.PLAYER && playingMode == PlayingMode.VS_AI) { // Player's turn
      ShotAttemptResult shotResult = opponent.board.shotAtField(guessed);
      // check if hit or miss
      if (shotResult == ShotAttemptResult.HIT) {
        // update boards accordingly
        if (opponent.board.checkIfShipWasSunk()) {
          shotResult = ShotAttemptResult.SUNK;
        }
      }
      return shotResult;
    } else if (whoseTurn == PlayerTurn.OPPONENT) { // Opponent's turn
      // try to shoot at opponent's board
      ShotAttemptResult shotResult = player.board.shotAtField(guessed);
      // check if hit or miss
      if (shotResult == ShotAttemptResult.HIT) {
        // update boards accordingly
        // process sunk ships
        if (player.board.checkIfShipWasSunk()) {
          shotResult = ShotAttemptResult.SUNK;
        }
      }
      return shotResult;
    } else {
      throw new IllegalStateException("It's not the turn of the local player attempting to shoot.");
    }
  }
}
