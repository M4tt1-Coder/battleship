package com.matti.battleship;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ButtonGridApp extends Application {
  private static final int ROWS = 6; // change as needed
  private static final int COLS = 8; // change as needed
  private static final int BUTTON_SIZE = 60;

  @Override
  public void start(Stage primaryStage) {
    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);
    grid.setPadding(new Insets(12));

    for (int r = 0; r < ROWS; r++) {
      for (int c = 0; c < COLS; c++) {
        Button btn = new Button(r + "," + c);
        btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        final int rr = r;
        final int cc = c;
        btn.setOnAction(e -> System.out.println("Clicked: row=" + rr + " col=" + cc));
        grid.add(btn, c, r);
      }
    }

    Scene scene = new Scene(grid);
    primaryStage.setTitle("2D Button Grid");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
