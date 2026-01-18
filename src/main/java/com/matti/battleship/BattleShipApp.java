package com.matti.battleship;

import com.matti.battleship.enums.AIDifficulty;
import com.matti.battleship.enums.PlayingMode;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.types.*;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.GameUtils;

import java.io.File;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

// option + shift + f -> formatieren
public class BattleShipApp extends Application {

    private Scene scene1;
    int selected_field_size = 10;
    int selected_amount_of_boats = 5;

    // ----- Temporary Game -----
    private Game game;
    private PlayingMode playingMode;
    @Nullable
    private AIDifficulty difficulty;

    // percentage rule
    private ShipLength[] initialShipSetup;

    // ----- Player -----
    private Board board;

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

        StackPane root1 = new StackPane(Singleplayer_button_r1, Multiplayer_button_r1, label_battleship_lobby_r1);
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

        Image image_player_vs_ai = new Image(getClass().getResource("/com/matti/battleship/images/player_vs_ai.jpg").toExternalForm());
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

        StackPane root2 = new StackPane(imageview_player_vs_ai, back_button_r2, label_settings_r2, start_game_button_r2, load_game_button_r2, select_field_size_r2, difficulty_selection_r2, label_select_difficulty_r2, label_size_of_field_r2);
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

        StackPane root3 = new StackPane(back_button_r3, label_available_servers_r3, start_new_game_button_r3);
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

        Labels background_label_select_position_r4 = new Labels("Select the position of you boats");
        background_label_select_position_r4.setId("label_background");

        StackPane root4 = new StackPane(end_game_button_r4, background_label_select_position_r4, start_game_button_r4);
        root4.setId("pane4");

        end_game_button_r4.position(root4, -0.4, -0.43);
        end_game_button_r4.fontsize(root4, 0.01);
        end_game_button_r4.size(root4, 0.15, 0.06);

        start_game_button_r4.position(root4, 0.25, 0.4);
        start_game_button_r4.fontsize(root4, 0.02);
        start_game_button_r4.size(root4, 0.15, 0.05);

        background_label_select_position_r4.position(root4, 0, -0.01);
        background_label_select_position_r4.fontsize(root4, 0.03);
        background_label_select_position_r4.size(root4, 0.6, 0.8);
        background_label_select_position_r4.setAlignment(Pos.TOP_CENTER);

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
        Image image_player_vs_player = new Image(getClass().getResource("/com/matti/battleship/images/player_vs_player.jpg").toExternalForm());
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

        StackPane root6 = new StackPane(imageview_player_vs_player, back_button_r6, label_settings_r6, start_game_button_r6, select_field_size_r6, label_size_of_field_r6);
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
        Singleplayer_button_r1.setOnAction(e -> {
            scene1.setRoot(root2);
            this.playingMode = PlayingMode.VS_AI;
            // this.game = new Game(PlayingMode.VS_AI, new Player("Player", boardSize), ,
            // turn, initialShipSetup)
        });
        Multiplayer_button_r1.setOnAction(e -> {
            scene1.setRoot(root3);
            this.playingMode = PlayingMode.VS_PLAYER;
        });

        // --------------------------------- root 2
        // ----------------------
        back_button_r2.setOnAction(e -> scene1.setRoot(root1));

        load_game_button_r2.setOnAction(e -> {
            FileChooser fileChooser_r2 = new FileChooser();
            fileChooser_r2.setTitle("Vorheriges Spiel laden");
            fileChooser_r2.setInitialDirectory(new File("."));
            fileChooser_r2.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.png", "*.jpg", "*.jpeg") // anpassen
            );
            File file = fileChooser_r2.showOpenDialog((Stage) root2.getScene().getWindow());
        });

        start_game_button_r2.setOnAction(e -> {
            // prüfen ob Eingabe über tf22 + 21
            if (!select_field_size_r2.getText().isEmpty()) {
                try {
                    this.selected_field_size = Integer.parseInt(select_field_size_r2.getText());

                } catch (NumberFormatException ex) {
                    System.out.println("Ungültige Feldgröße, Standardwert 10");
                }
            }
            // prepare ship setup for ship placement
            this.initialShipSetup = BoardUtils.generateShipSetupForPlacement(this.selected_field_size);
            // save current AIDifficulty
            String selectedDifficultyString = difficulty_selection_r2.getSelectionModel().getSelectedItem();
            this.difficulty = GameUtils.getDifficultyFromString(selectedDifficultyString);

            this.board = new Board(selected_field_size);
            double BOARD_SIZE = 400;
            double cellSize = BOARD_SIZE / selected_field_size;

            // ev das global dann kann battle grid auch in anderen angezeigt
            GridPane battleGrid = new GridPane();
            // werden
            battleGrid.setPrefSize(BOARD_SIZE, BOARD_SIZE);
            battleGrid.setMaxSize(BOARD_SIZE, BOARD_SIZE);
            battleGrid.setStyle("-fx-background-color: transparent;");

            for (Field[] _row : board.board) {
                for (Field field : _row) {
                    int c = field.getCoordinates().x;
                    int r = field.getCoordinates().y;
                    StackPane cell = new StackPane();
                    cell.setPrefSize(cellSize, cellSize);
                    cell.setStyle("-fx-border-color: black;-fx-background-color: lightblue;");

                    cell.setOnDragOver(ev -> {
                        if (ev.getGestureSource() != cell) { // zelle nicht serlbst gezogen
                            ev.acceptTransferModes(TransferMode.MOVE);
                        }
                        ev.consume();
                    });

                    cell.setOnDragDropped(ev -> {
                        if (!ev.getDragboard().hasString()) return;

                        Rectangle shipNode = (Rectangle) ev.getGestureSource();

                        // aus altem Parent entfernen
                        if (shipNode.getParent() != null) {
                            ((javafx.scene.layout.Pane) shipNode.getParent()).getChildren().remove(shipNode);
                        }


                        int startCol = Math.max(0, c - 2);
                        int row = r;

                        // Schiff direkt ins Grid legen und spannen
                        battleGrid.getChildren().add(shipNode);
                        GridPane.setRowIndex(shipNode, row);
                        GridPane.setColumnIndex(shipNode, startCol);
                        GridPane.setRowSpan(shipNode, 1);
                        GridPane.setColumnSpan(shipNode, 3);

                        GridPane.setHalignment(shipNode, javafx.geometry.HPos.CENTER);
                        GridPane.setValignment(shipNode, javafx.geometry.VPos.CENTER);

                        ev.setDropCompleted(true);
                        ev.consume();

                        System.out.println("Ship spanning: (" + row + "," + startCol + ") + (" + row + "," + c + ")");
                    });

                    battleGrid.add(cell, c, r);
                }
            }

            Image ship_length3 = new Image(getClass().getResource("/com/matti/battleship/images/ships/destroyer_length2.png").toExternalForm());
            ImageViews imageview_ship_length3 = new ImageViews(ship_length3);
            imageview_ship_length3.setFitWidth(cellSize * 0.8);
            imageview_ship_length3.setFitHeight(cellSize * 0.8);

            Image ship_length5 = new Image(getClass().getResource("/com/matti/battleship/images/ships/aircraft_carrier_length4.png").toExternalForm());
            ImageViews imageview_ship_length5 = new ImageViews(ship_length5);
            imageview_ship_length5.setFitWidth(cellSize * 0.8);
            imageview_ship_length5.setFitHeight(cellSize * 0.8);

            for (ShipLength length : this.initialShipSetup) {
                Rectangle ship = new Rectangle(cellSize * length.getValue(), cellSize * 0.8, Color.DARKGRAY);
                ship.setArcWidth(10);
                ship.setArcHeight(10);

                ship.setOnDragDetected(ev -> {
                    Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("SHIP_LEN2_LEFT");
                    db.setContent(content);
                    ev.consume();
                });
                root4.getChildren().addAll(battleGrid, ship);
                StackPane.setAlignment(ship, Pos.TOP_CENTER);
            }


            StackPane.setAlignment(battleGrid, Pos.CENTER);
            scene1.setRoot(root4);
        });

        // --------------------------------- root 3
        // ----------------------

        back_button_r3.setOnAction(e -> scene1.setRoot(root1));
        start_new_game_button_r3.setOnAction(e -> {
            scene1.setRoot(root6);
        });

        // --------------------------------- root 4
        // ----------------------

        end_game_button_r4.setOnAction(e -> scene1.setRoot(root1));

        start_game_button_r4.setOnAction(e -> {
            double BOARD_SIZE = 400;
            double BUTTON_SIZE = BOARD_SIZE / selected_field_size;

            GridPane grid = new GridPane();
            grid.setHgap(0);
            grid.setVgap(0);
            grid.setPadding(new Insets(12));

            Image imgMiss = new Image(getClass().getResource("/com/matti/battleship/images/game/tile_miss.png").toExternalForm());
            Image imgHit = new Image(getClass().getResource("/com/matti/battleship/images/game/tile_hit.png").toExternalForm());

            for (int r = 0; r < selected_field_size; r++) {
                for (int c = 0; c < selected_field_size; c++) {
                    Buttons btn = new Buttons();
                    btn.setStyle("-fx-background-color: lightgray; -fx-border-color: black; -fx-background-radius: 0; -fx-border-radius: 0;");
                    btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

                    final int rr = r;
                    final int cc = c;

                    btn.setOnAction(ev -> {
                        System.out.println("Clicked: row=" + rr + " col=" + cc);
                        // hier prüfuzng ob treffer oder nicht

                        ImageViews iv = new ImageViews(imgMiss);
                        iv.setFitWidth(BUTTON_SIZE * 0.4);
                        iv.setFitHeight(BUTTON_SIZE * 0.4);
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
        // ----------------------
        end_game_button_r5.setOnAction(e -> scene1.setRoot(root1));

        // --------------------------------- root 6
        // ----------------------
        back_button_r6.setOnAction(e -> {
            scene1.setRoot(root3);
        });
        // start_game_button_r6.setOnAction(e -> { scene1.setRoot(root7);});

        // ---------------Stage
        // Setup--------------------------------------------------------------
        scene1 = new Scene(root1, 800, 600);
        scene1.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

        primaryStage.setTitle("Battleship");
        Image icon = new Image(getClass().getResource("/com/matti/battleship/images/favicon.png").toExternalForm());
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
