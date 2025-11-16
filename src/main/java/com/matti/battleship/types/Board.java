package com.matti.battleship.types;

public class Board {
    public int size;
    public int number_of_ships;
    public Field[][] board;
    public Board(int size) {
        this.size = size;
        this.number_of_ships = 0;
        this.board =  new Field[size][size];
    }
}
