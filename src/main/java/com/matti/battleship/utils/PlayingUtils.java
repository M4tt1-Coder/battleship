package com.matti.battleship.utils;

import com.matti.battleship.types.Labels;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class PlayingUtils {
  /**
   * Generates a random integer between 0 and 99.
   *
   * @return A random integer between 0 and 99.
   */
  public static int getRandomInt() {
    return (int) (Math.random() * 100);
  }

  public static void show_pop_up_information(Pane root, String text, int duration) {
    System.out.println("We are here");
    Labels popup = new Labels(text);
    popup.setId("label_pop_up_information");
    popup.setVisible(false);

    root.getChildren().add(popup);
    StackPane.setAlignment(popup, Pos.CENTER);

    popup.setOpacity(0);
    popup.setVisible(true);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), popup);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);
    fadeIn.play();

    PauseTransition wait = new PauseTransition(Duration.seconds(duration));
    wait.setOnFinished(
        e -> {
          FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popup);
          fadeOut.setFromValue(1);
          fadeOut.setToValue(0);
          fadeOut.setOnFinished(ev -> popup.setVisible(false));
          fadeOut.play();
        });
    wait.play();
  }
}
