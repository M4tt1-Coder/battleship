package com.matti.battleship.IO;

import com.matti.battleship.enums.ShipLength;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@code ResourceProfiler} class provides utilities for managing and verifying the existence of
 * ship image resources used within the application.
 *
 * <p>It offers methods to retrieve the file path of ship images based on their length and ensures
 * that all necessary resources are available at runtime by attempting to load them and logging any
 * missing or inaccessible resources.
 *
 * <p>Usage:
 *
 * <pre>
 * ResourceProfiler profiler = new ResourceProfiler();
 * String shipImagePath = profiler.getPictureOfShip(ShipLength.Four);
 * </pre>
 *
 * <p><b>Note:</b> Resource existence is checked once upon the first request for a ship image.
 * Subsequent calls assume resources are available unless explicitly re-checked.
 */
public class ResourceProfiler {
  private static final Logger logger = LogManager.getLogger(ResourceProfiler.class);

  // _____ Ship Picture Filepaths _____
  // !!Need to be update when the names of the files are changed!!

  /** File path to the image of a destroyer ship with length 2. */
  private static final String Ship2PicPath =
      "/com/matti/battleship/images/ships/destroyer_length2.png";

  /** File path to the image of a submarine with length 3. */
  private static final String Ship3PicPath =
      "/com/matti/battleship/images/ships/submarine_length3.png";

  /** File path to the image of an aircraft carrier with length 4. */
  private static final String Ship4PicPath =
      "/com/matti/battleship/images/ships/aircraft_carrier_length4.png";

  /** File path to the image of a cruiser with length 5. */
  private static final String Ship5PicPath =
      "/com/matti/battleship/images/ships/cruiser_length5.png";

  // __________________________________

  /**
   * Returns the file path to the image corresponding to the specified ship length.
   *
   * @param shipLength the length of the ship, represented by the {@link ShipLength} enum
   * @return the file path to the ship's image
   * @throws IllegalArgumentException if the provided ship length is invalid
   */
  public String getPictureOfShip(ShipLength shipLength) {
    // Make sure all files really exist
    checkAllShipPictureResources();

    switch (shipLength) {
      case Two:
        return Ship2PicPath;

      case Three:
        return Ship3PicPath;

      case Four:
        return Ship4PicPath;

      case Five:
        return Ship5PicPath;

      default:
        throw new IllegalArgumentException("An invalid ship length was passed!");
    }
  }

  // ----- private methods -----

  /**
   * Attempts to load a resource from the classpath given its relative path. Logs an error if the
   * resource cannot be found or loaded.
   *
   * @param relPath the relative path to the resource within the classpath
   */
  private void tryToLoadResource(String relPath) {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(relPath)) {
      if (input == null) {
        logger.error("Resource at {} couldn't be loaded!", relPath);
      }
    } catch (IOException e) {
      logger.error("The resource at {} couldn't be found or loaded! Error: {}", relPath, e);
    }
  }

  /**
   * Checks the availability of all ship picture resources by attempting to load each one. Logs
   * errors for any resources that cannot be loaded.
   */
  private void checkAllShipPictureResources() {
    // try to load all resources
    tryToLoadResource(Ship2PicPath);
    tryToLoadResource(Ship3PicPath);
    tryToLoadResource(Ship4PicPath);
    tryToLoadResource(Ship5PicPath);
  }
}
