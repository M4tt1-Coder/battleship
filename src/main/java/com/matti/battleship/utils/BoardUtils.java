package com.matti.battleship.utils;

import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;

/**
 * Holds the helper functions to execute checks like if a some fields are already occupied etc. 
 * 
 * @author m4tt1
 */
public class BoardUtils {
    /**
     * Determines if ship can be placed on to the board.
     * 
     * @param board Board of the game
     * @param ship Ship which the user / generator wants to place on to the field.
     * @return TRUE, if the ship can't be placed on the board.
     */
    public static boolean AreFieldsOfShipAlreadyOccupied(Board board, Ship ship) {
        // first get all occupied fields of the board
        var occupiedFields = board.getCoordinatesOfOccupiedFields();
        
        // get supposed occupied fields by the ship 
        var intended_fields = ShipUtils.getFieldsOfShip(board, ship);
        
        // validate if the ship can be placed in that field
        for (Coordinates occupiedField : occupiedFields) {
            for (Coordinates intendedField : intended_fields) {
                if (occupiedField.x == intendedField.x && occupiedField.y == intendedField.y) {
                    return true;
                }
            }
        }
        return false;
    }
}
