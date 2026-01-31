package com.matti.battleship.utils;

import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Field;
import com.matti.battleship.types.Ship;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contains the helper functions for working with the ships.
 *
 * @author m4tt1
 */
public class ShipUtils {
  private static final Logger logger = LogManager.getLogger(ShipUtils.class);

  /**
   * Retrieves the fields of a 'Ship' where it is placed on. Executes main check that the fields are
   * on the board.
   *
   * @param board Board of the 'Game' with the 'Ship's on it
   * @param ship 'Ship' who's fields need to be calculated
   * @return Array of 'Coordinates' of the fields on which the ship is placed on.
   */
  public static Coordinates[] getFieldsOfShip(Board board, Ship ship) {
    // generate the field coordinates of the wanted fields
    Coordinates[] fields =
        calcFieldsOfShip(
            ship.getStartCoordinates(), ship.getDirection(), ship.getLength().getValue());

    // validate fields against the game logic
    for (var c : fields) {
      // on field?
      if (c.x < 0 || c.x > board.getSize() - 1 || c.y < 0 || c.y > board.getSize() - 1) {
        logger.error(
            "Ship coordinates: {} not on the board! Can't determine the fields of a ship!",
            c.toString());
        return new Coordinates[] {};
      }
    }
    return fields;
  }

  /**
   * Retrieves the coordinates of all fields surrounding the specified {@link Ship} on the given
   * {@link Board}.
   *
   * <p>This method identifies all fields adjacent diagonally to any part of the ship, excluding the
   * ship's own fields. The surrounding fields are returned as an array of {@link Coordinates}.
   *
   * @param board the {@link Board} containing the ship.
   * @param ship the {@link Ship} whose surrounding fields are to be determined.
   * @return an array of {@link Coordinates} representing the fields around the ship.
   */
  public static Coordinates[] getFieldsAroundShip(Board board, Ship ship) {
    ArrayList<Coordinates> output = new ArrayList<>();
    // get fields of ship
    Coordinates[] fieldsOfShip = getFieldsOfShip(board, ship);
    for (Field[] row : board.board) {
      for (Field field : row) {
        for (Coordinates shipFieldCoords : fieldsOfShip) {
          if (shipFieldCoords.isNeighbourDiagonal(field.getCoordinates())
              && !CoordinatesUtils.areCoordinatesInArray(field.getCoordinates(), fieldsOfShip)
              && !output.contains(field.getCoordinates())) {
            output.add(field.getCoordinates());
          }
        }
      }
    }
    return output.toArray(new Coordinates[0]);
  }

  // ------------ private helper functions -------------------

  /**
   * Ignores any checks and simply creates reference instances of 'Coordinates' of the ships
   * coordinates.
   *
   * @param length Length of the ship
   * @param shipCoordinates Coordinates of the ship
   * @param direction Direction in which the ship is pointing to relative to its 'starting point'
   * @return Array of coordinates of fields where the ship claims to be.
   */
  private static Coordinates[] calcFieldsOfShip(
      Coordinates shipCoordinates, Direction direction, int length) {
    Coordinates[] output = new Coordinates[length];
    output[0] = shipCoordinates;
    for (int i = 1; i < length; i++) {
      switch (direction) {
        case UP:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y - i);
          break;
        case DOWN:
          output[i] = new Coordinates(shipCoordinates.x, shipCoordinates.y + i);
          break;
        case LEFT:
          output[i] = new Coordinates(shipCoordinates.x - i, shipCoordinates.y);
          break;
        case RIGHT:
          output[i] = new Coordinates(shipCoordinates.x + i, shipCoordinates.y);
          break;
      }
    }
    logger.debug("Calculated fields of ship: {}", Arrays.toString(output));
    return output;
  }

  /**
   * Validates an integer if it resembles a valid ship length.
   *
   * @param length Ship length to be tested
   * @return TRUE, if the length is valid;
   */
  public static boolean validLength(int length) {
    return length >= 2 && length <= 5;
  }

  /**
   * Converts an integer value to the corresponding ShipLength enum.
   *
   * @param value the integer value representing a ShipLength
   * @return the corresponding ShipLength enum
   * @throws IndexOutOfBoundsException if the value does not correspond to any ShipLength
   */
  public static ShipLength shipLengthFromInt(int value) {
    if (!ShipUtils.validLength(value)) {
      throw new IndexOutOfBoundsException("Invalid value for ShipLength: " + value);
    }
    return ShipLength.values()[value];
  }

  /**
   * Determines the orientation (direction) of a ship based on its field coordinates.
   *
   * <p>Assumes that the input array contains exactly two Coordinates that represent the positions
   * of two adjacent fields of a ship. The method checks the relative positions to determine whether
   * the ship is oriented horizontally (to the right) or vertically (downwards).
   *
   * @param shipFieldCoordinates an array of Coordinates representing two adjacent fields of a ship
   * @return the Direction indicating the orientation of the ship (Direction.RIGHT or
   *     Direction.DOWN)
   * @throws IllegalArgumentException if the input list has an invalid length, contains duplicate
   *     fields, or does not represent a valid ship orientation
   */
  public static Direction determineShipDirection(Coordinates[] shipFieldCoordinates) {
    // make sure there is a valid number of fields in the list
    if (validLength(shipFieldCoordinates.length)) {
      logger.error("An invalid ship length represented in the list of its fields!");
      throw new IllegalArgumentException("Invalid number coordinates in the list!");
    }
    if (shipFieldCoordinates[0].x != shipFieldCoordinates[1].x) {
      return Direction.RIGHT;
    } else if (shipFieldCoordinates[0].y != shipFieldCoordinates[1].y) {
      return Direction.DOWN;
    } else {
      throw new IllegalArgumentException(
          "There can't be a duplicate of a field inside of the list of fields of a ship!");
    }
  }

  /**
   * Rotates the given image by a specified angle in degrees.
   *
   * <p>This method creates an {@link ImageView} node with the input image, applies the rotation,
   * and then snapshots the rotated view into a new {@link Image}. The rotation occurs around the
   * center of the image by default. The resulting image has a transparent background and includes
   * all parts of the rotated image, properly adjusted for size.
   *
   * @param inputImage The original {@link Image} to be rotated.
   * @param angle The rotation angle in degrees. Positive values rotate clockwise, negative values
   *     rotate counter-clockwise.
   * @return A new {@link Image} object representing the input image rotated by the specified angle.
   *     The size of the returned image generally increases to accommodate the entire rotated image.
   * @throws NullPointerException if {@code inputImage} is null.
   */
  public static Image rotateImage(Image inputImage, double angle) {
    // Create a temporary ImageView for rotation
    ImageView iv = new ImageView(inputImage);
    iv.setRotate(angle);

    SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.TRANSPARENT);
    // Snapshot the rotated image
    return iv.snapshot(params, null);
  }
}
