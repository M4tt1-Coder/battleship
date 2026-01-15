package com.matti.battleship.types;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Convenience wrapper around JavaFX {@link Label} that provides helper methods for responsive
 * layout.
 *
 * <p>The helper methods bind this label's properties (position, font size, preferred size) to a
 * parent {@link Pane}'s width and height, allowing the label to scale with the window size.
 *
 * @author Thomas Weigl
 */
public class Labels extends Label {

  /**
   * Creates a label with the given text.
   *
   * @param text the label text
   * @throws IllegalArgumentException if {@code text} is null
   */
  public Labels(String text) {
    super(text);
    if (text == null) {
      throw new IllegalArgumentException("text must not be null");
    }
  }

  /**
   * Binds the label's translation (offset) to a percentage of the parent's size.
   *
   * @param root the parent pane whose size is used for binding
   * @param positionX the horizontal multiplier of {@code root.width}
   * @param positionY the vertical multiplier of {@code root.height}
   * @throws IllegalArgumentException if {@code root} is null
   */
  public void position(Pane root, double positionX, double positionY) {
    if (root == null) {
      throw new IllegalArgumentException("root must not be null");
    }
    this.translateXProperty().bind(root.widthProperty().multiply(positionX));
    this.translateYProperty().bind(root.heightProperty().multiply(positionY));
  }

  /**
   * Binds the label's CSS font size to a percentage of the parent's width.
   *
   * @param root the parent pane whose width is used for binding
   * @param fontsize the multiplier of {@code root.width} used as pixel font size
   * @throws IllegalArgumentException if {@code root} is null
   */
  public void fontsize(Pane root, double fontsize) {
    if (root == null) {
      throw new IllegalArgumentException("root must not be null");
    }
    this.styleProperty()
        .bind(Bindings.concat("-fx-font-size: ", root.widthProperty().multiply(fontsize), ";"));
  }

  /**
   * Binds the label's preferred width and height to a percentage of the parent's size.
   *
   * @param root the parent pane whose size is used for binding
   * @param width the multiplier of {@code root.width} for {@code prefWidth}
   * @param height the multiplier of {@code root.height} for {@code prefHeight}
   * @throws IllegalArgumentException if {@code root} is null
   */
  public void size(Pane root, double width, double height) {
    if (root == null) {
      throw new IllegalArgumentException("root must not be null");
    }
    this.prefWidthProperty().bind(root.widthProperty().multiply(width));
    this.prefHeightProperty().bind(root.heightProperty().multiply(height));
  }
}
