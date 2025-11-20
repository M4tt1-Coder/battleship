package com.matti.battleship.types;

/**
 * Represents a single field on the field.
 * A 'Field' can be identified with its coordinates.
 * When a 'Ship' is placed on a 'Field' 'isOccupied' will be set to TRUE (default FALSE). 
 * 
 * @author m4tt1 
 * @since OPENJDK 17.0.17
 */
public class Field {
    /**
     * TRUE, when a ship is situated on the 'Field'.
     */
    public boolean isOccupied;
    /**
     * Coordinates of the 'Field';
     */
    private Coordinates coordinates;

    /**
     * 
     */
    private Ship[] ships;
    
    public Field(Coordinates coordinates) {
        isOccupied = false;
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
