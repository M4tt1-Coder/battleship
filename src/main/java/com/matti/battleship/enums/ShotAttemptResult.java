package com.matti.battleship.enums;

/**
 * Enum representing the result of a shot attempt in the Battleship game.
 *
 * @author m4tt1
 */
public enum ShotAttemptResult {
  /** Shot was a hit on a ship. */
  HIT,
  /** Shot resulted in sinking a ship. */
  SUNK,
  /** Shot was a miss (water). */
  MISS,
  /** Shot was invalid (e.g., out of bounds or already targeted). */
  INVALID
}
