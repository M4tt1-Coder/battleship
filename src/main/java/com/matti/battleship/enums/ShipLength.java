package com.matti.battleship.enums;

/**
 * To not have to use an integer for the ship length which can be an invalid value, we use this enum
 * which represents the mentioned length of a ship.
 *
 * @author m4tt1
 */
public enum ShipLength {
  Two(2),
  Three(3),
  Four(4),
  Five(5);

  private final int length;

  ShipLength(int length) {
    this.length = length;
  }

  /**
   * Gets tha value of the enum value as an integer between 2 to 5!
   *
   * @return Integer value of the enum value.
   */
  public int getValue() {
    return length;
  }
}
