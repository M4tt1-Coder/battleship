package com.matti.battleship.utils;

import com.matti.battleship.types.Coordinates;

/**
 * Provides simple helper functions when working with 'Coordinates';
 *
 * @author m4tt1
 */
public class CoordinatesUtils {
  /**
   * Checks whether the specified {@link Coordinates} are present in the given array of {@link
   * Coordinates}.
   *
   * @param coordinates the {@link Coordinates} to search for.
   * @param arr the array of {@link Coordinates} to search within.
   * @return {@code true} if the coordinates are found in the array; {@code false} otherwise.
   */
  public static boolean areCoordinatesInArray(Coordinates coordinates, Coordinates[] arr) {
    for (Coordinates c : arr) {
      if (c.x == coordinates.x && c.y == coordinates.y) return true;
    }
    return false;
  }
}
