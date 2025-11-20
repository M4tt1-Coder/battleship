package com.matti.battleship.types;

import kotlin.random.XorWowRandom;

import java.util.ArrayList;
import java.util.UUID;

public class Board { 
    int size;
    public int number_of_ships;
    public Field[][] board;
    public Board(int size) {
        this.size = size;
        this.number_of_ships = 0;
        // creates an empty field with no content
        this.board =  new Field[size][size];
    }

    /**
     * Provides the size of a 'Board' instance.
     *
     * @author m4tt1
     * @return The size of a board
     */
    public int get_size() {
        return size;
    }
    
    // add all ships to the playing board
    // remove a ship from the board
    
    // TODO - Finish the 'addShips' method
    /**
     * 
     */
    public void addShips(Ship[] ships) {
        // for each ship check if its occupied fields are free
        // log if a ship couldn't be placed on the field
    }
    
    /**
     * Since the 'Size' property of can only be set during initialization it isn't mutated.
     * The method clears the 'board' and sets the 'number_of_ships' to null.
     * 
     * @author m4tt1
     */
    public void reset() {
        number_of_ships = 0;
        board =  new Field[size][size];
    }

    /**
     * Iterates over all fields and looks if a field is occupied or not.
     * <p> 
     * Then adds an occupied field to the output list.
     * @return List of occupied fields
     * @author m4tt1
     */
    public ArrayList<Coordinates> getCoordinatesOfOccupiedFields() {
        ArrayList<Coordinates> output_coordinates = new ArrayList<Coordinates>();
        
        for (Field[] row : this.board) {
            for (Field field : row) {
                if (field.isOccupied) output_coordinates.add(field.getCoordinates());
            }
        }
        
        return output_coordinates;
    }

    Ship getShipOnBoardByID(UUID id) {
        for (Field[] row : this.board) {
            for (Field field : row) {
                if 
            }
        }
        return null;
    }
    
    /**
     * 
     * @return 
     * @author m4tt1
     */
    public Coordinates[] getCoordinatesOfShip() {
        
    }
    
    public Coordinates[] getShipCoordinates() {
        return new Coordinates[1];
    }
}
