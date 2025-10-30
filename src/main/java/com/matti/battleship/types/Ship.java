package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;

import javax.swing.text.LabelView;

public class Ship {
    public Coordinate start;
    public Direction direction;
    public boolean sunk;
    public int length;
    public Ship(Coordinate start, Direction direction) {}
}
