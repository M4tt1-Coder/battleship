package com.matti.battleship;

import com.matti.battleship.computer.PlacementAlgorithm;
import com.matti.battleship.enums.AIDifficulty;
import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.PlayerTurn;
import com.matti.battleship.enums.PlayingMode;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.*;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.GameUtils;
import com.matti.battleship.utils.PlayingUtils;
import com.matti.battleship.utils.datatypes.ShipGridElement;
import java.io.File;
import javafx.application.Application;
import javafx.beans.binding.Bindings; // NEW
import javafx.beans.binding.DoubleBinding; // NEW
import javafx.beans.property.DoubleProperty; // NEW
import javafx.beans.property.SimpleDoubleProperty; // NEW
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

// option + shift + f -> formatieren
public class BattleShipApp extends Application {

  private Scene scene1;
  private int selected_field_size = 10;

  // NEW: Boardgröße dynamisch (abhängig von Scene)
  private final DoubleProperty boardSize = new SimpleDoubleProperty(400);

  // OPTIONAL: falls du BOARD_SIZE weiter als "Default" behalten willst
  private double BOARD_SIZE = 400;
  private double cellSize = BOARD_SIZE / selected_field_size;
  private int selected_amount_of_boats = 5;

  // ----- Temporary Game -----
  private Game game;
  private PlayingMode playingMode;
  @Nullable private AIDifficulty difficulty;

  // percentage rule ... 30% of the field must be occupied by ships
  private ShipLength[] initialShipSetup;

  // ----- Player -----
  private Board board;

  // ----- Game Logic -----
  private int[] coordinatesOfLastDraggedShip = new int[2];

  @Override
  public void start(Stage primaryStage) {

    // ---------------root 1
    // ---------------------------------------------------------------------
    // root1

    Buttons Singleplayer_button_r1 = new Buttons("Singleplayer");
    Singleplayer_button_r1.setId("Singleplayer_button_r1");

    Buttons Multiplayer_button_r1 = new Buttons("Multiplayer");
    Multiplayer_button_r1.setId("Multiplayer_button_r1");

    Labels label_battleship_lobby_r1 = new Labels("Battleship");
    label_battleship_lobby_r1.setId("label_battleship_lobby_r1");

    StackPane root1 =
        new StackPane(Singleplayer_button_r1, Multiplayer_button_r1, label_battleship_lobby_r1);
    root1.setId("stack_pane_root1");

    // Layout root1:

    Singleplayer_button_r1.position(root1, -0.253, 0.35);
    Singleplayer_button_r1.fontsize(root1, 0.05);
    Singleplayer_button_r1.size(root1, 0.495, 0.3);

    Multiplayer_button_r1.position(root1, 0.25, 0.351);
    Multiplayer_button_r1.fontsize(root1, 0.05);
    Multiplayer_button_r1.size(root1, 0.5, 0.3);

    label_battleship_lobby_r1.position(root1, 0.00, -0.4);
    label_battleship_lobby_r1.fontsize(root1, 0.05);
    label_battleship_lobby_r1.size(root1, 0.4, 0.1);
    label_battleship_lobby_r1.setAlignment(Pos.CENTER);

    // ---------------root 2
    // ---------------------------------------------------------------------
    // root2

    Image image_player_vs_ai =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/player_vs_ai.jpg")
                .toExternalForm());
    ImageViews imageview_player_vs_ai = new ImageViews(image_player_vs_ai);

    Buttons back_button_r2 = new Buttons();
    back_button_r2.setId("back_button");

    Buttons start_game_button_r2 = new Buttons("Start Game");
    start_game_button_r2.setId("start_game_button");

    Buttons load_game_button_r2 = new Buttons("Load Game");
    load_game_button_r2.setId("load_game_button_r2");

    ComboBoxes difficulty_selection_r2 = new ComboBoxes();
    difficulty_selection_r2.setId("combobox21");

    TextFields select_field_size_r2 = new TextFields();
    select_field_size_r2.setId("text_field_field_size");
    select_field_size_r2.setPromptText("Type in field size");

    Labels label_settings_r2 = new Labels("Settings");
    label_settings_r2.setId("label_settings");

    Labels label_select_difficulty_r2 = new Labels("Difficulty:");
    label_select_difficulty_r2.setId("label_select_difficulty_r2");

    Labels label_size_of_field_r2 = new Labels("Field Size:");
    label_size_of_field_r2.setId("label_size_of_field_r2");

    StackPane root2 =
        new StackPane(
            imageview_player_vs_ai,
            back_button_r2,
            label_settings_r2,
            start_game_button_r2,
            load_game_button_r2,
            select_field_size_r2,
            difficulty_selection_r2,
            label_select_difficulty_r2,
            label_size_of_field_r2);
    root2.setId("stack_pane_root2");

    imageview_player_vs_ai.position(root2, 0.25, 0.00);
    imageview_player_vs_ai.size(root2, 0.5, 1);

    back_button_r2.position(root2, -0.45, -0.43);
    back_button_r2.fontsize(root2, 0.01);
    back_button_r2.size(root2, 0.07, 0.1);

    start_game_button_r2.position(root2, -0.325, 0.25);
    start_game_button_r2.fontsize(root2, 0.02);
    start_game_button_r2.size(root2, 0.13, 0.05);

    load_game_button_r2.position(root2, -0.185, 0.25);
    load_game_button_r2.fontsize(root2, 0.02);
    load_game_button_r2.size(root2, 0.13, 0.05);

    difficulty_selection_r2.set_selections("Medium", "Easy", "Medium", "Hard");
    difficulty_selection_r2.position(root2, -0.2, -0.05);
    difficulty_selection_r2.fontsize(root2, 0.01);
    difficulty_selection_r2.size(root2, 0.15, 0.05);

    select_field_size_r2.position(root2, -0.2, 0.1);
    select_field_size_r2.fontsize(root2, 0.01);
    select_field_size_r2.size(root2, 0.15, 0.05);

    label_settings_r2.position(root2, -0.255, 0.05);
    label_settings_r2.fontsize(root2, 0.03);
    label_settings_r2.size(root2, 0.3, 0.6);
    label_settings_r2.setAlignment(Pos.TOP_CENTER);

    label_select_difficulty_r2.position(root2, -0.355, -0.05);
    label_select_difficulty_r2.fontsize(root2, 0.02);
    label_select_difficulty_r2.size(root2, 0.1, 0.07);
    label_select_difficulty_r2.setAlignment(Pos.CENTER);

    label_size_of_field_r2.position(root2, -0.355, 0.1);
    label_size_of_field_r2.fontsize(root2, 0.02);
    label_size_of_field_r2.size(root2, 0.1, 0.07);
    label_size_of_field_r2.setAlignment(Pos.CENTER);

    // ---------------root 3
    // ---------------------------------------------------------------------
    // root3

    Buttons back_button_r3 = new Buttons();
    back_button_r3.setId("back_button");

    Buttons start_new_game_button_r3 = new Buttons("Create own Game");
    start_new_game_button_r3.setId("start_new_game_button_r3");

    Labels label_available_servers_r3 = new Labels("Join other players");
    label_available_servers_r3.setId("label_background");

    StackPane root3 =
        new StackPane(back_button_r3, label_available_servers_r3, start_new_game_button_r3);
    root3.setId("stack_pane_root3");

    back_button_r3.position(root3, -0.45, -0.43);
    back_button_r3.fontsize(root3, 0.01);
    back_button_r3.size(root3, 0.07, 0.1);

    start_new_game_button_r3.position(root3, 0.00, 0.3);
    start_new_game_button_r3.fontsize(root3, 0.015);
    start_new_game_button_r3.size(root3, 0.2, 0.07);

    label_available_servers_r3.position(root3, 0, 0.05);
    label_available_servers_r3.fontsize(root3, 0.03);
    label_available_servers_r3.size(root3, 0.7, 0.8);
    label_available_servers_r3.setAlignment(Pos.TOP_CENTER);

    // ---------------root 4
    // ---------------------------------------------------------------------
    // root4
    Buttons end_game_button_r4 = new Buttons(); // ändern
    end_game_button_r4.setId("end_game_button");

    Buttons start_game_button_r4 = new Buttons("Start");
    start_game_button_r4.setId("start_game_button");

    Labels background_label_select_position_r4 = new Labels("Select the position of your boats");
    background_label_select_position_r4.setId("label_background");

    Labels background_label_ships_r4 = new Labels("");
    background_label_ships_r4.setId("label_background");

    StackPane root4 = new StackPane();
    root4.setId("pane4");

    end_game_button_r4.position(root4, -0.4, -0.43);
    end_game_button_r4.fontsize(root4, 0.01);
    end_game_button_r4.size(root4, 0.15, 0.06);

    start_game_button_r4.position(root4, 0.38, 0.3);
    start_game_button_r4.fontsize(root4, 0.02);
    start_game_button_r4.size(root4, 0.15, 0.05);

    background_label_select_position_r4.position(root4, 0, -0.01);
    background_label_select_position_r4.fontsize(root4, 0.03);
    background_label_select_position_r4.size(root4, 0.6, 0.8);
    background_label_select_position_r4.setAlignment(Pos.TOP_CENTER);

    background_label_ships_r4.position(root4, 0, 0.4);
    background_label_ships_r4.fontsize(root4, 0.03);
    background_label_ships_r4.size(root4, 0.95, 0.1);

    // ---------------root 5
    // ---------------------------------------------------------------------
    // root5

    Buttons end_game_button_r5 = new Buttons();
    end_game_button_r5.setId("end_game_button");

    Labels background_label_r5 = new Labels("");
    background_label_r5.setId("label_background");

    StackPane root5 = new StackPane(end_game_button_r5, background_label_r5);
    root5.setId("pane5");

    end_game_button_r5.position(root5, -0.4, -0.43);
    end_game_button_r5.fontsize(root5, 0.01);
    end_game_button_r5.size(root5, 0.15, 0.06);

    background_label_r5.position(root5, 0, -0.01);
    background_label_r5.fontsize(root5, 0.03);
    background_label_r5.size(root5, 0.6, 0.8);
    background_label_r5.setAlignment(Pos.TOP_CENTER);

    // ---------------root 6
    // ---------------------------------------------------------------------
    // root6
    Image image_player_vs_player =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/player_vs_player.jpg")
                .toExternalForm());
    ImageViews imageview_player_vs_player = new ImageViews(image_player_vs_player);

    Buttons back_button_r6 = new Buttons();
    back_button_r6.setId("back_button");

    Buttons start_game_button_r6 = new Buttons("Start Game");
    start_game_button_r6.setId("start_game_button");

    TextFields select_field_size_r6 = new TextFields();
    select_field_size_r6.setId("text_field_field_size");
    select_field_size_r6.setPromptText("Type in field size");

    Labels label_settings_r6 = new Labels("Settings");
    label_settings_r6.setId("label_settings");

    Labels label_size_of_field_r6 = new Labels("Field Size:");
    label_size_of_field_r6.setId("label_size_of_field");

    StackPane root6 =
        new StackPane(
            imageview_player_vs_player,
            back_button_r6,
            label_settings_r6,
            start_game_button_r6,
            select_field_size_r6,
            label_size_of_field_r6);
    root6.setId("stack_pane_root6");

    imageview_player_vs_player.position(root6, 0.25, 0.00);
    imageview_player_vs_player.size(root6, 0.5, 1);

    back_button_r6.position(root6, -0.45, -0.43);
    back_button_r6.fontsize(root6, 0.01);
    back_button_r6.size(root6, 0.07, 0.1);

    start_game_button_r6.position(root6, -0.25, 0.05);
    start_game_button_r6.fontsize(root6, 0.02);
    start_game_button_r6.size(root6, 0.13, 0.05);

    select_field_size_r6.position(root6, -0.2, -0.07);
    select_field_size_r6.fontsize(root6, 0.01);
    select_field_size_r6.size(root6, 0.15, 0.05);

    label_settings_r6.position(root6, -0.255, -0.05);
    label_settings_r6.fontsize(root6, 0.03);
    label_settings_r6.size(root6, 0.3, 0.4);
    label_settings_r6.setAlignment(Pos.TOP_CENTER);

    label_size_of_field_r6.position(root6, -0.355, -0.07);
    label_size_of_field_r6.fontsize(root6, 0.02);
    label_size_of_field_r6.size(root6, 0.1, 0.07);
    label_size_of_field_r6.setAlignment(Pos.CENTER);

    // ---------------root 7
    // ---------------------------------------------------------------------
    // root7

    // ---------------root 8
    // ---------------------------------------------------------------------
    // root8

    // ---------------button_actions---------------------------------------------------------------------

    // --------------------------------- root 1
    // ----------------------
    Singleplayer_button_r1.setOnAction(
        e -> {
          scene1.setRoot(root2);
          this.playingMode = PlayingMode.VS_AI;
        });
    Multiplayer_button_r1.setOnAction(
        e -> {
          scene1.setRoot(root3);
          this.playingMode = PlayingMode.VS_PLAYER;
        });

    // --------------------------------- root 2
    // ----------------------
    back_button_r2.setOnAction(e -> scene1.setRoot(root1));

    load_game_button_r2.setOnAction(
        e -> {
          FileChooser fileChooser_r2 = new FileChooser();
          fileChooser_r2.setTitle("Vorheriges Spiel laden");
          fileChooser_r2.setInitialDirectory(new File("."));
          fileChooser_r2
              .getExtensionFilters()
              .add(new FileChooser.ExtensionFilter("*.png", "*.jpg", "*.jpeg"));
          File file = fileChooser_r2.showOpenDialog((Stage) root2.getScene().getWindow());
        });

    start_game_button_r2.setOnAction(
        e -> {
          root4
              .getChildren()
              .addAll(
                  end_game_button_r4,
                  background_label_select_position_r4,
                  background_label_ships_r4,
                  start_game_button_r4);

          if (!select_field_size_r2.getText().isEmpty()) {
            try {
              this.selected_field_size = Integer.parseInt(select_field_size_r2.getText());
              this.cellSize = BOARD_SIZE / selected_field_size;
            } catch (NumberFormatException ex) {
              System.out.println("Ungültige Feldgröße, Standardwert 10");
              this.selected_field_size = 10;
            }
          }

          // prepare ship setup for ship placement
          this.initialShipSetup =
              BoardUtils.generateShipSetupForPlacement(this.selected_field_size);

          // save current AIDifficulty
          String selectedDifficultyString =
              difficulty_selection_r2.getSelectionModel().getSelectedItem();
          this.difficulty = GameUtils.getDifficultyFromString(selectedDifficultyString);

          this.board = new Board(selected_field_size);

          // ev das global dann kann battle grid auch in anderen angezeigt
          GridPane battleGrid = new GridPane();

          // NEW: Grid skaliert mit boardSize
          battleGrid.prefWidthProperty().bind(boardSize);
          battleGrid.prefHeightProperty().bind(boardSize);
          battleGrid.minWidthProperty().bind(boardSize);
          battleGrid.minHeightProperty().bind(boardSize);
          battleGrid.maxWidthProperty().bind(boardSize);
          battleGrid.maxHeightProperty().bind(boardSize);

          battleGrid.setStyle("-fx-background-color: transparent;");

          // initialize the grid with cells
          initializePlacementBoard(battleGrid);

          prepareShipRectangles(root4);

          root4.getChildren().add(battleGrid);
          StackPane.setAlignment(battleGrid, Pos.CENTER);
          scene1.setRoot(root4);
        });

    // --------------------------------- root 3
    // ----------------------

    back_button_r3.setOnAction(e -> scene1.setRoot(root1));
    start_new_game_button_r3.setOnAction(e -> scene1.setRoot(root6));

    // --------------------------------- root 4
    end_game_button_r4.setOnAction(
        e -> {
          root4.getChildren().clear();
          scene1.setRoot(root1);
        });

    start_game_button_r4.setOnAction(
        e -> {

          // TODO: Add the case for playing against another player -> no board needs to be
          // added
          System.out.println("Starting game with board:");
          BoardUtils.logBoardToConsole(this.board);
          Board opponentBoard = new Board(this.selected_field_size);
          PlacementAlgorithm.placeShips(opponentBoard, this.initialShipSetup);
          this.game =
              new Game(
                  this.playingMode,
                  new Player("Player", this.selected_field_size),
                  new Player("Opponent", this.selected_field_size),
                  PlayerTurn.PLAYER,
                  this.initialShipSetup);
          this.game.opponent.board = opponentBoard;
          this.game.player.board = this.board;
          BoardUtils.logBoardToConsole(opponentBoard);
          // NEW: dynamische Buttongröße (statt BOARD_SIZE)
          DoubleBinding BUTTON_SIZE =
              Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

          GridPane grid = new GridPane();
          grid.setHgap(0);
          grid.setVgap(0);
          grid.setPadding(new Insets(12));

          // OPTIONAL: Grid auch dynamisch groß machen
          grid.prefWidthProperty().bind(boardSize);
          grid.prefHeightProperty().bind(boardSize);

          Image imgMiss =
              new Image(
                  getClass()
                      .getResource("/com/matti/battleship/images/game/tile_miss.png")
                      .toExternalForm());
          Image imgHit =
              new Image(
                  getClass()
                      .getResource("/com/matti/battleship/images/game/tile_hit.png")
                      .toExternalForm());

          for (int r = 0; r < selected_field_size; r++) {
            for (int c = 0; c < selected_field_size; c++) {
              Buttons btn = new Buttons();
              btn.setStyle(
                  "-fx-background-color: lightgray; -fx-border-color: black; -fx-background-radius: 0; -fx-border-radius: 0;");

              // NEW: Button skaliert mit boardSize
              btn.prefWidthProperty().bind(BUTTON_SIZE);
              btn.prefHeightProperty().bind(BUTTON_SIZE);
              btn.minWidthProperty().bind(BUTTON_SIZE);
              btn.minHeightProperty().bind(BUTTON_SIZE);

              final int rr = r;
              final int cc = c;

              btn.setOnAction(
                  ev -> {
                    System.out.println("Clicked: row=" + rr + " col=" + cc);
                    // hier prüfuzng ob treffer oder nicht

                    ImageViews iv = new ImageViews(imgMiss);

                    // NEW: Icon skaliert mit Buttongröße
                    iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
                    iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
                    iv.setPreserveRatio(false);

                    btn.setGraphic(iv);
                  });

              grid.add(btn, c, r);
            }
          }

          root5.getChildren().addAll(grid);
          grid.setAlignment(Pos.CENTER);
          end_game_button_r5.toFront();
          scene1.setRoot(root5);
        });

    // --------------------------------- root 5
    end_game_button_r5.setOnAction(
        e -> {
          root4.getChildren().clear();
          scene1.setRoot(root1);
        });

    // --------------------------------- root 6
    // ----------------------
    back_button_r6.setOnAction(
        e -> {
          scene1.setRoot(root3);
        });
    // start_game_button_r6.setOnAction(e -> { scene1.setRoot(root7);});

    // ---------------Stage
    // Setup--------------------------------------------------------------
    scene1 = new Scene(root1, 800, 600);
    scene1.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

    // NEW: boardSize hängt an Scene-Größe
    boardSize.bind(Bindings.min(scene1.widthProperty(), scene1.heightProperty()).multiply(0.65));

    primaryStage.setTitle("Battleship");
    Image icon =
        new Image(
            getClass().getResource("/com/matti/battleship/images/favicon.png").toExternalForm());
    primaryStage.getIcons().add(icon);
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  // _________________________________________________________________
  // ----- Helpers -----
  // _________________________________________________________________

  private void prepareShipRectangles(Pane root) {
    ShipLength[] allLengths = this.initialShipSetup;

    DoubleBinding cs =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    int offsetUnits = 0;

    for (int i = 0; i < allLengths.length; i++) {
      ShipLength length = allLengths[i];

      Rectangle ship = new Rectangle();
      ship.setFill(Color.DARKGRAY);
      ship.setArcWidth(10);
      ship.setArcHeight(10);

      // NEW: Schiffgröße dynamisch
      ship.widthProperty().bind(cs.multiply(length.getValue()).multiply(0.95));
      ship.heightProperty().bind(cs.multiply(0.8));

      ship.setUserData(
          new ShipGridElement(
              new Coordinates(0, 0), Direction.RIGHT, length, boardSize.intValue()));

      ship.setOnDragDetected(
          ev -> {
            // TODO: Add logic to rotate ship by pressing a key while dragging
            Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.format("SHIP_WIDTH_%d", PlayingUtils.getRandomInt()));
            db.setContent(content);
            ev.consume();
          });

      root.getChildren().add(ship);

      StackPane.setAlignment(ship, Pos.BOTTOM_LEFT);
      StackPane.setMargin(ship, new Insets(0, 0, 50, 25)); // top, right, bottom, left

      int finalOffsetUnits = offsetUnits;
      ship.translateXProperty().bind(cs.multiply(finalOffsetUnits).multiply(0.6));
      ship.setTranslateY(0);

      ship.setScaleX(0.6);
      ship.setScaleY(0.6);

      offsetUnits += length.getValue();
    }
  }

  private void initializePlacementBoard(GridPane grid) {
    DoubleBinding cs =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    for (Field[] row : board.board) {
      for (Field field : row) {
        int X = field.getCoordinates().x;
        int Y = field.getCoordinates().y;

        StackPane cell = new StackPane();

        // NEW: Zellgröße dynamisch
        cell.prefWidthProperty().bind(cs);
        cell.prefHeightProperty().bind(cs);
        cell.minWidthProperty().bind(cs);
        cell.minHeightProperty().bind(cs);

        cell.setStyle("-fx-border-color: black;-fx-background-color: lightblue;");

        cell.setOnDragOver(
            ev -> {
              if (ev.getGestureSource() != cell) {
                ev.acceptTransferModes(TransferMode.MOVE);
              }
              ev.consume();
            });

        cell.setOnDragDropped(
            ev -> {
              if (!ev.getDragboard().hasString()) return;
              Coordinates coords = new Coordinates(X, Y);

              Rectangle shipNode = (Rectangle) ev.getGestureSource();
              ShipGridElement shipData = (ShipGridElement) shipNode.getUserData();
              // first operate on the board data structure -> check if placement is valid
              Ship ship;
              Coordinates previousShipCoords = shipData.getCoordinates();
              boolean freshlyPlaced = false;
              if (shipData.isPlaced()) {
                ship = this.board.removeShip(shipData.getCoordinates());
                previousShipCoords = ship.getStartCoordinates();
                // update the new ship
                if (!ship.setStart(coords, this.board.getSize())) {
                  System.out.println("Failed to update ship start to " + coords);
                }
              } else {
                ship = new Ship(coords, shipData.getDirection(), shipData.getLength());
                shipData.setPlaced(true);
                freshlyPlaced = true;
              }

              if (!this.board.addShip(ship)) {
                // revert changes in ship
                ship.setStart(previousShipCoords, this.board.getSize());
                // try to add back the ship to the old position
                // if a ship was just placed freshly, no need to place is somewhere back on the
                // board
                if (!freshlyPlaced) {
                  if (!this.board.addShip(ship)) {
                    throw new IllegalStateException(
                        "Could not revert ship placement on the board!");
                  }
                } else {
                  shipData.setPlaced(false);
                }
                System.out.println(
                    "Could not place the ship back on its old field due to unknown reasons! Invalid placement at "
                        + coords);
                ev.setDropCompleted(false);
                ev.consume();
                return;
              }
              BoardUtils.logBoardToConsole(this.board);

              // NEW: translate reset, damit es im Grid nicht daneben sitzt
              shipNode.translateXProperty().unbind();
              shipNode.setTranslateX(0);
              shipNode.setTranslateY(0);
              shipNode.setScaleX(1);
              shipNode.setScaleY(1);

              if (shipNode.getParent() != null) {
                ((javafx.scene.layout.Pane) shipNode.getParent()).getChildren().remove(shipNode);
              }

              // apply grid layout to rectangle
              applyGridLayoutToRectangle(
                  shipNode,
                  grid,
                  Y,
                  X,
                  shipData.getDirection(),
                  shipData.getLength().getValue(),
                  this.board.getSize());

              // update "userData" of the rectangle
              shipData.setCoordinates(coords);
              shipData.setCoordinates(new Coordinates(X, Y));
              shipNode.setUserData(shipData);

              ev.setDropCompleted(true);
              ev.consume();
            });

        grid.add(cell, X, Y);
      }
    }
  }

  private void applyGridLayoutToRectangle(
      Rectangle rect,
      GridPane grid,
      int row,
      int col,
      Direction direction,
      int length,
      int boardSize) {
    // Schiff direkt ins Grid legen und spannen
    grid.getChildren().add(rect);
    switch (direction) {
      case DOWN:
        int finalColD = col;
        int finalRowD = row;
        if (row + (length - 1) > boardSize) {
          finalRowD = boardSize - length;
        }

        GridPane.setRowIndex(rect, finalRowD);
        GridPane.setColumnIndex(rect, finalColD);
        GridPane.setRowSpan(rect, length);
        GridPane.setColumnSpan(rect, 1);

        break;
      case RIGHT:
        int finalColR = col;
        if (col + (length - 1) > boardSize) {
          finalColR = boardSize - length;
        }
        int finalRowR = row;
        GridPane.setRowIndex(rect, finalRowR);
        GridPane.setColumnIndex(rect, finalColR);
        GridPane.setRowSpan(rect, 1);
        GridPane.setColumnSpan(rect, length);
        break;
    }
    GridPane.setHalignment(rect, javafx.geometry.HPos.CENTER);
    GridPane.setValignment(rect, javafx.geometry.VPos.CENTER);
  }

  // Entry Point -> main function

  public static void main(String[] args) {
    launch(args);
  }
}
