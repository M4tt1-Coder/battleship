package com.matti.battleship.utils.datatypes;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.utils.BoardUtils;

/**
 * Represents an element of a ship on the game grid. It contains the starting coordinates,
 * orientation, and length of the ship.
 */
public class ShipGridElement {

  /** The starting coordinates of the ship on the grid. */
  private Coordinates coordinates;

  /** The direction/orientation of the ship (e.g., horizontal or vertical). */
  private Direction direction;

  /** The length of the ship. */
  private ShipLength length;

  /**
   * Constructs a ShipGridElement with specified coordinates, direction, length, and board size.
   * Validates that the provided coordinates are within the bounds of the board.
   *
   * @param coordinates the starting coordinates of the ship
   * @param direction the orientation of the ship
   * @param length the length of the ship
   * @param boardSize the size of the game board (used for coordinate validation)
   * @throws IllegalArgumentException if the coordinates are outside the board bounds
   */
  public ShipGridElement(
      Coordinates coordinates, Direction direction, ShipLength length, int boardSize) {
    if (!BoardUtils.isCoordinateOnBoard(coordinates, boardSize)) {
      throw new IllegalArgumentException("Coordinates are out of board bounds.");
    }
    this.coordinates = coordinates;
    this.direction = direction;
    this.length = length;
  }

  /**
   * Gets the starting coordinates of the ship.
   *
   * @return the coordinates
   */
  public Coordinates getCoordinates() {
    return this.coordinates;
  }

  /**
   * Sets the starting coordinates of the ship.
   *
   * @param coordinates the new coordinates to set
   */
  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  /**
   * Gets the direction/orientation of the ship.
   *
   * @return the direction
   */
  public Direction getDirection() {
    return this.direction;
  }

  /**
   * Sets the direction/orientation of the ship.
   *
   * @param direction the new direction to set
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * Gets the length of the ship.
   *
   * @return the length
   */
  public ShipLength getLength() {
    return this.length;
  }

  /**
   * Sets the length of the ship.
   *
   * @param length the new length to set
   */
  public void setLength(ShipLength length) {
    this.length = length;
  }
}
