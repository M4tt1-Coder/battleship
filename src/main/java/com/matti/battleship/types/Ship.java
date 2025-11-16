package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;

public class Ship {
    public Coordinates start;
    public Direction direction;
    public boolean sunk;
    public int length;
    public Ship(Coordinates start, Direction direction) {}
}
