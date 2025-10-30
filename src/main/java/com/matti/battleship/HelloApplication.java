package com.matti.battleship;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Hello!");
        Button btn = new Button("Click me");
        btn.setOnAction( event ->
                System.out.println("hello world")

        );

        StackPane root = new StackPane(); //Layout
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 300);
        primaryStage.setScene(scene);
        
        primaryStage.show();
    }
}
