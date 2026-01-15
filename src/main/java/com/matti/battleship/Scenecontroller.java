package com.matti.battleship;

import com.matti.battleship.types.*;
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
import java.io.File;
import javafx.stage.Stage;

public class Scenecontroller extends Application {

  private Scene scene1;
  int selected_field_size = 10;
  int selected_amount_of_boats = 5;

  @Override
  public void start(Stage primaryStage) {

    // ---------------root 1---------------------------------------------------------------------
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

    // ---------------root 2---------------------------------------------------------------------
    // root2

    Image image_player_vs_ai =
        new Image(getClass().getResource("/com/matti/battleship/images/player_vs_ai.jpg").toExternalForm());
    ImageViews imageview_player_vs_ai = new ImageViews(image_player_vs_ai);

    Buttons back_button_r2 = new Buttons();
    back_button_r2.setId("back_button");

    Buttons start_game_button_r2 = new Buttons("Start Game");
    start_game_button_r2.setId("start_game_button_r2");

    Buttons load_game_button_r2 = new Buttons("Load Game");
    load_game_button_r2.setId("load_game_button_r2");

    ComboBoxes difficulty_selection_r2 = new ComboBoxes();
    difficulty_selection_r2.setId("combobox21");

    TextFields select_amount_of_boats_r2 = new TextFields();
    select_amount_of_boats_r2.setId("select_amount_of_boats_r2");
    select_amount_of_boats_r2.setPromptText("Type in amount of boats");

    TextFields select_field_size_r2 = new TextFields();
    select_field_size_r2.setId("tf22");
    select_field_size_r2.setPromptText("Type in field size");

    Labels label_settings_r2 = new Labels("Settings");
    label_settings_r2.setId("label_settings_r2");

    Labels label_select_difficulty_r2 = new Labels("Difficulty:");
    label_select_difficulty_r2.setId("label_select_difficulty_r2");

    Labels label_amount_of_boats_r2 = new Labels("Boat-\nAmount:");
      label_amount_of_boats_r2.setId("label_amount_of_boats_r2");

    Labels label_size_of_field_r2 = new Labels("Field Size:");
      label_size_of_field_r2.setId("label_size_of_field_r2");

    StackPane root2 =
        new StackPane(imageview_player_vs_ai, back_button_r2, label_settings_r2,
                start_game_button_r2,load_game_button_r2, select_amount_of_boats_r2, select_field_size_r2,
                difficulty_selection_r2, label_select_difficulty_r2, label_amount_of_boats_r2,label_size_of_field_r2);
    root2.setId("stack_pane_root2");

    imageview_player_vs_ai.position(root2, 0.25, 0.00);
    imageview_player_vs_ai.size(root2, 0.5, 1);

    back_button_r2.position(root2, -0.45, -0.43);
    back_button_r2.fontsize(root2, 0.01);
    back_button_r2.size(root2, 0.07, 0.1);

    start_game_button_r2.position(root2, -0.325, 0.3);
    start_game_button_r2.fontsize(root2, 0.02);
    start_game_button_r2.size(root2, 0.13, 0.05);

    load_game_button_r2.position(root2, -0.185, 0.3);
    load_game_button_r2.fontsize(root2, 0.02);
    load_game_button_r2.size(root2, 0.13, 0.05);

    difficulty_selection_r2.set_selections("Medium", "Easy", "Medium", "Hard");
    difficulty_selection_r2.position(root2, -0.2, -0.15);
    difficulty_selection_r2.fontsize(root2, 0.01);
    difficulty_selection_r2.size(root2, 0.15, 0.05);

    select_amount_of_boats_r2.position(root2, -0.2, 0.00);
    select_amount_of_boats_r2.fontsize(root2, 0.01);
    select_amount_of_boats_r2.size(root2, 0.15, 0.05);

    select_field_size_r2.position(root2, -0.2, 0.15);
    select_field_size_r2.fontsize(root2, 0.01);
    select_field_size_r2.size(root2, 0.15, 0.05);

    label_settings_r2.position(root2, -0.255, 0.05);
    label_settings_r2.fontsize(root2, 0.03);
    label_settings_r2.size(root2, 0.3, 0.7);
    label_settings_r2.setAlignment(Pos.TOP_CENTER);

    label_select_difficulty_r2.position(root2, -0.355, -0.15);
    label_select_difficulty_r2.fontsize(root2, 0.02);
    label_select_difficulty_r2.size(root2, 0.1, 0.07);
    label_select_difficulty_r2.setAlignment(Pos.CENTER);

    label_amount_of_boats_r2.position(root2, -0.355, 0.00);
    label_amount_of_boats_r2.fontsize(root2, 0.02);
    label_amount_of_boats_r2.size(root2, 0.1, 0.07);
    label_amount_of_boats_r2.setAlignment(Pos.CENTER);

    label_size_of_field_r2.position(root2, -0.355, 0.15);
    label_size_of_field_r2.fontsize(root2, 0.02);
    label_size_of_field_r2.size(root2, 0.1, 0.07);
    label_size_of_field_r2.setAlignment(Pos.CENTER);

    // ---------------root 3---------------------------------------------------------------------
    // root3

    Buttons button31b = new Buttons();
    button31b.setId("back_button");

    Buttons button32 = new Buttons("Start Game");
    button32.setId("button32");

    StackPane root3 = new StackPane(button31b, button32);
    root3.setId("pane3");

    button31b.position(root3, -0.45, -0.43);
    button31b.fontsize(root3, 0.01);
    button31b.size(root3, 0.07, 0.1);

    // ---------------root 4---------------------------------------------------------------------
    // root4
    Buttons button41e = new Buttons();
    button41e.setId("end_game_button");

    Buttons button42 = new Buttons("Start");
    button42.setId("button11");

    Labels label41 = new Labels("Select the position of you boats");
    label41.setId("label21");

    StackPane root4 = new StackPane(button41e, label41, button42);
    root4.setId("pane4");

    button41e.position(root4, -0.4, -0.43);
    button41e.fontsize(root4, 0.01);
    button41e.size(root4, 0.15, 0.06);

    button42.position(root4, 0.25, 0.4);
    button42.fontsize(root4, 0.02);
    button42.size(root4, 0.15, 0.05);

    label41.position(root4, 0, -0.01);
    label41.fontsize(root4, 0.03);
    label41.size(root4, 0.6, 0.8);
    label41.setAlignment(Pos.TOP_CENTER);

    start_game_button_r2.setOnAction(
        e -> {
          // prüfen ob Eingabe über tf22 + 21
          if (!select_field_size_r2.getText().isEmpty()) {
            try {
              selected_field_size = Integer.parseInt(select_field_size_r2.getText());
            } catch (NumberFormatException ex) {
              System.out.println("Ungültige Feldgröße, Standardwert 10");
            }
          }

          if (!select_amount_of_boats_r2.getText().isEmpty()) {
            try {
              selected_amount_of_boats = Integer.parseInt(select_amount_of_boats_r2.getText());
            } catch (NumberFormatException ex) {
              System.out.println("Ungültige Bootanzahl, Standardwert 5");
            }
          }

          Board board = new Board(selected_field_size);
          double BOARD_SIZE = 400;
          double cellSize = BOARD_SIZE / selected_field_size;

          GridPane battleGrid =
              new GridPane(); // ev das global dann kann battle grid auch in anderen angezeigt
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

              cell.setOnDragOver(
                  ev -> {
                    if (ev.getGestureSource() != cell) { // zelle nicht serlbst gezogen
                      ev.acceptTransferModes(TransferMode.MOVE);
                    }
                    ev.consume();
                  });

              cell.setOnDragDropped(
                  ev -> {

                    // Ship ship1 = new Ship(field.getCoordinates(),);
                    // Prüfung ob gültig
                    Rectangle ship = (Rectangle) ev.getGestureSource();
                    cell.getChildren().clear();
                    cell.getChildren().add(ship);
                    StackPane.setAlignment(ship, Pos.CENTER);
                    ev.setDropCompleted(true);
                    ev.consume();
                    System.out.println("Zelle belegt: (" + r + "," + c + ")");
                  });

              battleGrid.add(cell, c, r);
            }
          }

          Image ship_length3 =
              new Image(getClass().getResource("/com/matti/battleship/images/ships/destroyer_length2.png").toExternalForm());
          ImageViews imageview_ship_length3 = new ImageViews(ship_length3);
          imageview_ship_length3.setFitWidth(cellSize * 0.8);
          imageview_ship_length3.setFitHeight(cellSize * 0.8);

          Image ship_length5 =
              new Image(getClass().getResource("/com/matti/battleship/images/ships/aircraft_carrier_length4.png").toExternalForm());
          ImageViews imageview_ship_length5 = new ImageViews(ship_length5);
          imageview_ship_length5.setFitWidth(cellSize * 0.8);
          imageview_ship_length5.setFitHeight(cellSize * 0.8);

          Rectangle ship = new Rectangle(cellSize * 0.8, cellSize * 0.8, Color.DARKGRAY);
          ship.setOnDragDetected(
              ev -> {
                Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("SHIP");
                db.setContent(content);
                ev.consume();
              });

          root4.getChildren().addAll(battleGrid, ship);
          StackPane.setAlignment(battleGrid, Pos.CENTER);
          StackPane.setAlignment(ship, Pos.TOP_CENTER);
          scene1.setRoot(root4);
        });
    // ---------------root 5---------------------------------------------------------------------
    // root5

    Buttons button51e = new Buttons();
    button51e.setId("end_game_button");

    Labels label51 = new Labels("Test");
    label51.setId("label21");

    StackPane root5 = new StackPane(button51e, label51);
    root5.setId("pane5");

    button51e.position(root5, -0.4, -0.43);
    button51e.fontsize(root5, 0.01);
    button51e.size(root5, 0.15, 0.06);

    label51.position(root5, 0, -0.01);
    label51.fontsize(root5, 0.03);
    label51.size(root5, 0.6, 0.8);
    label51.setAlignment(Pos.TOP_CENTER);

    button42.setOnAction(
        e -> {
          double BOARD_SIZE = 400;
          double BUTTON_SIZE = BOARD_SIZE / selected_field_size;

          GridPane grid = new GridPane();
          grid.setHgap(0);
          grid.setVgap(0);
          grid.setPadding(new Insets(12));

          Image imgMiss =
              new Image(
                  getClass().getResource("/com/matti/battleship/images/game/tile_miss.png").toExternalForm());
          Image imgHit =
              new Image(
                  getClass().getResource("/com/matti/battleship/images/game/tile_hit.png").toExternalForm());

          for (int r = 0; r < selected_field_size; r++) {
            for (int c = 0; c < selected_field_size; c++) {
              Buttons btn = new Buttons();
              btn.setStyle(
                  "-fx-background-color: lightgray; -fx-border-color: black; -fx-background-radius: 0; -fx-border-radius: 0;");
              btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);

              final int rr = r;
              final int cc = c;

              btn.setOnAction(
                  ev -> {
                    System.out.println("Clicked: row=" + rr + " col=" + cc);
                    // hier prüfuzng ob treffer oder nicht
                    /*if(ship_was hit == false) {

                    }
                    else if(ship_was hit == true && ship_sunken == false) {

                    } else {

                    }
                     */

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
          scene1.setRoot(root5);
        });

    // ---------------button_actions---------------------------------------------------------------------
    Singleplayer_button_r1.setOnAction(e -> scene1.setRoot(root2));
    Multiplayer_button_r1.setOnAction(e -> scene1.setRoot(root3));
    back_button_r2.setOnAction(e -> scene1.setRoot(root1));
    button31b.setOnAction(e -> scene1.setRoot(root1));
    button41e.setOnAction(e -> scene1.setRoot(root1));
    button51e.setOnAction(e -> scene1.setRoot(root1));
    load_game_button_r2.setOnAction(e -> {
        FileChooser fileChooser_r2 = new FileChooser();
        fileChooser_r2.setTitle("Vorheriges Spiel laden");
        fileChooser_r2.setInitialDirectory(new File("."));
        fileChooser_r2.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("*.png", "*.jpg", "*.jpeg") //anpassen
        );
        File file = fileChooser_r2.showOpenDialog((Stage) root2.getScene().getWindow());
    });




    // ---------------Stage Setup--------------------------------------------------------------
    scene1 = new Scene(root1, 800, 600);
    scene1.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

    primaryStage.setTitle("Battleship");
    Image icon =
        new Image(getClass().getResource("/com/matti/battleship/images/favicon.png").toExternalForm());
    primaryStage.getIcons().add(icon);
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}

// Aufruf
// int boats[] = {2, 3, 3, 4, 5};
// for (int i = 0; i < selected_amount_of_boats; i++) {
// int length = boat[i % 4];
// add_ship()
// }

// auslagerung in ship als funktion
/*boolean add_ship(Ship ship) {
    cell.setOnDragDropped(
            ev -> {
            // hier ev noch bedingung ob möglich von matti
                Rectangle ship = (Rectangle) ev.getGestureSource();
                cell.getChildren().clear();
                cell.getChildren().add(ship);
                ship.setStart(,this)
                StackPane.setAlignment(ship, Pos.CENTER);
                ev.setDropCompleted(true);
                ev.consume();
                System.out.println("Zelle belegt: (" + r + "," + c + ")");
            });

    battleGrid.add(cell, col, row);


    ship.setOnDragDetected(
            ev -> {
                Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("SHIP");
                db.setContent(content);
                ev.consume();
            });
    StackPane.setAlignment(ship, Pos.TOP_CENTER);
}
 */
