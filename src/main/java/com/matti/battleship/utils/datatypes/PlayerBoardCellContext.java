package com.matti.battleship.utils.datatypes;

/**
 * Represents the context or state of a single cell on the player's game board.
 *
 * <p>This class encapsulates the current display state of a cell, which can be one of:
 *
 * <ul>
 *   <li>{@link DisplayState#MISS} - The cell was targeted and resulted in a miss.
 *   <li>{@link DisplayState#SUNK} - A ship occupying this cell has been sunk.
 *   <li>{@link DisplayState#HIT} - The cell was targeted and resulted in a hit on a ship.
 *   <li>{@link DisplayState#NOT_SET} - The cell has not been targeted or its state is not yet
 *       determined.
 * </ul>
 *
 * The {@code state} field indicates the current display status of the cell, initialized to {@code
 * NOT_SET}.
 */
public class PlayerBoardCellContext {
  /** Enum representing possible display states of a cell on the player's board. */
  public static enum FieldDisplayState {
    MISS,
    SUNK,
    HIT,
    NOT_SET
  }

  /** Current display state of the cell. */
  public FieldDisplayState state = FieldDisplayState.NOT_SET;
}
