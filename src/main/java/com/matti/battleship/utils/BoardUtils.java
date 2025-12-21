package com.matti.battleship.utils;

import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Ship;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the helper functions to execute checks like if a some fields are already occupied etc. 
 * 
 * @author m4tt1
 */
public class BoardUtils {
    private static final Logger logger = LogManager.getLogger(BoardUtils.class);
    
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

    /**
     * Validates the provided data and determines if some coordinates are on the board according to its size.
     * 
     * @param coordinates Coordinates of a ship or any field
     * @param boardSize Size of the board
     * @return TRUE, when the coordinates are on the field.
     */
    public static boolean isCoordinateOnBoard(Coordinates coordinates, int boardSize) {
        if (boardSize < 1) {
            logger.error("Board size must be greater than zero");
            return false;
        }
        // when the X or Y ordinate are negative
        if (coordinates.x < 0 || coordinates.y < 0) {
            logger.error("Coordinate coordinates can not be negative numbers! X: {} , Y: {}", coordinates.x, coordinates.y);
            return false;
        }
        
        // check that the coordinates are on the board
        return coordinates.x <= boardSize && coordinates.y <= boardSize;
    } 
}
