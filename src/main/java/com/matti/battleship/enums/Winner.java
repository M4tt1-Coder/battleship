package com.matti.battleship.enums;

/**
 * Represents the winner state of a Battleship game.
 *
 * <p>Values:
 *
 * <ul>
 *   <li><b>OPPONENT</b> — The opponent has won the game.
 *   <li><b>PLAYER</b> — The player has won the game.
 *   <li><b>NONE_YET</b> — No winner yet; the game is still in progress or undecided.
 * </ul>
 */
public enum Winner {
  /** The opponent has won the game. */
  OPPONENT,

  /** The player has won the game. */
  PLAYER,

  /** No winner yet; the game is still in progress or undecided. */
  NONE_YET
}
