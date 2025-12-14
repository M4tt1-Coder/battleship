package com.matti.battleship.enums;

/**
 * To not have to use an integer for the ship length which can be an invalid value,
 * we use this enum which represents the mentioned length of a ship.
 * 
 * @author m4tt1
 */
public enum ShipLength {
    Two(2),
    Three (3),
    Four(4),
    Five(5);
    
    private final int length;
    
    ShipLength(int length) {
        this.length = length;
    }
    public int getValue() {
        return length;
    }
}
