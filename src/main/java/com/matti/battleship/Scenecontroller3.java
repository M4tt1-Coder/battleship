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

public class Scenecontroller3 extends Application {

    private Scene scene1;
    private Scene scene2;
    private Scene scene3;
    private Scene scene4;

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
        Button button5 = new Button("Start Game");
        button5.setId("button5");

        Label label2 = new Label("Game Settings");
        label2.setId("label2");
        label2.setAlignment(Pos.TOP_CENTER);

        Image image1 = new Image(
                getClass().getResource("/com/matti/battleship/picture2.jpg").toExternalForm()
        );
        ImageView imageView1 = new ImageView(image1);

        ComboBox<String> combo1 = new ComboBox<>();
        combo1.getItems().addAll("Einfach", "Mittel", "Schwer");
        combo1.setValue("Mittel");

        TextField tf1 = new TextField();
        tf1.setPromptText("Anzahl Bote eingeben");
        TextField tf2 = new TextField();
        tf2.setPromptText("Größe des Feldes eingeben");


        StackPane root2 = new StackPane(button3,label2, button5, imageView1, combo1, tf1, tf2);
        root2.setId("pane2");


        // Ausrichtung
        button3.translateXProperty().bind(root2.widthProperty().multiply(-0.45)); // links 25%
        button3.translateYProperty().bind(root2.heightProperty().multiply(-0.43));
        button5.translateXProperty().bind(root1.widthProperty().multiply(-0.55)); // links 25%
        button5.translateYProperty().bind(root1.heightProperty().multiply(0.5)); // oben 25%

        label2.translateXProperty().bind(root1.widthProperty().multiply(-0.55)); // 25% nach links
        label2.translateYProperty().bind(root1.heightProperty().multiply(0.1)); // 25% nach unten

        combo1.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        combo1.translateYProperty().bind(root2.heightProperty().multiply(-0.1));

        imageView1.translateXProperty().bind(root2.widthProperty().multiply(0.25));
        imageView1.translateYProperty().bind(root2.heightProperty().multiply(0.0));

        tf1.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        tf1.translateYProperty().bind(root2.heightProperty().multiply(0.05));
        tf2.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        tf2.translateYProperty().bind(root2.heightProperty().multiply(0.2));

        button3.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),  // 3% der Breite als Schriftgröße
                ";"
        ));

        button5.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.02),  // 3% der Breite als Schriftgröße
                ";"
        ));

        label2.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.05),  // 3% der Breite als Schriftgröße
                ";"
        ));

        combo1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        scene2 = new Scene(root2, 800, 600);
        scene2.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        // Größe
        button3.prefWidthProperty().bind(scene2.widthProperty().multiply(0.07));
        button3.prefHeightProperty().bind(scene2.heightProperty().multiply(0.1));
        button5.prefWidthProperty().bind(scene1.widthProperty().multiply(0.3));
        button5.prefHeightProperty().bind(scene1.heightProperty().multiply(0.1));

        label2.prefWidthProperty().bind(scene1.widthProperty().multiply(0.7));
        label2.prefHeightProperty().bind(scene1.heightProperty().multiply(1));

        combo1.prefWidthProperty().bind(scene2.widthProperty().multiply(0.1));
        combo1.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));

        imageView1.fitWidthProperty().bind(scene2.widthProperty().multiply(0.5));   // 50% der Scene-Breite
        imageView1.fitHeightProperty().bind(scene2.heightProperty().multiply(1)); // 30% der Scene-Höhe

        tf1.prefWidthProperty().bind(scene2.widthProperty().multiply(0.001));
        tf1.setMaxWidth(200);
        tf1.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));
        tf2.prefWidthProperty().bind(scene2.widthProperty().multiply(0.001));
        tf2.setMaxWidth(200);
        tf2.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));






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

        // ------------------ Scene 4 ------------------
        StackPane root4 = new StackPane();
        root4.setId("pane4");
        scene4 = new Scene(root4, 800, 600);
        scene4.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        // ------------------ Scene-Wechsel ------------------
        button1.setOnAction(e -> {
            primaryStage.setScene(scene2);
            Platform.runLater(() -> primaryStage.setFullScreen(true));

        });
        button2.setOnAction(e -> {
            primaryStage.setScene(scene3);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });
        button3.setOnAction(e -> {
            primaryStage.setScene(scene1);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });
        button4.setOnAction(e -> {
            primaryStage.setScene(scene1);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });
        button5.setOnAction(e -> {
            primaryStage.setScene(scene4);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });




        // ------------------ Stage Setup ------------------
        primaryStage.setTitle("Battleship");
        Image icon = new Image(
                getClass().getResource("/com/matti/battleship/Icon.png").toExternalForm() //bindet das kleine Icon oben ein
        );
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setFullScreen(true));
    }

    public static void main(String[] args) {
        launch(args);
    }
}