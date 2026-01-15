package com.matti.battleship.types;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/**
 * Convenience wrapper around JavaFX {@link Button} that provides helper methods for responsive
 * layout.
 *
 * <p>The helper methods bind this button's properties (position, font size, preferred size) to a
 * parent {@link Pane}'s width/height, so the UI scales with the window.
 *
 *
 * @author Thomas Weigl
 */
public class Buttons extends Button {

    /**
     * Creates a button with the given text.
     *
     * @param text the button text
     * @throws IllegalArgumentException if {@code text} is null
     */
    public Buttons(String text) {
        super(text);
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
    }

    /** Creates an empty button (no text). */
    public Buttons() {
        super();
    }

    /**
     * Binds the button's translation (offset) to a percentage of the parent's size.
     *
     * <p>{@code positionX} is multiplied by {@code root.width}. {@code positionY} is multiplied by
     * {@code root.height}.
     *
     * <p>Example: {@code position(root, -0.4, -0.43)} moves the button left/up relative to the center.
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
     * Binds the button's CSS font size to a percentage of the parent's width.
     *
     * <p>Internally binds {@code -fx-font-size} to {@code root.width * fontsize}.
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
     * Binds the button's preferred width and height to a percentage of the parent's size.
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