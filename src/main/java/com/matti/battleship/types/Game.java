package com.matti.battleship.types;

import com.matti.battleship.enums.PlayingMode;

// TODO: Add a timer of some sort to limit the duration of a player's turn?
// TODO: Add 'shot' functionality -> player shoots at opponent's board -> check if hit (can shot
// again) or miss -> update boards accordingly
// TODO: Add functionality to check for win conditions -> add method to check if all ships of a
// player have been sunk + winner property

/**
 * Represents a Battleship game instance. Contains information about the playing mode, players, and
 * game status.
 *
 * @author m4tt1
 */
public class Game {
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

  public Game(PlayingMode playingMode, Player player, Player opponent) {
    this.playingMode = playingMode;
    this.player = player;
    this.opponent = opponent;
    this.hasEnded = false;
  }

  // ----- Methods -----

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
}
