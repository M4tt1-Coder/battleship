package com.matti.battleship;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Scenecontroller3 extends Application {

    private Scene scene1;
    private Scene scene2;
    private Scene scene3;

    @Override
    public void start(Stage primaryStage) {
        // ------------------ Scene 1 ------------------
        Button button1 = new Button("Singleplayer");
        button1.setId("button1");
        Button button2 = new Button("Multiplayer");
        button2.setId("button2");

        Label label1 = new Label("Battleship");
        label1.setId("label1");
        label1.setAlignment(Pos.CENTER);

        StackPane root1 = new StackPane();
        root1.setId("pane1");
        root1.getChildren().addAll(button1, button2, label1);

        // Links/rechts dynamisch positionieren
        button1.translateXProperty().bind(root1.widthProperty().multiply(-0.25)); // links 25%
        button2.translateXProperty().bind(root1.widthProperty().multiply(0.25));  // rechts 25%
        button1.translateYProperty().bind(root1.heightProperty().multiply(0.25)); // oben 25%
        button2.translateYProperty().bind(root1.heightProperty().multiply(0.25)); // oben 25%
        label1.translateXProperty().bind(root1.widthProperty().multiply(0.00)); // 25% nach links
        label1.translateYProperty().bind(root1.heightProperty().multiply(-0.45)); // 25% nach unten


        scene1 = new Scene(root1, 800, 600);
        scene1.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        // Breite/Höhe relativ zur Scene
        button1.prefWidthProperty().bind(scene1.widthProperty().multiply(0.2));   // 20% der Breite
        button1.prefHeightProperty().bind(scene1.heightProperty().multiply(0.1)); // 10% der Höhe
        button2.prefWidthProperty().bind(scene1.widthProperty().multiply(0.2));
        button2.prefHeightProperty().bind(scene1.heightProperty().multiply(0.1));
        label1.prefWidthProperty().bind(scene1.widthProperty().multiply(0.4));
        label1.prefHeightProperty().bind(scene1.heightProperty().multiply(0.2));

        button1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.02),  // 3% der Breite als Schriftgröße
                ";"
        ));
        button2.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.02),  // 3% der Breite als Schriftgröße
                ";"
        ));
        label1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.05),  // 3% der Breite als Schriftgröße
                ";"
        ));



        // ------------------ Scene 2 ------------------
        Button button3 = new Button();
        button3.setId("button3");
        Image image1 = new Image(
                getClass().getResource("/com/matti/battleship/picture2.jpg").toExternalForm()
        );
        ImageView imageView1 = new ImageView(image1);
        ComboBox<String> combo1 = new ComboBox<>();
        combo1.getItems().addAll("Einfach", "Mittel", "Schwer");
        combo1.setValue("Mittel");

        StackPane root2 = new StackPane(imageView1, combo1, button3);
        root2.setId("pane2");



        button3.translateXProperty().bind(root2.widthProperty().multiply(-0.45)); // links 25%
        button3.translateYProperty().bind(root2.heightProperty().multiply(-0.43));
        combo1.translateXProperty().bind(root2.widthProperty().multiply(-0.3));
        combo1.translateYProperty().bind(root2.heightProperty().multiply(0.0));

        button3.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),  // 3% der Breite als Schriftgröße
                ";"
        ));
        combo1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        scene2 = new Scene(root2, 800, 600);
        scene2.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        button3.prefWidthProperty().bind(scene2.widthProperty().multiply(0.07));
        button3.prefHeightProperty().bind(scene2.heightProperty().multiply(0.1));

        combo1.prefWidthProperty().bind(scene2.widthProperty().multiply(0.07));
        combo1.prefHeightProperty().bind(scene2.heightProperty().multiply(0.1));

        // ------------------ Scene 3 ------------------
        Button button4 = new Button();
        button4.setId("button4");
        Image image2 = new Image(
                getClass().getResource("/com/matti/battleship/picture3.jpg").toExternalForm()
        );
        ImageView imageView2 = new ImageView(image2);

        StackPane root3 = new StackPane(imageView2, button4);
        root3.setId("pane3");

        button4.translateXProperty().bind(root3.widthProperty().multiply(-0.45)); // links 25%
        button4.translateYProperty().bind(root3.heightProperty().multiply(-0.43));

        button4.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root3.widthProperty().multiply(0.01),  // 3% der Breite als Schriftgröße
                ";"
        ));
        scene3 = new Scene(root3, 800, 600);
        scene3.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        button4.prefWidthProperty().bind(scene3.widthProperty().multiply(0.07));   // 20% der Breite
        button4.prefHeightProperty().bind(scene3.heightProperty().multiply(0.1));

        // ------------------ Scene-Wechsel ------------------
        button1.setOnAction(e -> {
            primaryStage.setScene(scene2);
            primaryStage.setFullScreen(true);
        });
        button2.setOnAction(e -> {
            primaryStage.setScene(scene3);
            primaryStage.setFullScreen(true);
        });
        button3.setOnAction(e -> {
            primaryStage.setScene(scene1);
            primaryStage.setFullScreen(true);
        });
        button4.setOnAction(e -> {
            primaryStage.setScene(scene1);
            primaryStage.setFullScreen(true);
        });

        // ------------------ Stage Setup ------------------
        primaryStage.setTitle("Battleship");
        Image icon = new Image(
                getClass().getResource("/com/matti/battleship/Icon.png").toExternalForm() //bindet das kleine Icon oben ein
        );
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}