package com.matti.battleship.types;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

/**
 * Convenience wrapper around JavaFX {@link ComboBox} specialized for {@link String} values that
 * provides helper methods for responsive layout.
 *
 * <p>The helper methods bind this combo box's properties (position, font size, preferred size) to a
 * parent {@link Pane}'s width/height, allowing the control to scale proportionally with the window.
 *
 * @author Thomas Weigl
 */
public class ComboBoxes extends ComboBox<String> {

  /** Creates an empty {@code ComboBox<String>}. */
  public ComboBoxes() {
    super();
  }

  /**
   * Initializes the combo box with a predefined set of selectable values and sets the initial
   * selection.
   *
   * @param initial the value that should be selected initially
   * @param possibilities the list of selectable values
   * @throws IllegalArgumentException if {@code initial} or {@code possibilities} is null
   */
  public void set_selections(String initial, String... possibilities) {
    if (initial == null || possibilities == null) {
      throw new IllegalArgumentException("initial and possibilities must not be null");
    }
    for (String possibility : possibilities) {
      this.getItems().add(possibility);
    }
    this.setValue(initial);
  }

  /**
   * Binds the combo box's translation (offset) to a percentage of the parent's size.
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
   * Binds the combo box's CSS font size to a percentage of the parent's width.
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
   * Binds the combo box's preferred width and height to a percentage of the parent's size.
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
