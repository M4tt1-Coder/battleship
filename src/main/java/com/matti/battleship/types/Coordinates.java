package com.matti.battleship.types;

/**
 * Coordinates on the board. Represents an (x,y) pair.
 *
 * @author m4tt1
 */
public class Coordinates {
  /** X ordinate */
  public int x;

  /** Y ordinate */
  public int y;

  public Coordinates(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // ----- Methods -----

  /**
   * Checks if the given coordinates are neighboring cells.
   *
   * @param other The Coordinates object to compare with.
   * @return {@code true} if {@code other} is directly adjacent (above, below, left, or right) to
   *     this coordinate; {@code false} if they are the same or not neighboring.
   */
  public boolean isNeighbour(Coordinates other) {
    // the same
    if (this.x == other.x && this.y == other.y) return false;

    return this.x == other.x && this.y + 1 == other.y
        || this.x == other.x && this.y - 1 == other.y
        || this.x - 1 == other.x && this.y == other.y
        || this.x + 1 == other.x && this.y == other.y;
  }
}
