package com.matti.battleship.IO;

import com.matti.battleship.enums.*;
import com.matti.battleship.types.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileReaderService {
  private static final Logger logger = LogManager.getLogger(FileReaderService.class);

  // ---------------- FileChooser ----------------

  /**
   * Opens a file chooser dialog to select a file for opening, specifically for loading a saved
   * game.
   *
   * <p>The dialog title is set to "Load 'Game' from a file" and it initially opens in the
   * "./target/" directory. It filters files to show only those with the ".txt" extension, labeled
   * as "Battleship Save (*.txt)".
   *
   * @param ownerWindow the owner {@link Window} for the dialog, used to block input to other
   *     windows until closed.
   * @return the selected {@link File} if the user chooses one; {@code null} if the user cancels the
   *     operation.
   */
  public static File chooseSaveFile(Window ownerWindow) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Load 'Game' from a file");
    chooser.setInitialDirectory(new File("./target/"));

    chooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Battleship Save (*.txt)", "*.txt"));

    return chooser.showOpenDialog(ownerWindow);
  }

  // ---------------- Public: Load Game ----------------

  /**
   * Loads a {@link Game} instance from the specified save file.
   *
   * <p>This method reads all lines from the file using UTF-8 encoding, parses the metadata,
   * initializes the players and game state, fills their boards with the saved data, and sets
   * additional game properties such as whether the game has ended, the winner, and the difficulty
   * level.
   *
   * @param file the {@link File} object representing the saved game file to load.
   * @return a {@link Game} object reconstructed from the file data.
   * @throws Exception if there is an error reading or parsing the file.
   */
  public static Game loadGameFromFile(File file) throws Exception {
    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

    ParsedMeta meta = parseMeta(lines);

    Player player = new Player(meta.playerName, meta.boardSize);
    Player opponent = new Player(meta.opponentName, meta.boardSize);

    fillBoards(lines, player.board, opponent.board);

    player.board.setShipShare(meta.shipShare);
    opponent.board.setShipShare(meta.shipShare);

    Game game = new Game(meta.playingMode, player, opponent, meta.turn, meta.initialSetup);

    game.setHasEnded(meta.hasEnded);
    game.setWinner(meta.winner);
    game.setDifficulty(meta.difficulty);

    logger.info("Successfully loaded the game state from the file: {}", file);

    return game;
  }

  // =========================================================
  // ====================== Parsing ==========================
  // =========================================================

  /**
   * A helper class to store metadata parsed from the game save file.
   *
   * <p>Contains information about the game state, players, settings, and initial setup used during
   * the game loading process.
   */
  private static class ParsedMeta {
    /** Indicates whether the game has ended. */
    boolean hasEnded = false;

    /** The playing mode of the game, e.g., versus player or AI. */
    PlayingMode playingMode = PlayingMode.VS_PLAYER;

    /** The initial setup of ships on the board, represented as an array of ship lengths. */
    ShipLength[] initialSetup = new ShipLength[0];

    /** Whose turn it is to play. */
    PlayerTurn turn = PlayerTurn.PLAYER;

    /** The winner of the game, if it has ended. */
    Winner winner = Winner.NONE_YET;

    /** The difficulty level of the AI, if applicable. */
    AIDifficulty difficulty = AIDifficulty.MEDIUM;

    /** The name of the human player. */
    String playerName = "Player";

    /** The name of the opponent (could be another player or AI). */
    String opponentName = "Opponent";

    /** The size of the game board. */
    int boardSize = 10;

    /** Share of the board occupied by ships. */
    ShipBoardShare shipShare = ShipBoardShare.THIRTY;
  }

  /**
   * Parses the metadata from a list of lines representing the saved game file.
   *
   * <p>This method processes the lines, identifies sections (such as "game", "Player", "Opponent"),
   * and extracts key-value pairs to populate a {@link ParsedMeta} instance with the game's saved
   * state. It handles parsing of boolean, enum, integer, and list values, delegating specific
   * parsing tasks to helper methods like {@link #parsePlayingMode(String)}, {@link
   * #parseShipSetup(String)}, etc.
   *
   * @param lines the list of lines read from the save file, containing metadata sections and
   *     key-value pairs.
   * @return a {@link ParsedMeta} object populated with the parsed game state information.
   */
  private static ParsedMeta parseMeta(List<String> lines) {
    ParsedMeta meta = new ParsedMeta();

    String section = "";

    for (String raw : lines) {
      String line = raw.trim();
      if (line.isEmpty()) continue;

      if (line.startsWith("---")) {
        section = line.substring(3).trim().toLowerCase();
        continue;
      }

      if (!line.contains("=")) continue;

      String[] kv = line.split("=", 2);
      String key = kv[0].trim();
      String value = kv[1].trim();

      if (section.equals("game")) {
        switch (key) {
          case "hasEnded" -> meta.hasEnded = Boolean.parseBoolean(value);
          case "playingMode" -> meta.playingMode = parsePlayingMode(value);
          case "initialShipSetup" -> meta.initialSetup = parseShipSetup(value);
          case "whoseTurn" -> meta.turn = parseTurn(value);
          case "winner" -> meta.winner = parseWinner(value);
          case "aiDifficulty" -> meta.difficulty = parseDifficulty(value);
        }
      }

      if (section.equals("Player")) {
        if (key.equals("name")) meta.playerName = value;
      }

      if (section.equals("Opponent")) {
        if (key.equals("name")) meta.opponentName = value;
      }

      if (key.equals("size")) {
        try {
          meta.boardSize = Integer.parseInt(value);
        } catch (Exception ignored) {
        }
      }

      if (key.equals("shipShare")) {
        try {
          meta.shipShare = ShipBoardShare.valueOf(value);
        } catch (Exception ex) {
        }
      }
    }

    return meta;
  }

  /**
   * Fills the game boards for both the player and the opponent by parsing the provided lines.
   *
   * <p>This method reads sectioned data from the input lines, identifying the current section
   * (either "player" or "opponent") and updating the corresponding board's fields based on
   * key-value pairs. It handles fields such as occupation status, shot status, and ship placement.
   * It utilizes helper methods like {@link #parseCoordinates(String)} and {@link
   * #parseShip(String)} to interpret coordinate and ship data.
   *
   * @param lines the list of lines representing the saved game board data.
   * @param playerBoard the Board object representing the player's game board to be filled.
   * @param opponentBoard the Board object representing the opponent's game board to be filled.
   */
  private static void fillBoards(List<String> lines, Board playerBoard, Board opponentBoard) {
    String section = "";
    Board currentBoard = null;
    Field currentField = null;

    for (String raw : lines) {
      String line = raw.trim();
      if (line.isEmpty()) continue;

      if (line.startsWith("---")) {
        section = line.substring(3).trim().toLowerCase();
        currentField = null;

        if (section.equals("player")) {
          currentBoard = playerBoard;
        } else if (section.equals("opponent")) {
          currentBoard = opponentBoard;
        } else currentBoard = null;

        continue;
      }

      if (line.startsWith("*") && currentBoard != null) {
        String coordStr = line.substring(1).trim(); // "0|0"
        Coordinates c = parseCoordinates(coordStr);
        currentField = currentBoard.getFieldOnBoardByCoordinates(c);
        continue;
      }

      if (!line.contains("=") || currentBoard == null || currentField == null) {
        continue;
      }

      String[] kv = line.split("=", 2);
      String key = kv[0].trim();
      String value = kv[1].trim();

      switch (key) {
        case "isOccupied" -> currentField.setOccupied(Boolean.parseBoolean(value));

        case "wasShotAt" -> {
          boolean shot = Boolean.parseBoolean(value);
          if (shot) currentField.markAsShotAt();
        }

        case "ship" -> {
          if (!value.isBlank()) {
            Ship ship = parseShip(value);
            if (ship != null) {
              currentField.setShip(ship);
            }
          }
        }
      }
    }
  }

  // =========================================================
  // ================== Helper Parser =========================
  // =========================================================

  private static Coordinates parseCoordinates(String s) {
    s = s.substring(1, s.length() - 1);
    String[] parts = s.split("\\|");
    int x = Integer.parseInt(parts[0].trim());
    int y = Integer.parseInt(parts[1].trim());
    return new Coordinates(x, y);
  }

  private static Ship parseShip(String raw) {
    String s = raw.trim();
    if (s.startsWith("(")) s = s.substring(1);
    if (s.endsWith(")")) s = s.substring(0, s.length() - 1);

    String[] parts = s.split(",");

    String startStr = null;
    Direction dir = null;
    boolean hasSunk = false;
    ShipLength len = null;

    for (String p : parts) {
      String[] kv = p.split("=", 2);
      if (kv.length < 2) continue;

      String key = kv[0].trim();
      String value = kv[1].trim();

      switch (key) {
        case "start" -> startStr = value;
        case "direction" -> dir = Direction.valueOf(value);
        case "hasSunk" -> hasSunk = Boolean.parseBoolean(value);
        case "length" -> len = ShipLength.valueOf(value);
      }
    }

    if (startStr == null || dir == null || len == null) return null;

    Ship ship = new Ship(parseCoordinates(startStr), dir, len);

    if (hasSunk) ship.alterHasSunk();

    return ship;
  }

  private static ShipLength[] parseShipSetup(String s) {
    String cleaned = s.trim().replace("[", "").replace("]", "").trim();
    if (cleaned.isEmpty()) return new ShipLength[0];

    String[] parts = cleaned.split(",");
    ShipLength[] out = new ShipLength[parts.length];

    for (int i = 0; i < parts.length; i++) {
      out[i] = ShipLength.valueOf(parts[i].trim());
    }

    return out;
  }

  private static PlayingMode parsePlayingMode(String s) {
    try {
      return PlayingMode.valueOf(s.trim());
    } catch (Exception e) {
      return PlayingMode.VS_PLAYER;
    }
  }

  private static PlayerTurn parseTurn(String s) {
    try {
      return PlayerTurn.valueOf(s.trim());
    } catch (Exception e) {
      return PlayerTurn.PLAYER;
    }
  }

  private static Winner parseWinner(String s) {
    try {
      return Winner.valueOf(s.trim());
    } catch (Exception e) {
      return Winner.NONE_YET;
    }
  }

  @org.jetbrains.annotations.Nullable
  private static AIDifficulty parseDifficulty(String s) {
    try {
      return AIDifficulty.valueOf(s);
    } catch (Exception e) {
      return null;
    }
  }
}
