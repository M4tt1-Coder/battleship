package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;

import java.util.UUID;

/**
 * 
 */
public class Ship {
    public UUID id;
    public Coordinates start;
    public Direction direction;
    public UUID PlayerID;
    public boolean sunk;
    public int length;
    public Ship(Coordinates start, Direction direction) {
        this.start = start;
        this.direction = direction;
        this.id = UUID.randomUUID();
    }
}
