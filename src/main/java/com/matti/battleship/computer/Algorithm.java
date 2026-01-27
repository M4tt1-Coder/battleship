package com.matti.battleship.computer;

import com.matti.battleship.types.Game;

/** Interface representing a shooting algorithm for selecting target coordinates in the game. */
public interface Algorithm {

  /**
   * Determines the next shot coordinate based on the current game state.
   *
   * @param game The current state of the game, including information about previous shots and game
   *     board.
   */
  public void takeAShot(Game game);
}
