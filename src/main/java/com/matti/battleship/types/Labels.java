package com.matti.battleship.types;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;


public class Labels extends Label {

    public Labels(String text) {
        super(text);
    }

    public void position (Pane root, double positionX, double positionY) {
        this.translateXProperty().bind(root.widthProperty().multiply(positionX));
        this.translateYProperty().bind(root.heightProperty().multiply(positionY));
    }


    public void fontsize (Pane root, double fontsize) {
        this.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root.widthProperty().multiply(fontsize),
                ";"
        ));
    }

    public void size(Pane root, double width, double height) {
        this.prefWidthProperty().bind(root.widthProperty().multiply(width));
        this.prefHeightProperty().bind(root.heightProperty().multiply(height));
    }


}
