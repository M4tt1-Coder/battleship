package com.matti.battleship.computer;

import com.matti.battleship.types.Game;
import javafx.scene.layout.Pane;

/** Interface representing a shooting algorithm for selecting target coordinates in the game. */
public interface Algorithm {

  /**
   * Determines the next shot coordinate based on the current game state.
   *
   * @param game The current state of the game, including information about previous shots and game
   *     board.
   */
  public void takeAShot(Game game, Pane root);

  /**
   * Performs any necessary initialization or setup tasks after loading the game state from a file.
   *
   * <p>This method is called after the game data has been deserialized, allowing the game to
   * re-establish transient states, initialize resources, or perform any other post-loading
   * preparations required for the game to function correctly.
   *
   * @param game the {@link Game} instance that has been loaded from the file, which may require
   *     additional setup.
   */
  public void prepareAfterLoadingFromFile(Game game);
}
