package com.matti.battleship;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class BattleshipGridTest extends Application {

    private static final int GRID_SIZE = 2;
    private static final int CELL_SIZE = 100;

    @Override
    public void start(Stage stage) {

        GridPane grid = new GridPane();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {

                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle(
                        "-fx-border-color: black;" +
                                "-fx-background-color: lightblue;"
                );

                final int r = row;
                final int c = col;

                cell.setOnDragOver(e -> {
                    if (e.getGestureSource() != cell) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                cell.setOnDragDropped(e -> {
                    Rectangle ship = (Rectangle) e.getGestureSource();
                    cell.getChildren().clear();
                    cell.getChildren().add(ship);
                    e.setDropCompleted(true);
                    e.consume();

                    System.out.println("Zelle belegt: (" + r + "," + c + ")");
                });

                grid.add(cell, col, row);
            }
        }

        Rectangle ship = new Rectangle(80, 80, Color.DARKGRAY);

        ship.setOnDragDetected(e -> {
            Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("SHIP");
            db.setContent(content);
            e.consume();
        });

        VBox root = new VBox(20, ship, grid);
        root.setStyle("-fx-padding: 20;");

        stage.setScene(new Scene(root));
        stage.setTitle("Battleship 2x2 Grid Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}