package com.matti.battleship.IO;

import com.matti.battleship.enums.*;
import com.matti.battleship.types.*;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class FileReaderService {

    // ---------------- FileChooser ----------------

    public static File chooseSaveFile(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Savegame laden");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Battleship Save (*.txt)", "*.txt")
        );

        return chooser.showOpenDialog(ownerWindow); // null wenn abgebrochen
    }

    // ---------------- Public: Load Game ----------------

    public static Game loadGameFromFile(File file) throws Exception {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        // 1) Erst Metadaten sammeln (Size, Namen, Game-Settings)
        ParsedMeta meta = parseMeta(lines);

        // 2) Player + Opponent anlegen (Board direkt mit size!)
        Player player = new Player(meta.playerName, meta.boardSize);
        Player opponent = new Player(meta.opponentName, meta.boardSize);

        // 3) Game anlegen (weil playingMode/initialShipSetup final sind!)
        Game game = new Game(
                meta.playingMode,
                player,
                opponent,
                meta.turn,
                meta.initialSetup
        );

        // 4) game-state setzen (hasEnded & winner sind nicht final)
        game.setHasEnded(meta.hasEnded);
        game.setWinner(meta.winner);

        // 5) Jetzt Boards ausfüllen (Felder + Shots + Ships)
        fillBoards(lines, player.board, opponent.board);

        return game;
    }

    // =========================================================
    // ====================== Parsing ==========================
    // =========================================================

    private static class ParsedMeta {
        boolean hasEnded = false;
        PlayingMode playingMode = PlayingMode.VS_PLAYER;
        ShipLength[] initialSetup = new ShipLength[0];
        PlayerTurn turn = PlayerTurn.PLAYER;
        Winner winner = Winner.NONE_YET;

        String playerName = "Player";
        String opponentName = "Opponent";

        int boardSize = 10;
    }

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
                }
            }

            if (section.equals("player")) {
                if (key.equals("name")) meta.playerName = value;
            }

            if (section.equals("opponent")) {
                if (key.equals("name")) meta.opponentName = value;
            }

            // size steht im board-block, ist aber für Player/Opponent identisch
            if (key.equals("size")) {
                try {
                    meta.boardSize = Integer.parseInt(value);
                } catch (Exception ignored) {}
            }
        }

        return meta;
    }

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

                if (section.equals("player")) currentBoard = playerBoard;
                else if (section.equals("opponent")) currentBoard = opponentBoard;
                else currentBoard = null;

                continue;
            }

            // Feld Start: *0|0
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
                    // ship kann leer sein
                    if (!value.isBlank()) {
                        Ship ship = parseShip(value);
                        if (ship != null) {
                            currentField.setShip(ship); // setzt occupied automatisch true
                            // andere Felder vom Schiff werden später durch isOccupied=true markiert (steht ja in Datei)
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
        // erwartet: "x|y"
        String[] parts = s.split("\\|");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        return new Coordinates(x, y);
    }

    private static Ship parseShip(String raw) {
        // Format aus eurer Ship.toString():
        // (id=UUID,start=0|0,direction=UP,hasSunk=false,length=TWO)

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

        if (hasSunk) ship.alterHasSunk(); // default ist false → falls true im save, togglen
        return ship;
    }

    private static ShipLength[] parseShipSetup(String s) {
        // Arrays.toString -> "[TWO, TWO, THREE]" oder "[]"
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
        // euer Writer schreibt Enum -> z.B. VS_PLAYER
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
}
