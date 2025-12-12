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
    public boolean isOccupied;
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

    @Nullable
    public Ship getShip() {
        return this.ship;
    }
    
    public void setShip(@Nullable Ship ship) {
        this.ship = ship;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
