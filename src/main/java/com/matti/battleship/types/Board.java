package com.matti.battleship.types;

import com.matti.battleship.utils.BoardUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.UUID;

public class Board {
    private static final Logger logger = LogManager.getLogger(Board.class);
    
    // maximum 15
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
    public int getSize() {
        return size;
    }

    /**
     * Receives a list of ships to be placed on the board. Then validates the single ships and the whole process.
     * Used for playing against the 
     * 
     * @param ships Array of 'Ship's selected by the user / generator to be placed on the board of the 'Game'
     * @return TRUE, if the ships were added to the board.
     */
    public boolean addShips(Ship[] ships) {
        // for each ship check if its occupied fields are free
        for  (Ship ship : ships) {
            var can_not_be_placed = BoardUtils.AreFieldsOfShipAlreadyOccupied(this, ship);
            if(can_not_be_placed) {
                // log if a ship couldn't be placed on the field
                logger.error("Ship {} can't be placed on the board!", ship.toString());
                return false;
            }
            this.number_of_ships++;
            Field field = this.getFieldOnBoardByCoordinates(ship.start);
            if (field != null) {
                field.setShip(ship);
            } else {
                logger.error("Field at the coordinates {} could not be found and the ship couldn't be placed in consequence!", ship.start);
                return false;
            }
        }
        
        logger.debug("Added ships on the board!");
        return true;
    }
    
    /**
     * Since the 'Size' property of can only be set during initialization it isn't mutated.
     * The method clears the 'board' and sets the 'number_of_ships' to null.
     * 
     * @author m4tt1
     */
    public void reset() {
        number_of_ships = 0;
        board = new Field[size][size];
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

    /**
     * Searches on the board for a 'Ship' with the provided ID.
     * 
     * @param id The identifier of the 'Ship'
     * @return A 'Ship' with the provided id or else NULL; 
     */
    Ship getShipOnBoardByID(UUID id) {
        for (Field[] row : this.board) {
            for (Field field : row) {
                var ship = field.getShip();
                if (ship != null && ship.id.equals(id)) {
                    return ship;
                }
            }
        }
        return null;
    }

    /**
     * Gets the field which is on the coordinates provided.
     * 
     * @param coordinates Coordinates of the field
     * @return The 'Field' object if the coordinates are on the board
     */
    private Field getFieldOnBoardByCoordinates(Coordinates coordinates) {
        for (Field[] row : this.board) {
            for (Field field : row) {
               if (field.getCoordinates().equals(coordinates)) {
                   return field;
               } 
            }
        }
        logger.debug("Could not find field on the board with the coordinates {}!", coordinates);
        return null;
    }
}
