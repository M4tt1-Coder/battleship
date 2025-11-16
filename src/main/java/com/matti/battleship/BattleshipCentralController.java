package com.matti.battleship;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BattleshipCentralController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
