package com.matti.battleship.utils;

import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;

/**
 * @author m4tt1
 */
public class BoardUtils {
    public boolean AreFieldsOfShipAlreadyOccupied(Board board, Ship ship) {
        // first get all occupied fields of the board
        var occupiedFields = board.getCoordinatesOfOccupiedFields();
        
        // get supposed occupied fields by the ship 
        
        // validate if the ship can be placed in that field
        for (Coordinates occupiedField : occupiedFields) {
            if (occupiedField.equals(ship.)) {}
        }
    }
}
