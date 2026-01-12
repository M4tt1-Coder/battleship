package com.matti.battleship;

import com.matti.battleship.types.Buttons;
import com.matti.battleship.types.ComboBoxes;
import com.matti.battleship.types.ImageViews;
import com.matti.battleship.types.Labels;
import com.matti.battleship.types.TextFields;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Chat extends Application {

    private Scene scene1;

    int selected_field_size = 10;
    int selected_amount_of_boats = 5;

    // ausgewähltes Schiff für Rotation (Taste R)
    private StackPane selectedShip = null;

    // aktuelle Zellgröße (für Rotation)
    private double currentCellSize = 40;

    @Override
    public void start(Stage primaryStage) {

        // ---------------root 1---------------------------------------------------------------------
        Buttons button11 = new Buttons("Singleplayer");
        button11.setId("button11");

        Buttons button12 = new Buttons("Multiplayer");
        button12.setId("button12");

        Labels label11 = new Labels("Battleship");
        label11.setId("label11");

        StackPane root1 = new StackPane(button11, button12, label11);
        root1.setId("pane1");

        button11.position(root1, -0.25, 0.25);
        button11.fontsize(root1, 0.02);
        button11.size(root1, 0.2, 0.1);

        button12.position(root1, 0.25, 0.25);
        button12.fontsize(root1, 0.02);
        button12.size(root1, 0.2, 0.1);

        label11.position(root1, 0.00, -0.45);
        label11.fontsize(root1, 0.05);
        label11.size(root1, 0.4, 0.2);
        label11.setAlignment(Pos.CENTER);

        // ---------------root 2---------------------------------------------------------------------
        Image image21 =
                new Image(getClass().getResource("/com/matti/battleship/picture2.jpg").toExternalForm());
        ImageViews imageview21 = new ImageViews(image21);

        Buttons button21b = new Buttons();
        button21b.setId("back_button");

        Buttons button22 = new Buttons("Start Game");
        button22.setId("button22");

        ComboBoxes combobox21 = new ComboBoxes();
        combobox21.setId("combobox21");

        TextFields tf21 = new TextFields();
        tf21.setId("tf21");
        tf21.setPromptText("Type in amount of boats");

        TextFields tf22 = new TextFields();
        tf22.setId("tf22");
        tf22.setPromptText("Type in field size");

        Labels label21 = new Labels("Settings");
        label21.setId("label21");

        StackPane root2 =
                new StackPane(imageview21, button21b, label21, button22, tf21, tf22, combobox21);
        root2.setId("pane2");

        imageview21.position(root2, 0.25, 0.00);
        imageview21.size(root2, 0.5, 1);

        button21b.position(root2, -0.45, -0.43);
        button21b.fontsize(root2, 0.01);
        button21b.size(root2, 0.07, 0.1);

        button22.position(root2, -0.255, 0.3);
        button22.fontsize(root2, 0.02);
        button22.size(root2, 0.15, 0.05);

        combobox21.set_selections("Medium", "Easy", "Medium", "Hard");
        combobox21.position(root2, -0.255, -0.15);
        combobox21.fontsize(root2, 0.01);
        combobox21.size(root2, 0.12, 0.05);

        tf21.position(root2, -0.255, 0.00);
        tf21.fontsize(root2, 0.01);
        tf21.size(root2, 0.12, 0.05);
        tf21.setMinWidth(80);
        tf21.setMaxWidth(200);

        tf22.position(root2, -0.255, 0.15);
        tf22.fontsize(root2, 0.01);
        tf22.size(root2, 0.12, 0.05);
        tf22.setMinWidth(80);
        tf22.setMaxWidth(200);

        label21.position(root2, -0.255, 0.05);
        label21.fontsize(root2, 0.03);
        label21.size(root2, 0.3, 0.7);
        label21.setAlignment(Pos.TOP_CENTER);

        // ---------------root 3---------------------------------------------------------------------
        Buttons button31b = new Buttons();
        button31b.setId("back_button");

        Buttons button32 = new Buttons("Start Game");
        button32.setId("button32");

        StackPane root3 = new StackPane(button31b, button32);
        root3.setId("pane3");

        button31b.position(root3, -0.45, -0.43);
        button31b.fontsize(root3, 0.01);
        button31b.size(root3, 0.07, 0.1);

        // ---------------root 4---------------------------------------------------------------------
        Buttons button41e = new Buttons();
        button41e.setId("end_game_button");

        Buttons button42 = new Buttons("Start");
        button42.setId("button11");

        Labels label41 = new Labels("Select the position of you boats");
        label41.setId("label21");

        StackPane root4 = new StackPane(button41e, label41, button42);
        root4.setId("pane4");

        button41e.position(root4, -0.4, -0.43);
        button41e.fontsize(root4, 0.01);
        button41e.size(root4, 0.15, 0.06);

        button42.position(root4, 0.25, 0.4);
        button42.fontsize(root4, 0.02);
        button42.size(root4, 0.15, 0.05);

        label41.position(root4, 0, -0.01);
        label41.fontsize(root4, 0.03);
        label41.size(root4, 0.6, 0.8);
        label41.setAlignment(Pos.TOP_CENTER);

        // -------------------- START GAME -> root4: Grid + Dock + Board-Schiffe verschiebbar
        button22.setOnAction(
                e -> {
                    if (!tf22.getText().isEmpty()) {
                        try {
                            selected_field_size = Integer.parseInt(tf22.getText());
                        } catch (NumberFormatException ex) {
                            System.out.println("Ungültige Feldgröße, Standardwert 10");
                        }
                    }

                    if (!tf21.getText().isEmpty()) {
                        try {
                            selected_amount_of_boats = Integer.parseInt(tf21.getText());
                        } catch (NumberFormatException ex) {
                            System.out.println("Ungültige Bootanzahl, Standardwert 5");
                        }
                    }

                    double BOARD_SIZE = 400;
                    double cellSize = BOARD_SIZE / selected_field_size;
                    currentCellSize = cellSize;
                    selectedShip = null;

                    // alte dynamische Nodes entfernen
                    root4.getChildren().removeIf(n -> "dynamic".equals(n.getId()));

                    // --- 1) Grid nur als Hintergrund
                    GridPane battleGrid = new GridPane();
                    battleGrid.setPrefSize(BOARD_SIZE, BOARD_SIZE);
                    battleGrid.setMaxSize(BOARD_SIZE, BOARD_SIZE);
                    battleGrid.setStyle("-fx-background-color: transparent;");

                    for (int row = 0; row < selected_field_size; row++) {
                        for (int col = 0; col < selected_field_size; col++) {
                            StackPane cell = new StackPane();
                            cell.setPrefSize(cellSize, cellSize);
                            cell.setMinSize(cellSize, cellSize);
                            cell.setMaxSize(cellSize, cellSize);
                            cell.setStyle("-fx-border-color: black;-fx-background-color: lightblue;");
                            battleGrid.add(cell, col, row);
                        }
                    }

                    // --- 2) shipLayer: Schiffe liegen darüber (Grid verschiebt sich nicht)
                    Pane shipLayer = new Pane();
                    shipLayer.setPrefSize(BOARD_SIZE, BOARD_SIZE);
                    shipLayer.setMinSize(BOARD_SIZE, BOARD_SIZE);
                    shipLayer.setMaxSize(BOARD_SIZE, BOARD_SIZE);
                    shipLayer.setStyle("-fx-background-color: transparent;");

                    StackPane boardLayer = new StackPane(battleGrid, shipLayer);
                    boardLayer.setId("dynamic");

                    // shipLayer akzeptiert Drag
                    shipLayer.setOnDragOver(
                            ev -> {
                                Dragboard db = ev.getDragboard();
                                if (db.hasString() && db.getString().startsWith("SHIP:")) {
                                    ev.acceptTransferModes(TransferMode.MOVE);
                                }
                                ev.consume();
                            });

                    // Drop: entweder neues Schiff aus Dock, oder vorhandenes Schiff verschieben
                    shipLayer.setOnDragDropped(
                            ev -> {
                                Dragboard db = ev.getDragboard();
                                if (!(db.hasString() && db.getString().startsWith("SHIP:"))) {
                                    ev.setDropCompleted(false);
                                    ev.consume();
                                    return;
                                }

                                // Format: "SHIP:<len>:<resourcePath>"
                                String[] parts = db.getString().split(":", 3);
                                int len = 2;
                                String imgPath = "";

                                try { len = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                                if (parts.length >= 3) imgPath = parts[2];

                                // Drop Position -> auf Zelle snappen
                                double x = ev.getX();
                                double y = ev.getY();
                                int col = (int) (x / cellSize);
                                int row = (int) (y / cellSize);

                                double snappedX = col * cellSize;
                                double snappedY = row * cellSize;

                                Node src = (Node) ev.getGestureSource();

                                // Fall A: Schiff vom Board wird verschoben
                                if (src instanceof StackPane && Boolean.TRUE.equals(src.getProperties().get("onBoard"))) {
                                    StackPane movingShip = (StackPane) src;
                                    movingShip.setLayoutX(snappedX);
                                    movingShip.setLayoutY(snappedY);
                                    movingShip.toFront();
                                    ev.setDropCompleted(true);
                                    ev.consume();
                                    return;
                                }

                                // Fall B: neues Schiff aus Dock
                                boolean vertical = false; // immer horizontal platzieren, Rotation später per R
                                StackPane placedShip = createPlacedShipNode(len, cellSize, vertical, imgPath);

                                placedShip.setLayoutX(snappedX);
                                placedShip.setLayoutY(snappedY);

                                // auswählen per Klick (für Rotation)
                                placedShip.setOnMouseClicked(
                                        click -> {
                                            if (selectedShip != null) {
                                                selectedShip.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");
                                            }
                                            selectedShip = placedShip;
                                            selectedShip.setStyle("-fx-border-color: red; -fx-border-width: 2;");
                                        });

                                // Board-Schiff wieder verschiebbar machen
                                enableBoardDragging(placedShip);

                                shipLayer.getChildren().add(placedShip);

                                // Dock-Schiff entfernen (kein Rest, kein Text)
                                if (src != null && src.getParent() instanceof VBox) {
                                    ((VBox) src.getParent()).getChildren().remove(src);
                                }

                                ev.setDropCompleted(true);
                                ev.consume();
                            });

                    // --- 3) Dock links: mehrere Schiffe (BILDER)
                    VBox shipDock = new VBox(10);
                    shipDock.setId("dynamic");
                    shipDock.setPadding(new Insets(12));
                    shipDock.setMaxWidth(260);

                    // HIER deine Bildnamen/Dateien:
                    // Lege diese Dateien in resources an:
                    // /com/matti/battleship/ship_len2.png
                    // /com/matti/battleship/ship_len3.png
                    // /com/matti/battleship/ship_len4.png
                    // /com/matti/battleship/ship_len5.png
                    String img2 = "/com/matti/battleship/ship_len2.png";
                    String img3 = "/com/matti/battleship/ship_len3.png";
                    String img4 = "/com/matti/battleship/ship_len4.png";
                    String img5 = "/com/matti/battleship/ship_len5.png";

                    // Anzahl Schiffe: zyklisch 2,3,4,5
                    List<Integer> lengths = new ArrayList<>();
                    for (int i = 0; i < selected_amount_of_boats; i++) {
                        lengths.add(2 + (i % 4));
                    }

                    for (int l : lengths) {
                        String path = switch (l) {
                            case 2 -> img2;
                            case 3 -> img3;
                            case 4 -> img4;
                            default -> img5;
                        };

                        StackPane dockShip = createDockShipNode(l, cellSize, path);

                        dockShip.setOnDragDetected(
                                ev2 -> {
                                    Dragboard db2 = dockShip.startDragAndDrop(TransferMode.MOVE);
                                    ClipboardContent content = new ClipboardContent();
                                    content.putString("SHIP:" + l + ":" + path);
                                    db2.setContent(content);
                                    ev2.consume();
                                });

                        shipDock.getChildren().add(dockShip);
                    }

                    root4.getChildren().addAll(boardLayer, shipDock);
                    StackPane.setAlignment(boardLayer, Pos.CENTER);
                    StackPane.setAlignment(shipDock, Pos.CENTER_LEFT);
                    StackPane.setMargin(shipDock, new Insets(0, 0, 0, 10));

                    scene1.setRoot(root4);

                    // Fokus für Taste R
                    root4.setFocusTraversable(true);
                    root4.requestFocus();
                });

        // ---------------root 5---------------------------------------------------------------------
        StackPane root5 = new StackPane();
        root5.setId("pane5");

        button42.setOnAction(
                e -> {
                    double BOARD_SIZE = 400;
                    double BUTTON_SIZE = BOARD_SIZE / selected_field_size;
                    GridPane grid = new GridPane();
                    grid.setHgap(0);
                    grid.setVgap(0);
                    grid.setPadding(new Insets(12));

                    for (int r = 0; r < selected_field_size; r++) {
                        for (int c = 0; c < selected_field_size; c++) {
                            Buttons btn = new Buttons(r + "," + c);
                            btn.setStyle("-fx-background-radius: 0; -fx-border-radius: 0;");
                            btn.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
                            final int rr = r;
                            final int cc = c;
                            btn.setOnAction(ev -> System.out.println("Clicked: row=" + rr + " col=" + cc));
                            grid.add(btn, c, r);
                        }
                    }

                    root5.getChildren().addAll(grid);
                    grid.setAlignment(Pos.CENTER);

                    scene1.setRoot(root5);
                });

        // ---------------button_actions---------------------------------------------------------------------
        button11.setOnAction(e -> scene1.setRoot(root2));
        button12.setOnAction(e -> scene1.setRoot(root3));
        button21b.setOnAction(e -> scene1.setRoot(root1));
        button31b.setOnAction(e -> scene1.setRoot(root1));
        button41e.setOnAction(e -> scene1.setRoot(root1));

        // ---------------Stage Setup--------------------------------------------------------------
        scene1 = new Scene(root1, 800, 600);
        scene1.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        // Taste R: rotiert das ausgewählte Schiff (ohne Checks)
        scene1.setOnKeyPressed(
                ke -> {
                    if (ke.getCode() == KeyCode.R) {
                        rotateSelectedShipSimple();
                    }
                });

        primaryStage.setTitle("Battleship");
        Image icon =
                new Image(getClass().getResource("/com/matti/battleship/Icon.png").toExternalForm());
        primaryStage.getIcons().add(icon);
        primaryStage.setScene(scene1);
        primaryStage.show();
    }

    // ---- Board-Schiff dragbar machen (verschieben)
    private void enableBoardDragging(StackPane ship) {
        ship.setPickOnBounds(true);
        ship.getProperties().put("onBoard", true);

        ship.setOnDragDetected(
                ev -> {
                    Object lenObj = ship.getProperties().get("len");
                    Object pathObj = ship.getProperties().get("imgPath");

                    int len = (lenObj instanceof Integer) ? (Integer) lenObj : 2;
                    String path = (pathObj instanceof String) ? (String) pathObj : "";

                    Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("SHIP:" + len + ":" + path);
                    db.setContent(content);
                    ev.consume();
                });
    }

    // ---- Rotation (ohne Checks): horizontal <-> vertikal (nur Größe ändern)
    private void rotateSelectedShipSimple() {
        if (selectedShip == null) return;

        Object lenObj = selectedShip.getProperties().get("len");
        Object vertObj = selectedShip.getProperties().get("vertical");
        Object ivObj = selectedShip.getProperties().get("iv");
        Object rectObj = selectedShip.getProperties().get("rect");

        if (!(lenObj instanceof Integer) || !(vertObj instanceof Boolean)) return;

        int len = (Integer) lenObj;
        boolean vertical = (Boolean) vertObj;
        boolean newVertical = !vertical;

        selectedShip.getProperties().put("vertical", newVertical);

        double w = newVertical ? currentCellSize : (currentCellSize * len);
        double h = newVertical ? (currentCellSize * len) : currentCellSize;

        if (ivObj instanceof ImageView) {
            ImageView iv = (ImageView) ivObj;
            iv.setFitWidth(w);
            iv.setFitHeight(h);
        }
        if (rectObj instanceof Rectangle) {
            Rectangle r = (Rectangle) rectObj;
            r.setWidth(w);
            r.setHeight(h);
        }
    }

    // ---- Dock-Schiff: Bild (wenn vorhanden), sonst Placeholder
    private StackPane createDockShipNode(int len, double cellSize, String resourcePath) {
        double w = cellSize * len;
        double h = cellSize;

        Image img = tryLoad(resourcePath);

        StackPane p = new StackPane();
        p.setPickOnBounds(true);

        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(w);
            iv.setFitHeight(h);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            p.getChildren().add(iv);
            return p;
        }

        Rectangle r = new Rectangle(w, h, Color.DARKGRAY);
        r.setArcWidth(8);
        r.setArcHeight(8);
        p.getChildren().add(r);
        return p;
    }

    // ---- Platziertes Schiff: Bild (wenn vorhanden), sonst Placeholder
    private StackPane createPlacedShipNode(int len, double cellSize, boolean vertical, String resourcePath) {
        double w = vertical ? cellSize : (cellSize * len);
        double h = vertical ? (cellSize * len) : cellSize;

        Image img = tryLoad(resourcePath);

        StackPane ship = new StackPane();
        ship.setPickOnBounds(true);

        ship.getProperties().put("len", len);
        ship.getProperties().put("vertical", vertical);
        ship.getProperties().put("imgPath", resourcePath);

        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(w);
            iv.setFitHeight(h);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            ship.getChildren().add(iv);
            ship.getProperties().put("iv", iv);
        } else {
            Rectangle r = new Rectangle(w, h, Color.GRAY);
            r.setArcWidth(10);
            r.setArcHeight(10);
            ship.getChildren().add(r);
            ship.getProperties().put("rect", r);
        }

        // direkt verschiebbar machen
        enableBoardDragging(ship);

        return ship;
    }

    // ---- Image aus resources laden (oder null)
    private Image tryLoad(String resourcePath) {
        try {
            if (resourcePath != null && getClass().getResource(resourcePath) != null) {
                return new Image(getClass().getResource(resourcePath).toExternalForm());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}