package com.matti.battleship;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BattleshipApp extends Application {
  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Hello!");
    Button btn = new Button("Click me");
    btn.setOnAction(event -> System.out.println("hello world"));

    StackPane root = new StackPane(); // Layout
    root.getChildren().add(btn);

    Scene scene = new Scene(root, 300, 300);
    primaryStage.setScene(scene);

    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args); // Launch JavaFX application
  }
}
