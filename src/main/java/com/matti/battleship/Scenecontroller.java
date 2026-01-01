package com.matti.battleship;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.matti.battleship.types.Buttons;



public class Scenecontroller4 extends Application {
    private Scene scene1;

    @Override
    public void start(Stage primaryStage) {


        // ---------------Scene 1--------------------------------------------------------------------- Scene 1
        // ---------------------------------------------
        // Pane 1
        Buttons button1 = new Buttons("Singleplayer");
        button1.setId("button1");

        StackPane root1 = new StackPane(button1);
        root1.setId("pane1");
        root1.getChildren().addAll();
        root1.setStyle("-fx-background-color: lightblue;"); // Hintergrund

        button1.position(root1, -0.25, 0.25);
        button1.fontsize(root1, 0.02);
        button1.size(root1, 0.2, 0.1);

        // Scene 1




        StackPane root2 = new StackPane();
        root2.setId("pane2");
        root2.getChildren().addAll();
        root2.setStyle("-fx-background-color: darkblue;");
        // Scene 2


        button1.setOnAction(e -> {
            scene1.setRoot(root2); // Root von scene1 wird auf root2 gesetzt
        });

        // ---------------Stage Setup--------------------------------------------------------------Stage Setup
        scene1 = new Scene(root1, 800, 600);
        scene1.getStylesheets().add(getClass().
                getResource("style1.css").toExternalForm());

        primaryStage.setTitle("Battleship");
        Image icon = new Image(
                getClass().getResource(
                        "/com/matti/battleship/Icon.png")
                        .toExternalForm()
        );
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}