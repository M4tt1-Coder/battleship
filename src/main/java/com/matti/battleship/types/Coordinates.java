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
  public boolean isNeighbourStraight(Coordinates other) {
    // the same
    if (this.x == other.x && this.y == other.y) return false;

    return this.x == other.x && this.y + 1 == other.y
        || this.x == other.x && this.y - 1 == other.y
        || this.x - 1 == other.x && this.y == other.y
        || this.x + 1 == other.x && this.y == other.y;
  }

  /**
   * Checks whether the specified coordinates are diagonally adjacent to the current coordinates.
   * <p>
   * The method returns {@code true} if the other coordinates are diagonally neighboring,
   * or if they are directly neighboring in a straight line (horizontal or vertical),
   * as determined by the {@link #isNeighbourStraight} method.
   * </p>
   *
   * @param other The coordinates to compare with the current coordinates.
   * @return {@code true} if the other coordinates are diagonally or straight neighboring; {@code false} otherwise.
   */
  public boolean isNeighbourDiagonal(Coordinates other) {
    if (isNeighbourStraight(other)) return true;

    return this.x + 1 == other.x && this.y + 1 == other.y
        || this.x + 1 == other.x && this.y - 1 == other.y
        || this.x - 1 == other.x && this.y + 1 == other.y
        || this.x - 1 == other.x && this.y - 1 == other.y;
  }

  /**
   * Returns a string representation of the object, formatted as "x|y".
   *
   * @return a string in the format "x|y"
   */
  @Override
  public String toString() {
    return x + "|" + y;
  }
}
