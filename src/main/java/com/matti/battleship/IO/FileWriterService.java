package com.matti.battleship.IO;

import com.matti.battleship.Starter;
import com.matti.battleship.types.Board;
import com.matti.battleship.types.Field;
import com.matti.battleship.types.Game;
import com.matti.battleship.types.Player;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * {@code FileWriterService} provides utility methods for serializing and saving the current game
 * state to a file located in the directory where the application's JAR file resides.
 *
 * <p>This class includes methods for generating a string representation of the game data, saving it
 * to a timestamped or hash-based file, and retrieving the directory of the running JAR. It is
 * designed to facilitate persistent storage of game progress and states, primarily for debugging or
 * game saving features.
 *
 * <p>All methods are static, emphasizing its utility nature. It relies on an internal logger for
 * logging operations and errors.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * boolean success = FileWriterService.safeGameStateToFile(currentGame, null);
 * }</pre>
 *
 * @see Game
 * @see Player
 * @see Board
 * @see Field
 */
public class FileWriterService {
  private static final Logger logger = LogManager.getLogger(FileWriterService.class);

  /**
   * Saves the current state of the game to a file in the directory where the application's JAR is
   * located.
   *
   * <p>If {@code fileName} is {@code null}, the method generates a filename based on the hash code
   * of the {@link Game} object, replacing any hyphens with underscores, and appends a ".txt"
   * extension. The game data is serialized into a string using {@link #generateFileString(Game)}
   * and written to the file.
   *
   * @param game the {@link Game} object representing the current game state to be saved. Must not
   *     be {@code null}.
   * @param fileName an optional filename for the save file. If {@code null}, a filename based on
   *     the game's hash code is used.
   * @return {@code true} if the game state was successfully saved; {@code false} otherwise.
   */
  public static boolean safeGameStateToFile(Game game, @Nullable String fileName) {
    // use the hash code of an object as file name when the game is initial saved
    // locally
    // safe file directly in the directory where .jar file is executed
    String _fileName =
        Objects.requireNonNullElseGet(fileName, () -> Integer.toString(game.hashCode()));
    _fileName = _fileName.replace("-", "_");
    _fileName = _fileName + ".txt";

    // get path to root directory where .jar placed -> uses the app entry point
    // (Starter)
    String jarFilePath = getJarDirectory(Starter.class);

    if (jarFilePath == null) {
      logger.error(
          "Couldn't create file to save to 'Game' state in the current directory of the .jar!");
      return false;
    }

    // build content string with the game data
    String contentString = generateFileString(game);

    String filePath = jarFilePath + File.separator + _fileName;

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(contentString);
      writer.close();
      logger.info("Saved game to the file {}!", filePath);
    } catch (IOException e) {
      logger.error(e);
      return false;
    }

    return true;
  }

  /**
   * Retrieves the directory path where the JAR file containing the specified class is located.
   *
   * <p>This method locates the code source of the provided class, converts its URL to a URI, and
   * then obtains the parent directory of the JAR file or class location.
   *
   * @param callingClass the {@link Class} object for which the JAR directory is to be determined.
   *     Typically, you pass the class that is calling this method.
   * @return the absolute path to the directory containing the JAR file, or {@code null} if the
   *     location cannot be determined due to an exception.
   */
  public static String getJarDirectory(Class<?> callingClass) {
    try {
      File jarFile =
          new File(callingClass.getProtectionDomain().getCodeSource().getLocation().toURI());
      return jarFile.getParentFile().getPath();
    } catch (URISyntaxException e) {
      // Handle potential URI syntax errors
      logger.warn("Error converting URL to URI: {}", e.getMessage());
      return null;
    } catch (NullPointerException e) {
      // Handle cases where getCodeSource() or getLocation() might be null
      logger.warn("Could not determine JAR location: {}", e.getMessage());
      return null;
    }
  }

  // ----- private methods -----

  /**
   * Generates a comprehensive string representation of the given {@link Game} object, including
   * general game data, player data, and opponent data.
   *
   * <p>This method constructs the string in a structured format suitable for saving to a file or
   * for debugging purposes. It internally calls other methods to append specific sections of game
   * information to the resulting string.
   *
   * @param game the {@link Game} object containing the data to be serialized. If {@code null}, the
   *     resulting string will indicate the absence of game data.
   * @return a String containing the serialized game data, including game info, player info, and
   *     opponent info.
   */
  private static String generateFileString(Game game) {
    StringBuilder sb = new StringBuilder();
    // first safe game data
    generateElementaryGameDataString(game, sb);
    // build player data
    generateElementPlayerDataString(game.player, false, sb);
    // build opponent data
    generateElementPlayerDataString(game.opponent, true, sb);

    return sb.toString();
  }

  /**
   * Generates a string representation of the elementary game data, including key game state
   * information.
   *
   * @param game the {@link Game} instance containing game state data
   */
  private static void generateElementaryGameDataString(Game game, StringBuilder sb) {
    String lineSeparator = System.lineSeparator();

    sb.append("---Game")
        .append(lineSeparator)
        .append("hasEnded=")
        .append(game.hasEnded())
        .append(lineSeparator)
        .append("playingMode=")
        .append(game.getPlayingMode())
        .append(lineSeparator)
        .append("initialShipSetup=")
        .append(Arrays.toString(game.getInitialShipSetup()))
        .append(lineSeparator)
        .append("whoseTurn=")
        .append(game.getWhoseTurn())
        .append(lineSeparator)
        .append("winner=")
        .append(game.getWinner())
        .append(lineSeparator)
        .append("aiDifficulty=")
        .append(game.getDifficulty())
        .append(lineSeparator);
  }

  /**
   * Appends the string representation of a Player's data to the provided StringBuilder.
   *
   * @param player the Player object containing player data
   * @param isOpponent boolean indicating if the player is an opponent
   * @param sb the StringBuilder to append data to
   */
  private static void generateElementPlayerDataString(
      Player player, boolean isOpponent, StringBuilder sb) {
    // decide heading line based on the player is the player or the opponent in the
    // game -> the rest
    // is the same referring to the data

    String lineSeparator = System.lineSeparator();

    if (isOpponent) {
      sb.append("---Opponent");
    } else {
      sb.append("---Player");
    }

    sb.append(lineSeparator)
        .append("name=")
        .append(player.getName())
        .append(lineSeparator)
        .append("ID=")
        .append(player.getID())
        .append(lineSeparator);

    generateElementBoardDataString(player.board, sb);
  }

  /**
   * Appends the string representation of a Board's data to the provided StringBuilder.
   *
   * @param board the Board object containing player data
   * @param sb the StringBuilder to append data to
   */
  private static void generateElementBoardDataString(Board board, StringBuilder sb) {
    String lineSeparator = System.lineSeparator();

    // apply all traits of a 'Board' object
    sb.append("-board")
        .append(lineSeparator)
        .append("size=")
        .append(board.getSize())
        .append(lineSeparator)
        .append("numberOfShips=")
        .append(board.numberOfShips)
        .append(lineSeparator)
        .append("#board")
        .append(lineSeparator);

    // add all in linear order when
    for (Field[] row : board.board) {
      for (Field field : row) {
        generateElementFieldDataString(field, sb);
      }
    }
  }

  /**
   * Appends the string representation of a Field's data to the provided StringBuilder.
   *
   * @param field the Field object containing data to be appended
   * @param sb the StringBuilder to append data to
   */
  private static void generateElementFieldDataString(Field field, StringBuilder sb) {
    String lineSeparator = System.lineSeparator();

    sb.append("*")
        .append(field.getCoordinates().toString())
        .append(lineSeparator)
        .append("isOccupied=")
        .append(field.isOccupied())
        .append(lineSeparator)
        .append("wasShotAt=")
        .append(field.wasShotAt())
        .append(lineSeparator)
        .append("ship=");

    // if the ship on the field is not null -> add it
    if (field.getShip() != null) {
      sb.append(field.getShip().toString());
    }

    sb.append(lineSeparator);
  }
}
