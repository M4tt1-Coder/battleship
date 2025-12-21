package com.matti.battleship.types;


import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageViews extends ImageView {

    void position (Pane root, double positionX, double positionY) {
        this.translateXProperty().bind(root.widthProperty().multiply(positionX));
        this.translateYProperty().bind(root.heightProperty().multiply(positionY));
    }

    void size(Pane root, double width, double height) {
        this.fitWidthProperty().bind(root.widthProperty().multiply(width));
        this.fitHeightProperty().bind(root.heightProperty().multiply(height));
    }
}

