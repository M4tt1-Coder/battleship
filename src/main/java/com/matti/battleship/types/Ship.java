package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;

import java.util.UUID;

/**
 * 
 */
public class Ship {
    /**
     * 
     */
    public UUID id;

    /**
     * 
     */
    public Coordinates start;

    /**
     * 
     */
    public Direction direction;

    /**
     * 
     */
    public UUID PlayerID;

    /**
     * 
     */
    public boolean sunk;

    /**
     * 
     */
    public ShipLength length;
    
    public Ship(Coordinates start, Direction direction, ShipLength length) {
        this.start = start;
        this.direction = direction;
        this.length = length;
        this.id = UUID.randomUUID();
    }
}
