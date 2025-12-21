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
    private Scene scene5;

    @Override
    public void start(Stage primaryStage) {

        // ---------------Scene 1--------------------------------------------------------------------- Scene 1
        // ---------------------------------------------
        // Adds all the Elements in Scene 1
        Button button1 = new Button("Singleplayer");
        button1.setId("button1");

        Button button2 = new Button("Multiplayer");
        button2.setId("button2");

        Label label1 = new Label("Battleship");
        label1.setId("label1");
        label1.setAlignment(Pos.CENTER);

        // Pane 1
        StackPane root1 = new StackPane();
        root1.setId("pane1");
        root1.getChildren().addAll(button1, button2, label1);


        // ---------------------------------------------
        // Position of all elements in Scene 1
        button1.translateXProperty().bind(root1.widthProperty().multiply(-0.25));
        button1.translateYProperty().bind(root1.heightProperty().multiply(0.25));

        button2.translateXProperty().bind(root1.widthProperty().multiply(0.25));
        button2.translateYProperty().bind(root1.heightProperty().multiply(0.25));

        label1.translateXProperty().bind(root1.widthProperty().multiply(0.00));
        label1.translateYProperty().bind(root1.heightProperty().multiply(-0.45));


        // ---------------------------------------------
        // Font Size of all elements in Scene 1
        button1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.02),
                ";"
        ));

        button2.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.02),
                ";"
        ));

        label1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root1.widthProperty().multiply(0.05),
                ";"
        ));


        // ---------------------------------------------
        // Scene 1
        scene1 = new Scene(root1, 800, 600);

        scene1.getStylesheets().add(getClass().
                getResource("style1.css").toExternalForm());


        // ---------------------------------------------
        // Size of all elements in Scene 1
        button1.prefWidthProperty().bind(scene1.widthProperty().multiply(0.2));
        button1.prefHeightProperty().bind(scene1.heightProperty().multiply(0.1));

        button2.prefWidthProperty().bind(scene1.widthProperty().multiply(0.2));
        button2.prefHeightProperty().bind(scene1.heightProperty().multiply(0.1));

        label1.prefWidthProperty().bind(scene1.widthProperty().multiply(0.4));
        label1.prefHeightProperty().bind(scene1.heightProperty().multiply(0.2));


        // ---------------Scene 2--------------------------------------------------------------------- Scene 2
        // ---------------------------------------------
        // Adds all the Elements in Scene 2
        Image image1 = new Image(
                getClass().getResource(
                        "/com/matti/battleship/picture2.jpg")
                        .toExternalForm()
        );
        ImageView imageView1 = new ImageView(image1);

        Button button3 = new Button();
        button3.setId("button3");

        Button button5 = new Button("Start Game");
        button5.setId("button5");

        ComboBox<String> combo1 = new ComboBox<>();
        combo1.getItems().addAll("Easy", "Medium", "Hard");
        combo1.setValue("Medium");

        TextField tf1 = new TextField();
        tf1.setPromptText("Type in amount of boats");

        TextField tf2 = new TextField();
        tf2.setPromptText("Type in field size");

        Label label2 = new Label("Game Settings");
        label2.setId("label2");
        label2.setAlignment(Pos.TOP_CENTER);

        Label label3 = new Label("Difficulty");
        label3.setId("label3");
        label3.setAlignment(Pos.CENTER);

        Label label4 = new Label("Amount of Boats");
        label4.setId("label4");
        label4.setAlignment(Pos.CENTER);

        Label label5 = new Label("Fiels Size");
        label5.setId("label4");
        label5.setAlignment(Pos.CENTER);

        // Pane 2
        StackPane root2 = new StackPane(button3,label2, label3, label4, label5,
                button5, imageView1, combo1, tf1, tf2);
        root2.setId("pane2");

        // ---------------------------------------------
        // Position of all elements in Scene 2
        imageView1.translateXProperty().bind(root2.widthProperty().multiply(0.25));
        imageView1.translateYProperty().bind(root2.heightProperty().multiply(0.0));

        button3.translateXProperty().bind(root2.widthProperty().multiply(-0.45));
        button3.translateYProperty().bind(root2.heightProperty().multiply(-0.43));

        button5.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        button5.translateYProperty().bind(root2.heightProperty().multiply(0.3));

        combo1.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        combo1.translateYProperty().bind(root2.heightProperty().multiply(-0.15));

        tf1.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        tf1.translateYProperty().bind(root2.heightProperty().multiply(0.00));

        tf2.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        tf2.translateYProperty().bind(root2.heightProperty().multiply(0.15));

        label2.translateXProperty().bind(root2.widthProperty().multiply(-0.255));
        label2.translateYProperty().bind(root2.heightProperty().multiply(0.05));

        label3.translateXProperty().bind(root2.widthProperty().multiply(-0.355));
        label3.translateYProperty().bind(root2.heightProperty().multiply(-0.15));

        label4.translateXProperty().bind(root2.widthProperty().multiply(-0.355));
        label4.translateYProperty().bind(root2.heightProperty().multiply(0.0));

        label5.translateXProperty().bind(root2.widthProperty().multiply(-0.355));
        label5.translateYProperty().bind(root2.heightProperty().multiply(0.15));

        // ---------------------------------------------
        // Font Size of all elements in Scene 2
        button3.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        button5.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.02),
                ";"
        ));

        combo1.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        label2.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.03),
                ";"
        ));

        label3.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        label4.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        label5.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root2.widthProperty().multiply(0.01),
                ";"
        ));

        // ---------------------------------------------
        // Scene 2
        scene2 = new Scene(root2, 800, 600);
        scene2.getStylesheets().add(getClass().
                getResource("style1.css").toExternalForm());

        // ---------------------------------------------
        // Size of all elements in Scene 1
        imageView1.fitWidthProperty().bind(scene2.widthProperty().multiply(0.5));
        imageView1.fitHeightProperty().bind(scene2.heightProperty().multiply(1));

        button3.prefWidthProperty().bind(scene2.widthProperty().multiply(0.07));
        button3.prefHeightProperty().bind(scene2.heightProperty().multiply(0.1));

        button5.prefWidthProperty().bind(scene2.widthProperty().multiply(0.15));
        button5.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));

        combo1.prefWidthProperty().bind(scene2.widthProperty().multiply(0.12));
        combo1.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));

        tf1.prefWidthProperty().bind(scene2.widthProperty().multiply(0.1));
        tf1.setMinWidth(80);
        tf1.setMaxWidth(200);
        tf1.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));

        tf2.prefWidthProperty().bind(scene2.widthProperty().multiply(0.1));
        tf2.setMinWidth(80);
        tf2.setMaxWidth(200);
        tf2.prefHeightProperty().bind(scene2.heightProperty().multiply(0.05));

        label2.prefWidthProperty().bind(scene2.widthProperty().multiply(0.3));
        label2.prefHeightProperty().bind(scene2.heightProperty().multiply(0.7));

        label3.prefWidthProperty().bind(scene2.widthProperty().multiply(0.2));
        label3.prefHeightProperty().bind(scene2.heightProperty().multiply(0.07));

        label4.prefWidthProperty().bind(scene2.widthProperty().multiply(0.2));
        label4.prefHeightProperty().bind(scene2.heightProperty().multiply(0.07));

        label5.prefWidthProperty().bind(scene2.widthProperty().multiply(0.2));
        label5.prefHeightProperty().bind(scene2.heightProperty().multiply(0.07));


        // ---------------Scene 3--------------------------------------------------------------------- Scene 3
        // ---------------------------------------------
        // Adds all the Elements in Scene 3
        Button button4 = new Button();
        button4.setId("button4");

        Button button6 = new Button("Create own Lobby");
        button6.setId("button6");

        StackPane root3 = new StackPane(button4, button6);
        root3.setId("pane3");

        // ---------------------------------------------
        // Position of all elements in Scene 3
        button4.translateXProperty().bind(root3.widthProperty().multiply(-0.45));
        button4.translateYProperty().bind(root3.heightProperty().multiply(-0.43));

        button6.translateXProperty().bind(root3.widthProperty().multiply(0));
        button6.translateYProperty().bind(root3.heightProperty().multiply(0.3));

        // ---------------------------------------------
        // Font Size of all the elements in Scene 3
        button4.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root3.widthProperty().multiply(0.01),
                ";"
        ));

        button6.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root3.widthProperty().multiply(0.01),
                ";"
        ));

        // ---------------------------------------------
        // Scene 3

        scene3 = new Scene(root3, 800, 600);
        scene3.getStylesheets().add(getClass().
                getResource("style1.css").toExternalForm());

        // ---------------------------------------------
        // Size of all elements in Scene 3
        button4.prefWidthProperty().bind(scene3.widthProperty().multiply(0.07));
        button4.prefHeightProperty().bind(scene3.heightProperty().multiply(0.1));

        button6.prefWidthProperty().bind(scene3.widthProperty().multiply(0.15));
        button6.prefHeightProperty().bind(scene3.heightProperty().multiply(0.05));


        // ---------------Scene 4--------------------------------------------------------------------- Scene 4
        // ---------------------------------------------
        // Adds all the Elements in Scene 4


        StackPane root4 = new StackPane();
        root4.setId("pane4");

        // ---------------------------------------------
        // Position of all elements in Scene 4


        // ---------------------------------------------
        // Font Size of all elements in Scene 4

        // ---------------------------------------------
        // Scene 4
        scene4 = new Scene(root4, 800, 600);
        scene4.getStylesheets().add(getClass().
                getResource("style1.css").toExternalForm());

        // ---------------------------------------------
        // Size of all elements in Scene 4



        // ---------------Scene 5----------------------------------------------------------- Scene 5
        // ---------------------------------------------
        // Adds all the Elements in Scene 5
        Image image2 = new Image(
                getClass().getResource(
                        "/com/matti/battleship/picture3.jpg")
                            .toExternalForm()
        );
        ImageView imageView2 = new ImageView(image2);

        Button button7 = new Button();
        button7.setId("button7");

        Button button8 = new Button("Start Game");
        button8.setId("button8");

        ComboBox<String> combo2 = new ComboBox<>();
        combo2.getItems().addAll("Easy", "Medium", "Hard");
        combo2.setValue("Medium");

        TextField tf3 = new TextField();
        tf3.setPromptText("Type in amount of boats");

        TextField tf4 = new TextField();
        tf4.setPromptText("Type in field size");

        Label label6 = new Label("Game Settings");
        label6.setId("label6");
        label6.setAlignment(Pos.TOP_CENTER);

        StackPane root5 = new StackPane(imageView2, label6, button7, button8, combo2, tf3, tf4);
        root5.setId("pane4");

        // ---------------------------------------------
        // Position of all elements in Scene 5
        imageView2.translateXProperty().bind(root5.widthProperty().multiply(0.25));
        imageView2.translateYProperty().bind(root5.heightProperty().multiply(0.0));

        button7.translateXProperty().bind(root5.widthProperty().multiply(-0.45)); // links 25%
        button7.translateYProperty().bind(root5.heightProperty().multiply(-0.43));

        button8.translateXProperty().bind(root5.widthProperty().multiply(-0.255));
        button8.translateYProperty().bind(root5.heightProperty().multiply(0.3));

        combo2.translateXProperty().bind(root5.widthProperty().multiply(-0.255));
        combo2.translateYProperty().bind(root5.heightProperty().multiply(-0.15));

        tf3.translateXProperty().bind(root5.widthProperty().multiply(-0.255));
        tf3.translateYProperty().bind(root5.heightProperty().multiply(0.00));

        tf4.translateXProperty().bind(root5.widthProperty().multiply(-0.255));
        tf4.translateYProperty().bind(root5.heightProperty().multiply(0.15));

        label6.translateXProperty().bind(root5.widthProperty().multiply(-0.255));
        label6.translateYProperty().bind(root5.heightProperty().multiply(0.05));


        // ---------------------------------------------
        // Font Size of all elements in Scene 5
        button7.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root5.widthProperty().multiply(0.01),
                ";"
        ));

        button8.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root5.widthProperty().multiply(0.01),
                ";"
        ));

        combo2.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root5.widthProperty().multiply(0.01),
                ";"
        ));

        label6.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root5.widthProperty().multiply(0.03),
                ";"
        ));

        // ---------------------------------------------
        // Scene 5
        scene5 = new Scene(root5, 800, 600);
        scene5.getStylesheets().add(getClass().getResource("style1.css").toExternalForm());

        // ---------------------------------------------
        // Size of all elements in Scene 5
        imageView2.fitWidthProperty().bind(scene5.widthProperty().multiply(0.5));
        imageView2.fitHeightProperty().bind(scene5.heightProperty().multiply(1));

        button7.prefWidthProperty().bind(scene5.widthProperty().multiply(0.07));
        button7.prefHeightProperty().bind(scene5.heightProperty().multiply(0.1));

        button8.prefWidthProperty().bind(scene5.widthProperty().multiply(0.15));
        button8.prefHeightProperty().bind(scene5.heightProperty().multiply(0.05));

        combo2.prefWidthProperty().bind(scene5.widthProperty().multiply(0.12));
        combo2.prefHeightProperty().bind(scene5.heightProperty().multiply(0.05));

        label6.prefWidthProperty().bind(scene5.widthProperty().multiply(0.3));
        label6.prefHeightProperty().bind(scene5.heightProperty().multiply(0.7));

        tf3.prefWidthProperty().bind(scene5.widthProperty().multiply(0.1));
        tf3.setMinWidth(80);
        tf3.setMaxWidth(200);
        tf3.prefHeightProperty().bind(scene5.heightProperty().multiply(0.05));

        tf4.prefWidthProperty().bind(scene5.widthProperty().multiply(0.1));
        tf4.setMinWidth(80);
        tf4.setMaxWidth(200);
        tf4.prefHeightProperty().bind(scene5.heightProperty().multiply(0.05));


        // ---------------Button-Action-Events-----------------------------------------------Button-Action-Events
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

        button6.setOnAction(e -> {
            primaryStage.setScene(scene5);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });

        button7.setOnAction(e -> {
            primaryStage.setScene(scene3);
            Platform.runLater(() -> primaryStage.setFullScreen(true));
        });


        // ---------------Stage Setup--------------------------------------------------------------Stage Setup

        primaryStage.setTitle("Battleship");

        Image icon = new Image(
                getClass().getResource(
                        "/com/matti/battleship/Icon.png")
                        .toExternalForm()
        );

        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.show();
        Platform.runLater(() -> primaryStage.setFullScreen(true));
        primaryStage.setFullScreenExitHint("");
    }

    public static void main(String[] args) {
        launch(args);
    }
}