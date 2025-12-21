package com.matti.battleship.types;


import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Images extends ImageView {

    // returns a image object
    public static ImageView createImage(String imageName) {
        Image image = new Image(Images.class.getResource(imageName).toExternalForm());
        ImageView imageView = new ImageView(image);
        return imageView;
    }

    void position (Pane root, double positionX, double positionY) {
        this.translateXProperty().bind(root.widthProperty().multiply(positionX));
        this.translateYProperty().bind(root.heightProperty().multiply(positionY));
    }

    void size(Pane root, double width, double height) {
        this.fitWidthProperty().bind(root.widthProperty().multiply(width));
        this.fitHeightProperty().bind(root.heightProperty().multiply(height));
    }
}

