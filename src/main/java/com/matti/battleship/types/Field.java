package com.matti.battleship.types;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a single field on the field.
 * A 'Field' can be identified with its coordinates.
 * When a 'Ship' is placed on a 'Field' 'isOccupied' will be set to TRUE (default FALSE). 
 * 
 * @author m4tt1 
 * @since OPENDxK 17.0.17
 */
public class Field {
    /**
     * TRUE, when a ship is situated on the 'Field'.
     */
    private boolean isOccupied;
    
    /**
     * Coordinates of the 'Field';
     */
    private Coordinates coordinates;

    /**
     * Optional ship on the field where the ships origin field is situated.
     */
    @Nullable
    private Ship ship;
    
    public Field(Coordinates coordinates) {
        isOccupied = false;
        this.coordinates = coordinates;
    }

    /**
     * Gets the ship which is placed on the field.
     * 
     * @return Ship which is placed on the field or NULL, when no ship is placed on the field.
     */
    @Nullable
    public Ship getShip() {
        return this.ship;
    }

    /**
     * Sets the ship on the field.
     * 
     * @param ship Ship to be placed on the field or NULL, when no ship is placed on the field.
     */
    public void setShip(@Nullable Ship ship) {
        this.ship = ship;
        this.isOccupied = (ship != null);
    }

    /**
     * Gets the coordinates of the field.
     * 
     * @return Coordinates of the field.
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the coordinates of the field.
     * 
     * @param coordinates Coordinates to be set on the field.
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    
    /**
     * Checks if the field is occupied by a ship.
     * 
     * @return TRUE, when the field is occupied by a ship.
     */
    public boolean isOccupied() {
        return isOccupied;
    }
     
    /**
     * Sets the occupation status of the field.
     * 
     * @param isOccupied TRUE, when the field is occupied by a ship.
     */
    public void setOccupied(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }
}
