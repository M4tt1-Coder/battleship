package com.matti.battleship.types;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * Convenience wrapper around JavaFX {@link ImageView} that provides helper methods for responsive
 * layout.
 *
 * <p>The helper methods bind this image view's properties (position and size) to a parent {@link
 * Pane}'s width/height, allowing the image to scale and move proportionally with the window.
 *
 * @author Thomas Weigl
 */
public class ImageViews extends ImageView {

  /**
   * Creates an {@code ImageView} displaying the given image.
   *
   * @param img the image to be displayed
   * @throws IllegalArgumentException if {@code img} is null
   */
  public ImageViews(Image img) {
    super(img);
    if (img == null) {
      throw new IllegalArgumentException("img must not be null");
    }
  }

  /**
   * Binds the image view's translation (offset) to a percentage of the parent's size.
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
   * Binds the image view's fit width and height to a percentage of the parent's size.
   *
   * @param root the parent pane whose size is used for binding
   * @param width the multiplier of {@code root.width} for {@code fitWidth}
   * @param height the multiplier of {@code root.height} for {@code fitHeight}
   * @throws IllegalArgumentException if {@code root} is null
   */
  public void size(Pane root, double width, double height) {
    if (root == null) {
      throw new IllegalArgumentException("root must not be null");
    }
    this.fitWidthProperty().bind(root.widthProperty().multiply(width));
    this.fitHeightProperty().bind(root.heightProperty().multiply(height));
  }
}
