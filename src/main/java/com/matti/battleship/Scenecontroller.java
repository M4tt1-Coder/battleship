package com.matti.battleship;

import com.matti.battleship.types.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Scenecontroller extends Application {

  private Scene scene1;

  @Override
  public void start(Stage primaryStage) {

    // ---------------root 1---------------------------------------------------------------------
    // root1

    // Names are ordered: <type of object><root><number>[<extra information>] like button11
    // b = back button

    // Adds all the Elements in root1
    Buttons button11 = new Buttons("Singleplayer");
    button11.setId("button11");

    Buttons button12 = new Buttons("Multiplayer");
    button12.setId("button12");

    Labels label11 = new Labels("Battleship");
    label11.setId("label11");

    // Adds StackPane for root1
    StackPane root1 = new StackPane(button11, button12, label11);
    root1.setId("pane1");

    // Sets properties of different objects
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
    // root2

    // Adds all the Elements in root2
    Image image21 =
        new Image(getClass().getResource("/com/matti/battleship/picture2.jpg").toExternalForm());
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

    // hier weitere labels einfügen

    // Adds StackPane for root2
    StackPane root2 =
        new StackPane(imageview21, button21b, label21, button22, tf21, tf22, combobox21);
    root2.setId("pane2");

    // Sets properties of different objects
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

    tf22.position(root2, -0.255, 0.15);
    tf22.fontsize(root2, 0.01);
    tf22.size(root2, 0.12, 0.05);

    label21.position(root2, -0.255, 0.05);
    label21.fontsize(root2, 0.03);
    label21.size(root2, 0.3, 0.7);
    label21.setAlignment(Pos.TOP_CENTER);

    // ---------------root 3---------------------------------------------------------------------
    // root3

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
    // root4

    StackPane root4 = new StackPane();
    root4.setId("pane4");

    // ---------------root 5---------------------------------------------------------------------
    // root5

    // ---------------button_actions--------------------------------------------------------------------- button_actions
    button11.setOnAction(
        e -> {
          scene1.setRoot(root2);
        });

    button12.setOnAction(
        e -> {
          scene1.setRoot(root3);
        });

    button21b.setOnAction(
        e -> {
          scene1.setRoot(root1);
        });

    button31b.setOnAction(
        e -> {
          scene1.setRoot(root1);
        });

    // ---------------Stage Setup--------------------------------------------------------------Stage
    // Setup
    scene1 = new Scene(root1, 800, 600);
    scene1.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

    primaryStage.setTitle("Battleship");
    Image icon =
        new Image(getClass().getResource("/com/matti/battleship/Icon.png").toExternalForm());
    primaryStage.getIcons().add(icon);
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
