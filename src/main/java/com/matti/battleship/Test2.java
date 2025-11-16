package com.matti.battleship;

import javafx.application.Application;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.sound.sampled.Line;

public class Test1 extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            //BorderPane root = new BorderPane();
            Group root = new Group();
            Scene scene = new Scene(root,Color.WHITE); //hier ev mit komma größe der Scene
            Stage stage = new Stage();
            scene.getStylesheets().add(
                    getClass().getResource("application.css").toExternalForm()
            );
            Image icon = new Image(
                    getClass().getResource("/com/matti/battleship/Icon.png").toExternalForm() //bindet das kleine Icon oben ein
            );

            Text text = new Text();
            text.setText("Hallo Welt");
            text.setX(50);
            text.setY(50);
            text.setFont(Font.font("Verdana",50));
            root.getChildren().add(text);
            text.setFill(Color.GREEN);


            stage.getIcons().add(icon);
            stage.setTitle("First JavaFx Test");
            stage.setWidth(600);
            stage.setHeight(600);
            //primaryStage.setResizable(false);
            //primaryStage.setFullScreen(true);
            stage.setFullScreenExitHint("Drücke q um Vollbild zu schließen");
            stage.setFullScreenExitKeyCombination(KeyCombination.valueOf("q"));

            stage.setScene(scene);
            stage.show();  //zeigt das eig Feld
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
