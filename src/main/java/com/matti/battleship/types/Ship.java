package com.matti.battleship.types;

import com.matti.battleship.IO.ResourceProfiler;
import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.utils.BoardUtils;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A ship which can be placed on to the board. Depending on its length it occupies a certain number
 * of fields. There is a 'starting point' (coordinates of a field) and a direction.
 *
 * @author m4tt1
 */
public class Ship {
  private static final Logger logger = LogManager.getLogger(Ship.class);

  /** Unique identifier of a ship. UUID version 4 ... */
  private final UUID id;

  /** Coordinates from where the ship can be set and */
  private Coordinates start;

  /** Direction the ship is turned to relatively to its starting coordinates. */
  private Direction direction;

  /** Specifies if the ship has been sunk or not. */
  private boolean hasSunk;

  /** How long a ship can be! Either 2, 3, 4 or 5. */
  private final ShipLength length;

  /** The file path to the picture or image associated with this object. */
  private final String picPath;

  public Ship(Coordinates start, Direction direction, ShipLength length) {
    this.start = start;
    this.direction = direction;
    this.length = length;
    this.id = UUID.randomUUID();
    this.hasSunk = false;

    ResourceProfiler profiler = new ResourceProfiler();

    this.picPath = profiler.getPictureOfShip(length);
  }

  // ----- Methods -----

  /**
   * Returns the file path to the picture corresponding with the ship and its length.
   *
   * @return Picture path as a STRING
   */
  public String getPicPath() {
    return this.picPath;
  }

  /**
   * Retrieves the unique identifier of a ship.
   *
   * @return UUID of the ship
   */
  public UUID getId() {
    return id;
  }

  /**
   * Provides the starting coordinates of a ship.
   *
   * @return Coordinates of the 'starting point' of a ship
   */
  public Coordinates getStartCoordinates() {
    return start;
  }

  /**
   * Updates the starting coordinates of a ship. For that the passed data needs to be valid and the
   * coordinates new.
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
   * Gets the direction of the ship.
   *
   * @return Direction of the ship.
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Sets the direction of the ship.
   *
   * @param direction New direction of the ship.
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * Gets the length of the ship.
   *
   * @return Length of the ship.
   */
  public ShipLength getLength() {
    return length;
  }

  /**
   * Retrieves if the ship has sunk.
   *
   * @return TRUE, if the ship has sunk.
   */
  public boolean getHasSunk() {
    return hasSunk;
  }

  /** Alters the 'has_sunk' property of the ship. */
  public void alterHasSunk() {
    this.hasSunk = !this.hasSunk;
  }

  /**
   * Returns a string representation of the object, detailing its properties.
   *
   * @return a formatted string including id, start position, direction, sunk status, and length
   */
  @Override
  public String toString() {
    return "("
        + "id="
        + id
        + ","
        + "start="
        + start.toString()
        + ","
        + "direction="
        + direction
        + ","
        + "hasSunk="
        + hasSunk
        + ","
        + "length="
        + length
        + ")";
  }
}
