package com.matti.battleship.types;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.Pane;
import javafx.scene.control.ComboBox;


public class ComboBoxes extends ComboBox<String> {

    void set_selections (String initial,String... possibilities) {
        for (String possibility : possibilities) {
            this.getItems().add(possibility);
        }
        this.setValue(initial);
    }

    void position (Pane root, double positionX, double positionY) {
        this.translateXProperty().bind(root.widthProperty().multiply(positionX));
        this.translateYProperty().bind(root.heightProperty().multiply(positionY));
    }


    void fontsize (Pane root, double fontsize) {
        this.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                root.widthProperty().multiply(fontsize),
                ";"
        ));
    }

    void size(Pane root, double width, double height) {
        this.prefWidthProperty().bind(root.widthProperty().multiply(width));
        this.prefHeightProperty().bind(root.heightProperty().multiply(height));
    }
}
