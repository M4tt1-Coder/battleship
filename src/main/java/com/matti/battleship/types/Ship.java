package com.matti.battleship.types;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.utils.BoardUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

/**
 * @author m4tt1 
 */
public class Ship {
    private static final Logger logger = LogManager.getLogger(Ship.class);
    
    /**
     * Unique identifier of a ship.
     * UUID version 4 ... 
     */
    UUID id;

    /**
     * Coordinates from where the ship can be set and 
     */
    Coordinates start;

    /**
     * Direction the ship is turned to relatively to its starting coordinates.
     */
    Direction direction;

    /**
     * Unique identifier of the player the ship belongs to.
     */
    UUID PlayerID;

    /**
     * Specifies if the ship has been sunk or not.
     */
    boolean has_sunk;

    /**
     * How long a ship can be! Either 2, 3, 4 or 5.
     */
    ShipLength length;
    
    public Ship(Coordinates start, Direction direction, ShipLength length, UUID PlayerID) {
        this.start = start;
        this.direction = direction;
        this.length = length;
        this.id = UUID.randomUUID();
        this.has_sunk = false;
        this.PlayerID = PlayerID;
    }
    
    // ----- Methods -----
    
    /**
     * 
     * @return
     */
    public UUID getId() {
        return id;
    }

    /**
     * 
     * @return
     */
    public Coordinates getStartCoordinates() {
        return start;
    }

    /**
     * Updates the starting coordinates of a ship. For that the passed data needs to be valid and the coordinates new.
     * 
     * @param start New / updated coordinates for a ship
     * @param boardSize Size of the board
     * @return TRUE, if the 'start' coordinates have been mutated.
     */
    public boolean setStart(Coordinates start, int boardSize) {
        if (BoardUtils.isCoordinateOnBoard(start, boardSize)) {
            return false;
        }
        if (this.start != start) {
            this.start = start;
        } else {
            logger.info("Please alter different coordinates not the SAME!");
        }
        return true;
    }
    
    /**
     * 
     * @return
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 
     * @param direction
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    /**
     * 
     * @return
     */
    public ShipLength getLength() {
        return length;
    }

    /**
     * 
     * @return
     */
    public UUID getPlayerID() {
        return PlayerID;
    }

    /**
     * 
     * @return
     */
    public boolean getHasSunk() {
        return has_sunk;
    }

    /**
     * 
     */
    public void alterHasSunk() {
        this.has_sunk = !this.has_sunk;
    }
}
