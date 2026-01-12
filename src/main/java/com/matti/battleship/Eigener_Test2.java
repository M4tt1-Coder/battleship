package com.matti.battleship;

import com.matti.battleship.types.Buttons;
import com.matti.battleship.types.ComboBoxes;
import com.matti.battleship.types.ImageViews;
import com.matti.battleship.types.Labels;
import com.matti.battleship.types.TextFields;
import java.util.Objects;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Eigener_Test2 extends Application {

  private Scene scene1;
  int selected_field_size = 10;
  int selected_amount_of_boats = 5;

  private Image loadImageOrThrow(String resourcePath) {
    return new Image(
        Objects.requireNonNull(
                getClass().getResource(resourcePath), "Resource not found: " + resourcePath)
            .toExternalForm());
  }

  @Override
  public void start(Stage primaryStage) {

    // ---------------root 1---------------------------------------------------------------------
    Buttons button11 = new Buttons("Singleplayer");
    button11.setId("button11");

    Buttons button12 = new Buttons("Multiplayer");
    button12.setId("button12");

    Labels label11 = new Labels("Battleship");
    label11.setId("label11");

    StackPane root1 = new StackPane(button11, button12, label11);
    root1.setId("pane1");

    button11.position(root1, -0.25, 0.25);
    button11.fontsize(root1, 0.02);
    button11.size(root1, 0.2, 0.1);

    button12.position(root1, 0.25, 0.25);
    button12.fontsize(root1, 0.02);
    button12.size(root1, 0.2, 0.1);

    label11.position(root1, 0.00, -0.45);
    label11.fontsize(root1, 0.05);
    label11.size(root1, 0.4, 0.2);
    label11.setAlignment(Pos.CENTER);

    // ---------------root 2---------------------------------------------------------------------
    Image image21 = loadImageOrThrow("/com/matti/battleship/picture2.jpg");
    ImageViews imageview21 = new ImageViews(image21);

    Buttons button21b = new Buttons();
    button21b.setId("back_button");

    Buttons button22 = new Buttons("Start Game");
    button22.setId("button22");

    ComboBoxes combobox21 = new ComboBoxes();
    combobox21.setId("combobox21");

    TextFields tf21 = new TextFields();
    tf21.setId("tf21");
    tf21.setPromptText("Type in amount of boats");

    TextFields tf22 = new TextFields();
    tf22.setId("tf22");
    tf22.setPromptText("Type in field size");

    Labels label21 = new Labels("Settings");
    label21.setId("label21");

    StackPane root2 =
        new StackPane(imageview21, button21b, label21, button22, tf21, tf22, combobox21);
    root2.setId("pane2");

    imageview21.position(root2, 0.25, 0.00);
    imageview21.size(root2, 0.5, 1);

    button21b.position(root2, -0.45, -0.43);
    button21b.fontsize(root2, 0.01);
    button21b.size(root2, 0.07, 0.1);

    button22.position(root2, -0.255, 0.3);
    button22.fontsize(root2, 0.02);
    button22.size(root2, 0.15, 0.05);

    combobox21.set_selections("Medium", "Easy", "Medium", "Hard");
    combobox21.position(root2, -0.255, -0.15);
    combobox21.fontsize(root2, 0.01);
    combobox21.size(root2, 0.12, 0.05);

    tf21.position(root2, -0.255, 0.00);
    tf21.fontsize(root2, 0.01);
    tf21.size(root2, 0.12, 0.05);
    tf21.setMinWidth(80);
    tf21.setMaxWidth(200);

    tf22.position(root2, -0.255, 0.15);
    tf22.fontsize(root2, 0.01);
    tf22.size(root2, 0.12, 0.05);
    tf22.setMinWidth(80);
    tf22.setMaxWidth(200);

    label21.position(root2, -0.255, 0.05);
    label21.fontsize(root2, 0.03);
    label21.size(root2, 0.3, 0.7);
    label21.setAlignment(Pos.TOP_CENTER);

    // ---------------root 3---------------------------------------------------------------------
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

    // label darf Drag&Drop nicht blocken
    label41.setMouseTransparent(true);

    // Dragboard key: wir legen STRING (Resource-Pfad) ab, NICHT Image (nicht serialisierbar)
    final DataFormat DF_SHIP_PATH = new DataFormat("SHIP_PATH");

    button22.setOnAction(
        e -> {

          // root4 reset (sonst doppelte grids/ships nach mehrfachem klicken)
          root4.getChildren().clear();
          root4.getChildren().addAll(button41e, label41, button42);

          // label bleibt mouse-transparent
          label41.setMouseTransparent(true);

          if (!tf22.getText().isEmpty()) {
            try {
              selected_field_size = Integer.parseInt(tf22.getText());
            } catch (NumberFormatException ex) {
              System.out.println("Ungültige Feldgröße, Standardwert 10");
            }
          }
          if (!tf21.getText().isEmpty()) {
            try {
              selected_amount_of_boats = Integer.parseInt(tf21.getText());
            } catch (NumberFormatException ex) {
              System.out.println("Ungültige Bootanzahl, Standardwert 5");
            }
          }

          double BOARD_SIZE = 400;
          double cellSize = BOARD_SIZE / selected_field_size;

          GridPane battleGrid = new GridPane();
          battleGrid.setPrefSize(BOARD_SIZE, BOARD_SIZE);
          battleGrid.setMaxSize(BOARD_SIZE, BOARD_SIZE);
          battleGrid.setStyle("-fx-background-color: transparent;");

          // ---------- ZELLEN bauen ----------
          for (int row = 0; row < selected_field_size; row++) {
            for (int col = 0; col < selected_field_size; col++) {
              StackPane cell = new StackPane();
              cell.setPrefSize(cellSize, cellSize);
              cell.setStyle("-fx-border-color: black;-fx-background-color: lightblue;");

              final int r = row;
              final int c = col;

              cell.setOnDragOver(
                  ev -> {
                    Dragboard db = ev.getDragboard();
                    if (db.hasContent(DF_SHIP_PATH) || db.hasString()) {
                      ev.acceptTransferModes(TransferMode.COPY);
                    }
                    ev.consume();
                  });

              cell.setOnDragDropped(
                  ev -> {
                    Dragboard db = ev.getDragboard();

                    String path = null;
                    if (db.hasContent(DF_SHIP_PATH)) {
                      path = (String) db.getContent(DF_SHIP_PATH);
                    } else if (db.hasString()) {
                      path = db.getString();
                    }

                    if (path == null) {
                      ev.setDropCompleted(false);
                      ev.consume();
                      return;
                    }

                    Image img = loadImageOrThrow(path);

                    // neues Bild in die Zelle (Kopie), Palette bleibt oben
                    javafx.scene.image.ImageView placed = new javafx.scene.image.ImageView(img);
                    placed.setFitWidth(cellSize * 0.8);
                    placed.setFitHeight(cellSize * 0.8);
                    placed.setPreserveRatio(false);

                    cell.getChildren().setAll(placed);
                    StackPane.setAlignment(placed, Pos.CENTER);

                    System.out.println("Zelle belegt: (" + r + "," + c + ")");
                    ev.setDropCompleted(true);
                    ev.consume();
                  });

              battleGrid.add(cell, col, row);
            }
          }

          // ---------- PALETTE: mehrere Schiffe oben ----------
          String p3 = "/com/matti/battleship/ship3.png";
          String p4 = "/com/matti/battleship/ship5_2.png";
          String p5 = "/com/matti/battleship/ship5.png";

          Image img3 = loadImageOrThrow(p3);
          javafx.scene.image.ImageView ship3 = new javafx.scene.image.ImageView(img3);
          ship3.setFitWidth(cellSize * 0.8);
          ship3.setFitHeight(cellSize * 0.8);
          ship3.setPreserveRatio(false);

          Image img4 = loadImageOrThrow(p4);
          javafx.scene.image.ImageView ship4 = new javafx.scene.image.ImageView(img4);
          ship4.setFitWidth(cellSize * 0.8);
          ship4.setFitHeight(cellSize * 0.8);
          ship4.setPreserveRatio(false);

          Image img5 = loadImageOrThrow(p5);
          javafx.scene.image.ImageView ship5 = new javafx.scene.image.ImageView(img5);
          ship5.setFitWidth(cellSize * 0.8);
          ship5.setFitHeight(cellSize * 0.8);
          ship5.setPreserveRatio(false);

          // Drag-Start fuer Palette: String (Resource-Pfad) ins Dragboard legen (COPY)
          java.util.function.BiConsumer<javafx.scene.image.ImageView, String> makeDraggable =
              (iv, path) ->
                  iv.setOnDragDetected(
                      ev -> {
                        Dragboard db = iv.startDragAndDrop(TransferMode.COPY);
                        ClipboardContent content = new ClipboardContent();
                        content.put(DF_SHIP_PATH, path);
                        content.putString(path); // fallback
                        db.setContent(content);
                        ev.consume();
                      });

          makeDraggable.accept(ship3, p3);
          makeDraggable.accept(ship4, p4);
          makeDraggable.accept(ship5, p5);

          // ---------- Layout in root4 ----------
          root4.getChildren().addAll(battleGrid, ship3, ship4, ship5);

          // Grid zentriert
          StackPane.setAlignment(battleGrid, Pos.CENTER);

          // Palette: knapp ueber dem Grid (relativ)
          double yAboveGrid = -(BOARD_SIZE / 2.0) - (cellSize * 0.8);
          ship3.setTranslateY(yAboveGrid);
          ship4.setTranslateY(yAboveGrid);
          ship5.setTranslateY(yAboveGrid);

          ship3.setTranslateX(-cellSize * 2.0);
          ship4.setTranslateX(0);
          ship5.setTranslateX(cellSize * 2.0);

          // Sichtbarkeits-Check: Shadow
          ship3.setStyle("-fx-effect: dropshadow(gaussian, black, 20, 0, 0, 0);");
          ship4.setStyle("-fx-effect: dropshadow(gaussian, black, 20, 0, 0, 0);");
          ship5.setStyle("-fx-effect: dropshadow(gaussian, black, 20, 0, 0, 0);");

          scene1.setRoot(root4);
        });

    // ---------------root 5---------------------------------------------------------------------
    StackPane root5 = new StackPane();
    root5.setId("pane5");

    button42.setOnAction(
        e -> {
          double BOARD_SIZE = 400;
          double BUTTON_SIZE = BOARD_SIZE / selected_field_size;
          GridPane grid = new GridPane();
          grid.setHgap(0);
          grid.setVgap(0);
          grid.setPadding(new Insets(12));

          for (int r = 0; r < selected_field_size; r++) {
            for (int c = 0; c < selected_field_size; c++) {
              Buttons btn = new Buttons(r + "," + c);
              btn.setStyle("-fx-background-radius: 0; -fx-border-radius: 0;");
              btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
              final int rr = r;
              final int cc = c;
              btn.setOnAction(ev -> System.out.println("Clicked: row=" + rr + " col=" + cc));
              grid.add(btn, c, r);
            }
          }

          root5.getChildren().addAll(grid);
          grid.setAlignment(Pos.CENTER);

          scene1.setRoot(root5);
        });

    // ---------------button_actions---------------------------------------------------------------------
    button11.setOnAction(e -> scene1.setRoot(root2));
    button12.setOnAction(e -> scene1.setRoot(root3));
    button21b.setOnAction(e -> scene1.setRoot(root1));
    button31b.setOnAction(e -> scene1.setRoot(root1));
    button41e.setOnAction(e -> scene1.setRoot(root1));

    // ---------------Stage Setup--------------------------------------------------------------
    scene1 = new Scene(root1, 800, 600);
    scene1.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

    primaryStage.setTitle("Battleship");
    Image icon = loadImageOrThrow("/com/matti/battleship/Icon.png");
    primaryStage.getIcons().add(icon);
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
