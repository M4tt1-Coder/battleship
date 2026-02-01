package com.matti.battleship;

import com.matti.battleship.IO.FileReaderService;
import com.matti.battleship.IO.FileWriterService;
import com.matti.battleship.IO.ResourceProfiler;
import com.matti.battleship.computer.Algorithm;
import com.matti.battleship.computer.PlacementAlgorithm;
import com.matti.battleship.enums.AIDifficulty;
import com.matti.battleship.enums.Direction;
import com.matti.battleship.enums.PlayerTurn;
import com.matti.battleship.enums.PlayingMode;
import com.matti.battleship.enums.Role;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.socket.config.EnvConfig;
import com.matti.battleship.socket.discovery.ClientDiscoveryScanner;
import com.matti.battleship.socket.discovery.DiscoveredServer;
import com.matti.battleship.types.*;
import com.matti.battleship.utils.BoardUtils;
import com.matti.battleship.utils.FieldUtils;
import com.matti.battleship.utils.GameUtils;
import com.matti.battleship.utils.GridPaneUtils;
import com.matti.battleship.utils.PlayingUtils;
import com.matti.battleship.utils.ShipUtils;
import com.matti.battleship.utils.datatypes.PlayerBoardCellContext;
import com.matti.battleship.utils.datatypes.PlayerBoardCellContext.FieldDisplayState;
import com.matti.battleship.utils.datatypes.ShipGridElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

// TODO: Refactor code -> extract indenpendent snippets into external functions

// TODO: Move all image loading to the 'ResourceProfiler' instance

// option + shift + f -> formatieren
public class BattleShipApp extends Application {

  private Scene scene1;
  private int selected_field_size = 10;

  // NEW: Boardgröße dynamisch (abhängig von Scene)
  private final DoubleProperty boardSize = new SimpleDoubleProperty(400);

  // OPTIONAL: falls du BOARD_SIZE weiter als "Default" behalten willst
  private double BOARD_SIZE = 400;

  // ----- Temporary Game -----
  private Game game;
  private PlayingMode playingMode;
  @Nullable private AIDifficulty difficulty;
  @Nullable private Algorithm aIAlgorithm;
  @Nullable private Role playerRole;

  // percentage rule ... 30% of the field must be occupied by ships
  private ShipLength[] initialShipSetup;

  // ----- Player -----
  private Board board;

  // ----- Game Logic -----

  @Override
  public void start(Stage primaryStage) {

    // ---------------root 1
    // ---------------------------------------------------------------------
    // root1

    Buttons buttonSingleplayerR1 = new Buttons("Singleplayer");
    buttonSingleplayerR1.setId("buttonSingleplayerR1");

    Buttons buttonMultiplayerR1 = new Buttons("Multiplayer");
    buttonMultiplayerR1.setId("buttonMultiplayerR1");

    StackPane root1GamemodeSelection = new StackPane(buttonSingleplayerR1, buttonMultiplayerR1);
    root1GamemodeSelection.setId("root1GamemodeSelection");

    // Layout root1:

    buttonSingleplayerR1.position(root1GamemodeSelection, -0.25, 0.35);
    buttonSingleplayerR1.fontsize(root1GamemodeSelection, 0.05);
    buttonSingleplayerR1.size(root1GamemodeSelection, 0.5, 0.3);

    buttonMultiplayerR1.position(root1GamemodeSelection, 0.25, 0.35);
    buttonMultiplayerR1.fontsize(root1GamemodeSelection, 0.05);
    buttonMultiplayerR1.size(root1GamemodeSelection, 0.5, 0.3);

    // ---------------root 2
    // ---------------------------------------------------------------------
    // root2

    Image imagePlayerVsAiR2 =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/player_vs_ai.png")
                .toExternalForm());
    ImageViews imageviewPlayerVsAiR2 = new ImageViews(imagePlayerVsAiR2);

    Buttons buttonGoBackR2 = new Buttons();
    buttonGoBackR2.setId("buttonsGoBack");

    Buttons buttonStartPlacingShipsR2 = new Buttons("Start Game");
    buttonStartPlacingShipsR2.setId("buttonsNormal");

    Buttons buttonLoadGameR2 = new Buttons("Load Game");
    buttonLoadGameR2.setId("buttonsSaveAndLoad");

    ComboBoxes comboboxesDifficultySelectionR2 = new ComboBoxes();
    comboboxesDifficultySelectionR2.setId("comboboxes");

    TextFields textfieldSelectFieldSizeR2 = new TextFields();
    textfieldSelectFieldSizeR2.setId("textfields");
    textfieldSelectFieldSizeR2.setPromptText("Type in field size");

    Labels labelSettingsR2 = new Labels("Settings");
    labelSettingsR2.setId("labelBackgroundBlueTopCenter");

    Labels labelTextSelectDifficultyR2 = new Labels("Difficulty:");
    labelTextSelectDifficultyR2.setId("labelsNormal");

    Labels labelTextSelectFieldSizeR2 = new Labels("Field Size:");
    labelTextSelectFieldSizeR2.setId("labelsNormal");

    StackPane root2SingleplayerSettings =
        new StackPane(
            imageviewPlayerVsAiR2,
            buttonGoBackR2,
            labelSettingsR2,
            buttonStartPlacingShipsR2,
            buttonLoadGameR2,
            textfieldSelectFieldSizeR2,
            comboboxesDifficultySelectionR2,
            labelTextSelectDifficultyR2,
            labelTextSelectFieldSizeR2);
    root2SingleplayerSettings.setId("root2SingleplayerSettings");

    imageviewPlayerVsAiR2.position(root2SingleplayerSettings, 0.25, 0.00);
    imageviewPlayerVsAiR2.size(root2SingleplayerSettings, 0.5, 1);

    buttonGoBackR2.position(root2SingleplayerSettings, -0.45, -0.43);
    buttonGoBackR2.fontsize(root2SingleplayerSettings, 0.01);
    buttonGoBackR2.size(root2SingleplayerSettings, 0.07, 0.1);

    buttonStartPlacingShipsR2.position(root2SingleplayerSettings, -0.325, 0.25);
    buttonStartPlacingShipsR2.fontsize(root2SingleplayerSettings, 0.013);
    buttonStartPlacingShipsR2.size(root2SingleplayerSettings, 0.13, 0.05);

    buttonLoadGameR2.position(root2SingleplayerSettings, -0.185, 0.25);
    buttonLoadGameR2.fontsize(root2SingleplayerSettings, 0.013);
    buttonLoadGameR2.size(root2SingleplayerSettings, 0.13, 0.05);

    comboboxesDifficultySelectionR2.set_selections("Medium", "Easy", "Medium", "Hard");
    comboboxesDifficultySelectionR2.position(root2SingleplayerSettings, -0.2, -0.05);
    comboboxesDifficultySelectionR2.fontsize(root2SingleplayerSettings, 0.01);
    comboboxesDifficultySelectionR2.size(root2SingleplayerSettings, 0.15, 0.05);

    textfieldSelectFieldSizeR2.position(root2SingleplayerSettings, -0.2, 0.1);
    textfieldSelectFieldSizeR2.fontsize(root2SingleplayerSettings, 0.01);
    textfieldSelectFieldSizeR2.size(root2SingleplayerSettings, 0.15, 0.05);

    labelSettingsR2.position(root2SingleplayerSettings, -0.255, 0.05);
    labelSettingsR2.fontsize(root2SingleplayerSettings, 0.04);
    labelSettingsR2.size(root2SingleplayerSettings, 0.3, 0.6);

    labelTextSelectDifficultyR2.position(root2SingleplayerSettings, -0.347, -0.05);
    labelTextSelectDifficultyR2.fontsize(root2SingleplayerSettings, 0.02);
    labelTextSelectDifficultyR2.size(root2SingleplayerSettings, 0.2, 0.07);

    labelTextSelectFieldSizeR2.position(root2SingleplayerSettings, -0.343, 0.1);
    labelTextSelectFieldSizeR2.fontsize(root2SingleplayerSettings, 0.02);
    labelTextSelectFieldSizeR2.size(root2SingleplayerSettings, 0.2, 0.07);

    // ---------------root 3
    // ---------------------------------------------------------------------
    // root3

    Buttons buttonGoBackR3 = new Buttons();
    buttonGoBackR3.setId("buttonsGoBack");

    Buttons buttonCreateOwnGameR3 = new Buttons("Create own Game");
    buttonCreateOwnGameR3.setId("buttonsNormal");

    Buttons buttonRefreshServersR3 = new Buttons("Refresh Servers");
    buttonRefreshServersR3.setId("buttonsNormal");

    Labels labelJoinOtherPlayersR3 = new Labels("Join other players");
    labelJoinOtherPlayersR3.setId("labelBackgroundBlueTopCenter");

    StackPane root3JoinOtherServers =
        new StackPane(
            buttonGoBackR3, labelJoinOtherPlayersR3, buttonCreateOwnGameR3, buttonRefreshServersR3);
    root3JoinOtherServers.setId("root3JoinOtherServers");

    buttonGoBackR3.position(root3JoinOtherServers, -0.45, -0.43);
    buttonGoBackR3.fontsize(root3JoinOtherServers, 0.01);
    buttonGoBackR3.size(root3JoinOtherServers, 0.07, 0.1);

    buttonCreateOwnGameR3.position(root3JoinOtherServers, 0.00, 0.3);
    buttonCreateOwnGameR3.fontsize(root3JoinOtherServers, 0.015);
    buttonCreateOwnGameR3.size(root3JoinOtherServers, 0.2, 0.07);

    buttonRefreshServersR3.position(root3JoinOtherServers, 0.25, -0.3);
    buttonRefreshServersR3.fontsize(root3JoinOtherServers, 0.01);
    buttonRefreshServersR3.size(root3JoinOtherServers, 0.15, 0.05);

    labelJoinOtherPlayersR3.position(root3JoinOtherServers, 0, 0.05);
    labelJoinOtherPlayersR3.fontsize(root3JoinOtherServers, 0.03);
    labelJoinOtherPlayersR3.size(root3JoinOtherServers, 0.7, 0.8);

    // ---------------root 4
    // ---------------------------------------------------------------------
    // root4
    Buttons buttonEndGameR4 = new Buttons("End Game");
    buttonEndGameR4.setId("buttonsEndGame");

    Buttons buttonStartShootingR4 = new Buttons("Start");
    buttonStartShootingR4.setId("buttonsNormal");

    Labels labelSelectPositionOfYourBoatsR4 = new Labels("Select the position of your boats");
    labelSelectPositionOfYourBoatsR4.setId("labelBackgroundBlueTopCenter");

    Labels labelShipsSpawnWhenBoardInitializedR4 = new Labels("");
    labelShipsSpawnWhenBoardInitializedR4.setId("labelsBackgroundBlueCenter");

    Labels labelPressRToRotateR4 = new Labels("Press R to Rotate");
    labelPressRToRotateR4.setId("labelsNormal");

    StackPane root4PlaceShips = new StackPane();
    root4PlaceShips.setId("root4PlaceShips");

    buttonEndGameR4.position(root4PlaceShips, -0.38, -0.43);
    buttonEndGameR4.fontsize(root4PlaceShips, 0.02);
    buttonEndGameR4.size(root4PlaceShips, 0.18, 0.07);

    buttonStartShootingR4.position(root4PlaceShips, 0.35, 0.3);
    buttonStartShootingR4.fontsize(root4PlaceShips, 0.02);
    buttonStartShootingR4.size(root4PlaceShips, 0.15, 0.05);

    labelSelectPositionOfYourBoatsR4.position(root4PlaceShips, 0.1, -0.01);
    labelSelectPositionOfYourBoatsR4.fontsize(root4PlaceShips, 0.03);
    labelSelectPositionOfYourBoatsR4.size(root4PlaceShips, 0.75, 0.8);

    labelShipsSpawnWhenBoardInitializedR4.position(root4PlaceShips, 0, 0.47);
    labelShipsSpawnWhenBoardInitializedR4.fontsize(root4PlaceShips, 0.03);
    labelShipsSpawnWhenBoardInitializedR4.size(root4PlaceShips, 1, 0.08);

    labelPressRToRotateR4.position(root4PlaceShips, 0.35, -0.2);
    labelPressRToRotateR4.fontsize(root4PlaceShips, 0.015);
    labelPressRToRotateR4.size(root4PlaceShips, 0.25, 0.1);

    // ---------------root 5
    // ---------------------------------------------------------------------
    // root5

    Buttons buttonEndGameR5 = new Buttons("End Game");
    buttonEndGameR5.setId("buttonsEndGame");

    Buttons buttonSafeGameR5 = new Buttons("Save Game");
    buttonSafeGameR5.setId("buttonsSaveAndLoad");

    Labels labelBackgroundShootingR5 = new Labels("");
    labelBackgroundShootingR5.setId("labelsBackgroundBlueCenter");

    Labels labelTextYourSideR5 = new Labels("Enemies field");
    labelTextYourSideR5.setId("labelsNormal");

    Labels labelTextEnemySideR5 = new Labels("Your field");
    labelTextEnemySideR5.setId("labelsNormal");

    StackPane root5ShootOnShips = new StackPane();
    root5ShootOnShips.setId("root5ShootOnShips");

    buttonEndGameR5.position(root5ShootOnShips, -0.38, -0.43);
    buttonEndGameR5.fontsize(root5ShootOnShips, 0.02);
    buttonEndGameR5.size(root5ShootOnShips, 0.18, 0.07);

    labelBackgroundShootingR5.position(root5ShootOnShips, 0, -0.01);
    labelBackgroundShootingR5.fontsize(root5ShootOnShips, 0.03);
    labelBackgroundShootingR5.size(root5ShootOnShips, 0.98, 0.7);

    labelTextYourSideR5.position(root5ShootOnShips, -0.25, -0.33);
    labelTextYourSideR5.fontsize(root5ShootOnShips, 0.03);
    labelTextYourSideR5.size(root5ShootOnShips, 0.3, 0.08);

    labelTextEnemySideR5.position(root5ShootOnShips, 0.25, -0.33);
    labelTextEnemySideR5.fontsize(root5ShootOnShips, 0.03);
    labelTextEnemySideR5.size(root5ShootOnShips, 0.3, 0.08);

    buttonSafeGameR5.position(root5ShootOnShips, 0.3, 0.4);
    buttonSafeGameR5.fontsize(root5ShootOnShips, 0.015);
    buttonSafeGameR5.size(root5ShootOnShips, 0.2, 0.08);

    // ---------------root 6
    // ---------------------------------------------------------------------
    // root6
    Image imagePlayerVsPlayerR6 =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/player_vs_player.png")
                .toExternalForm());
    ImageViews imageviewPlayerVsPlayerR6 = new ImageViews(imagePlayerVsPlayerR6);

    Buttons buttonGoBackR6 = new Buttons();
    buttonGoBackR6.setId("buttonsGoBack");

    Buttons buttonStartGameR6 = new Buttons("Start Game");
    buttonStartGameR6.setId("buttonsNormal");

    TextFields textfieldSelectFieldSizeR6 = new TextFields();
    textfieldSelectFieldSizeR6.setId("textfields");
    textfieldSelectFieldSizeR6.setPromptText("Type in field size");

    TextFields textfieldSelectServerNameR6 = new TextFields();
    textfieldSelectServerNameR6.setId("textfields");
    textfieldSelectServerNameR6.setPromptText("Type in server name");

    Labels labelSettingsR6 = new Labels("Settings");
    labelSettingsR6.setId("labelBackgroundBlueTopCenter");

    Labels labelTextSelectFieldSizeR6 = new Labels("Field Size:");
    labelTextSelectFieldSizeR6.setId("labelsNormal");

    Labels labelSelectServerNameR6 = new Labels("Server-\nName:");
    labelSelectServerNameR6.setId("labelsNormal");

    StackPane root6MultiplayerSettings =
        new StackPane(
            imageviewPlayerVsPlayerR6,
            buttonGoBackR6,
            labelSettingsR6,
            buttonStartGameR6,
            textfieldSelectFieldSizeR6,
            textfieldSelectServerNameR6,
            labelTextSelectFieldSizeR6,
            labelSelectServerNameR6);
    root6MultiplayerSettings.setId("root6MultiplayerSettings");

    imageviewPlayerVsPlayerR6.position(root6MultiplayerSettings, 0.25, 0.00);
    imageviewPlayerVsPlayerR6.size(root6MultiplayerSettings, 0.5, 1);

    buttonGoBackR6.position(root6MultiplayerSettings, -0.45, -0.43);
    buttonGoBackR6.fontsize(root6MultiplayerSettings, 0.01);
    buttonGoBackR6.size(root6MultiplayerSettings, 0.07, 0.1);

    buttonStartGameR6.position(root6MultiplayerSettings, -0.25, 0.25);
    buttonStartGameR6.fontsize(root6MultiplayerSettings, 0.013);
    buttonStartGameR6.size(root6MultiplayerSettings, 0.13, 0.05);

    textfieldSelectFieldSizeR6.position(root6MultiplayerSettings, -0.2, -0.05);
    textfieldSelectFieldSizeR6.fontsize(root6MultiplayerSettings, 0.01);
    textfieldSelectFieldSizeR6.size(root6MultiplayerSettings, 0.15, 0.05);

    textfieldSelectServerNameR6.position(root6MultiplayerSettings, -0.2, 0.1);
    textfieldSelectServerNameR6.fontsize(root6MultiplayerSettings, 0.01);
    textfieldSelectServerNameR6.size(root6MultiplayerSettings, 0.15, 0.05);

    labelSettingsR6.position(root6MultiplayerSettings, -0.255, 0.05);
    labelSettingsR6.fontsize(root6MultiplayerSettings, 0.03);
    labelSettingsR6.size(root6MultiplayerSettings, 0.3, 0.6);

    labelTextSelectFieldSizeR6.position(root6MultiplayerSettings, -0.347, -0.05);
    labelTextSelectFieldSizeR6.fontsize(root6MultiplayerSettings, 0.02);
    labelTextSelectFieldSizeR6.size(root6MultiplayerSettings, 0.2, 0.07);

    labelSelectServerNameR6.position(root6MultiplayerSettings, -0.347, 0.1);
    labelSelectServerNameR6.fontsize(root6MultiplayerSettings, 0.02);
    labelSelectServerNameR6.size(root6MultiplayerSettings, 0.2, 0.1);

    // ---------------root 7
    // ---------------------------------------------------------------------
    // root7
    Image imageGifOfJetR7 =
        new Image(getClass().getResource("/com/matti/battleship/images/jet.gif").toExternalForm());
    ImageViews imageviewGifOfJetR7 = new ImageViews(imageGifOfJetR7);

    Buttons buttonGoBackR7 = new Buttons();
    buttonGoBackR7.setId("buttonsGoBack");

    Labels labelWaitingForOtherPlayerToJoinR7 = new Labels("Waiting for other player to join");
    labelWaitingForOtherPlayerToJoinR7.setId("labelWaitingScreen1");

    List<String> listFunnyTextsR7 =
        List.of(
            "Get a quick coffee",
            "Sharpening torpedoes",
            "Waiting intensifies",
            "Checking radar signals",
            "Calibrating cannons");

    String base = listFunnyTextsR7.get(new Random().nextInt(listFunnyTextsR7.size()));

    Labels labelFunnyInfosR7 = new Labels(base);
    labelFunnyInfosR7.setId("labelWaitingScreen2");

    Timeline dots =
        new Timeline(
            new KeyFrame(Duration.millis(0), e -> labelFunnyInfosR7.setText(base)),
            new KeyFrame(Duration.millis(400), e -> labelFunnyInfosR7.setText(base + ".")),
            new KeyFrame(Duration.millis(800), e -> labelFunnyInfosR7.setText(base + "..")),
            new KeyFrame(Duration.millis(1200), e -> labelFunnyInfosR7.setText(base + "...")));
    dots.setCycleCount(Timeline.INDEFINITE);
    dots.play();

    StackPane root7LoadingScreen =
        new StackPane(
            buttonGoBackR7,
            labelWaitingForOtherPlayerToJoinR7,
            labelFunnyInfosR7,
            imageviewGifOfJetR7);
    root7LoadingScreen.setId("root7LoadingScreen");

    imageviewGifOfJetR7.position(root7LoadingScreen, 0, 0.2);
    imageviewGifOfJetR7.size(root7LoadingScreen, 0.5, 0.5);

    buttonGoBackR7.position(root7LoadingScreen, -0.45, -0.43);
    buttonGoBackR7.fontsize(root7LoadingScreen, 0.01);
    buttonGoBackR7.size(root7LoadingScreen, 0.07, 0.1);

    labelWaitingForOtherPlayerToJoinR7.position(root7LoadingScreen, 0, -0.2);
    labelWaitingForOtherPlayerToJoinR7.fontsize(root7LoadingScreen, 0.05);
    labelWaitingForOtherPlayerToJoinR7.size(root7LoadingScreen, 0.7, 0.2);
    labelWaitingForOtherPlayerToJoinR7.setAlignment(Pos.CENTER);

    labelFunnyInfosR7.position(root7LoadingScreen, 0, 0);
    labelFunnyInfosR7.fontsize(root7LoadingScreen, 0.03);
    labelFunnyInfosR7.size(root7LoadingScreen, 0.4, 0.2);
    labelFunnyInfosR7.setAlignment(Pos.CENTER);

    // ---------------root 8
    // ---------------------------------------------------------------------
    // root8

    // ---------------button_actions---------------------------------------------------------------------

    // --------------------------------- root 1
    // ----------------------
    buttonSingleplayerR1.setOnAction(
        e -> {
          scene1.setRoot(root2SingleplayerSettings);
          this.playingMode = PlayingMode.VS_AI;
          // this.game = new Game(PlayingMode.VS_AI, new Player("Player", boardSize), ,
          // turn, initialShipSetup)
        });
    buttonMultiplayerR1.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
          this.playingMode = PlayingMode.VS_PLAYER;
          discover_servers(root3JoinOtherServers);
        });

    // --------------------------------- root 2
    // ----------------------
    buttonGoBackR2.setOnAction(e -> scene1.setRoot(root1GamemodeSelection));

    buttonLoadGameR2.setOnAction(
        e -> {
          File storageFile =
              FileReaderService.chooseSaveFile(root2SingleplayerSettings.getScene().getWindow());
          if (storageFile == null) {
            System.out.println("Please choose a file to load the game state from a file!");
            return;
          }
          try {
            this.game = FileReaderService.loadGameFromFile(storageFile);
            this.selected_field_size = this.game.player.board.getSize();
            this.playingMode = this.game.getPlayingMode();
            this.board = this.game.player.board;
            // player choose single player so the playing mode can't be 'VS_PLAYER'
            if (game.getPlayingMode() == PlayingMode.VS_AI) {
              this.aIAlgorithm =
                  GameUtils.determineAlgorithmForTheGame(
                      this.game.getDifficulty(), selected_field_size);
              this.aIAlgorithm.prepareAfterLoadingFromFile(this.game);
            } else {
              throw new IllegalStateException(
                  "Loaded game state for wrong playing mode! For 'single player' please choose a file with the playing mode 'VS_AI'!");
            }
            root5ShootOnShips
                .getChildren()
                .addAll(
                    buttonEndGameR5,
                    labelBackgroundShootingR5,
                    buttonSafeGameR5,
                    labelTextYourSideR5,
                    labelTextEnemySideR5);

            preparePlayingGridPanes(root5ShootOnShips, this.game);
            buttonEndGameR5.toFront();
            buttonSafeGameR5.toFront();

            scene1.setRoot(root5ShootOnShips);
          } catch (Exception ex) {
            System.out.println(ex.toString());
            return;
          }
        });

    final EventHandler<ActionEvent> startHandler =
        (ActionEvent e) -> {
          Buttons source = (Buttons) e.getSource();

          if (source == buttonStartPlacingShipsR2) {
            if (!textfieldSelectFieldSizeR2.getText().isEmpty()) {
              try {
                this.selected_field_size = Integer.parseInt(textfieldSelectFieldSizeR2.getText());
              } catch (NumberFormatException ex) {
                System.out.println("Invalid field size, default value 10");
                this.selected_field_size = 10;
              }
            }
          } else if (source == buttonStartGameR6) {
            if (!textfieldSelectFieldSizeR6.getText().isEmpty()) {
              try {
                this.selected_field_size = Integer.parseInt(textfieldSelectFieldSizeR6.getText());
              } catch (NumberFormatException ex) {
                System.out.println("Invalid field size, default value 10");
                this.selected_field_size = 10;
              }
            }
            // if (!textfieldSelectServerNameR6.getText().isEmpty()) {
            //
            // }
          }

          // validate input data
          if (!BoardUtils.isValidBoardSize(this.selected_field_size)) {
            System.out.println("Please select a valid Board size!");
            return;
          }
          root4PlaceShips
              .getChildren()
              .addAll(
                  labelSelectPositionOfYourBoatsR4,
                  labelShipsSpawnWhenBoardInitializedR4,
                  buttonStartShootingR4,
                  labelPressRToRotateR4,
                  buttonEndGameR4);

          // prepare ship setup for ship placement
          this.initialShipSetup =
              BoardUtils.generateShipSetupForPlacement(this.selected_field_size);

          // save current AIDifficulty
          String selectedDifficultyString =
              comboboxesDifficultySelectionR2.getSelectionModel().getSelectedItem();
          this.difficulty = GameUtils.getDifficultyFromString(selectedDifficultyString);

          this.board = new Board(selected_field_size);

          GridPane battleGrid = new GridPane();

          battleGrid.prefWidthProperty().bind(boardSize);
          battleGrid.prefHeightProperty().bind(boardSize);
          battleGrid.minWidthProperty().bind(boardSize);
          battleGrid.minHeightProperty().bind(boardSize);
          battleGrid.maxWidthProperty().bind(boardSize);
          battleGrid.maxHeightProperty().bind(boardSize);

          battleGrid.setStyle("-fx-background-color: transparent;");

          // initialize the grid with cells
          initializePlacementBoard(battleGrid);

          prepareShipRectangles(root4PlaceShips);

          root4PlaceShips.getChildren().add(battleGrid);
          StackPane.setAlignment(battleGrid, Pos.CENTER);
          scene1.setRoot(root4PlaceShips);
        };

    buttonStartPlacingShipsR2.setOnAction(startHandler);

    // --------------------------------- root 3
    // ----------------------

    buttonGoBackR3.setOnAction(e -> scene1.setRoot(root1GamemodeSelection));
    buttonCreateOwnGameR3.setOnAction(e -> scene1.setRoot(root6MultiplayerSettings));
    buttonRefreshServersR3.setOnAction(e -> discover_servers(root3JoinOtherServers));

    // --------------------------------- root 4
    buttonEndGameR4.setOnAction(
        e -> {
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();
          this.board = null;
          scene1.setRoot(root1GamemodeSelection);
        });

    buttonStartShootingR4.setOnAction(
        e -> {
          // prevent starting a game when not all ships have been placed
          if (this.board.getNumberOfOccupiedFields()
              != BoardUtils.getNumberForExactNumberOfMandatoryOccupiedFields(
                  this.board.getSize())) {
            System.out.println(
                "You can't start a game if you don't have placed all ships on the board!");
            e.consume();
            return;
          }
          root5ShootOnShips
              .getChildren()
              .addAll(
                  buttonEndGameR5,
                  labelBackgroundShootingR5,
                  buttonSafeGameR5,
                  labelTextYourSideR5,
                  labelTextEnemySideR5);

          // TODO: Add the case for playing against another player -> no board needs to be
          // added

          // initialising the playing boards
          Board opponentBoard = new Board(this.selected_field_size);
          PlacementAlgorithm.placeShipsWithBacktracking(opponentBoard, this.initialShipSetup);
          this.game =
              new Game(
                  this.playingMode,
                  new Player("Player", this.selected_field_size),
                  new Player("Opponent", this.selected_field_size),
                  PlayerTurn.PLAYER,
                  this.initialShipSetup);
          this.game.opponent.board = opponentBoard;
          this.game.player.board = this.board;

          // determine AI algorithm for the 'VS_AI' playing mode
          if (this.game.getPlayingMode() == PlayingMode.VS_AI) {
            this.aIAlgorithm =
                GameUtils.determineAlgorithmForTheGame(this.difficulty, selected_field_size);
            this.game.setDifficulty(this.difficulty);
          }

          preparePlayingGridPanes(root5ShootOnShips, this.game);

          buttonEndGameR5.toFront();
          buttonSafeGameR5.toFront();
          scene1.setRoot(root5ShootOnShips);
        });

    // --------------------------------- root 5
    buttonEndGameR5.setOnAction(
        e -> {
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();
          this.board = null;
          scene1.setRoot(root1GamemodeSelection);
        });

    buttonSafeGameR5.setOnAction(
        e -> {
          // TODO: Get back the file path for multiplayer
          if (!FileWriterService.safeGameStateToFile(this.game, null)) {
            System.out.println("Failed to properly safe the gamestate!");
            return;
          }
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();
          this.board = null;
          this.game = null;
          this.playingMode = null;
          this.difficulty = null;
          this.aIAlgorithm = null;
          this.playerRole = null;
          this.initialShipSetup = null;

          scene1.setRoot(root1GamemodeSelection);
        });

    // --------------------------------- root 6
    // ----------------------
    buttonGoBackR6.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
        });

    buttonStartGameR6.setOnAction(
        e -> {
          scene1.setRoot(root7LoadingScreen);
        });

    // start_game_button_r6.setOnAction(startHandler);

    // --------------------------------- root 7
    // ----------------------
    buttonGoBackR7.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
        });

    // ---------------Stage
    // Setup--------------------------------------------------------------
    scene1 = new Scene(root1GamemodeSelection, 800, 600);
    scene1.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

    // NEW: boardSize hängt an Scene-Größe
    boardSize.bind(Bindings.min(scene1.widthProperty(), scene1.heightProperty()).multiply(0.65));

    primaryStage.setTitle("Battleship");
    Image icon =
        new Image(
            getClass().getResource("/com/matti/battleship/images/favicon.png").toExternalForm());
    primaryStage.getIcons().add(icon);
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  // _________________________________________________________________
  // ----- Helpers -----
  // _________________________________________________________________

  /**
   * Prepares and initializes the playing grid panes for the game UI.
   *
   * <p>This method creates two {@link GridPane} instances for the player's and opponent's boards,
   * configures their layout properties, binds their preferred sizes to the {@code boardSize}
   * property, and initializes their content through dedicated initialization methods. It also
   * positions the grids within the root pane using translation bindings based on the scene's width,
   * ensuring a responsive layout.
   *
   * @param root the root {@link Pane} to which the playing grid panes will be added.
   */
  private void preparePlayingGridPanes(Pane root, Game game) {
    // delete all existing gridpane children of the root
    for (Node node : root.getChildren()) {
      if (node instanceof GridPane) {
        root.getChildren().remove(node);
      }
    }

    GridPane playerGrid = new GridPane();
    playerGrid.setHgap(0);
    playerGrid.setVgap(0);
    playerGrid.setPadding(new Insets(12));
    playerGrid.prefWidthProperty().bind(boardSize);
    playerGrid.prefHeightProperty().bind(boardSize);

    GridPane opponentGrid = new GridPane();
    opponentGrid.setHgap(0);
    opponentGrid.setVgap(0);
    opponentGrid.setPadding(new Insets(12));
    opponentGrid.prefWidthProperty().bind(boardSize);
    opponentGrid.prefHeightProperty().bind(boardSize);

    initializePlayingBoardOpponentGrid(opponentGrid);
    initializePlayingBoardPlayerGrid(playerGrid, opponentGrid, root);

    StackPane.setAlignment(playerGrid, Pos.CENTER);
    playerGrid.translateXProperty().bind(scene1.widthProperty().multiply(-0.24));

    StackPane.setAlignment(opponentGrid, Pos.CENTER);
    opponentGrid.translateXProperty().bind(scene1.widthProperty().multiply(0.24));

    // synchronize the UI boards according to the datastructures
    applyBoardUpdatesToOpponentsUIBoard(opponentGrid, game.player.board);
    applyBoardUpdatesToPlayerUIBoard(playerGrid, game.opponent.board);

    root.getChildren().addAll(opponentGrid, playerGrid);
    playerGrid.setAlignment(Pos.CENTER);
    opponentGrid.setAlignment(Pos.CENTER);
  }

  /**
   * Updates the opponent's game UI board based on the current state of the opponent's logical
   * board. This method is responsible for synchronizing the visual representation of the game with
   * the actual game state. It iterates over the opponent's logical board, updating the
   * corresponding UI elements in the opponent's game board.
   *
   * @param pane the GridPane to which the buttons will be added, representing the game board grid.
   */
  private void applyBoardUpdatesToPlayerUIBoard(GridPane opponentPane, Board opponentBoard) {
    DoubleBinding BUTTON_SIZE =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    Image imgMiss =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_miss.png")
                .toExternalForm());
    Image imgHit =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_hit.png")
                .toExternalForm());

    for (Field[] row : opponentBoard.board) {
      for (Field field : row) {
        Coordinates coor = field.getCoordinates();
        Buttons cell = (Buttons) GridPaneUtils.getNodeByRowColumn(opponentPane, coor.y, coor.x);
        FieldDisplayState fieldState = FieldUtils.getTheoreticalStateOfField(field, opponentBoard);
        // update ui according to changes
        ImageViews iv;
        switch (fieldState) {
          case MISS -> {
            iv = new ImageViews(imgMiss);

            iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
            iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
            iv.setPreserveRatio(false);

            cell.setGraphic(iv);
            break;
          }
          case HIT -> {
            iv = new ImageViews(imgHit);

            iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
            iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
            iv.setPreserveRatio(false);

            cell.setGraphic(iv);
            break;
          }
          case SUNK -> {
            Ship ship = opponentBoard.getShipByCoordinates(coor);
            if (ship == null) {
              throw new NullPointerException("The ship at " + coor.toString() + " can't be null!");
            }

            Coordinates[] fieldsOfShip = ShipUtils.getFieldsOfShip(opponentBoard, ship);
            for (Coordinates shipCoordinates : fieldsOfShip) {
              Buttons shipCell =
                  (Buttons)
                      GridPaneUtils.getNodeByRowColumn(
                          opponentPane, shipCoordinates.y, shipCoordinates.x);
              shipCell.setGraphic(null);
              shipCell.setStyle("-fx-background-color: red;");
            }
            break;
          }
          case NOT_SET -> {}
        }
      }
    }
  }

  /**
   * Updates the opponent's game UI board based on the current state of the opponent's logical
   * board. This method is responsible for synchronizing the visual representation of the game with
   * the actual game state. It iterates over the opponent's logical board, updating the
   * corresponding UI elements in the opponent's game board.
   *
   * @param opponentPane The GridPane containing the opponent's UI board, which needs to be updated
   *     to reflect the current game state.
   * @param playerBoard The opponent's logical board, which contains the current state of the game.
   * @param gameStatus The current status of the game, which determines the appearance of certain UI
   *     elements.
   */
  private void applyBoardUpdatesToOpponentsUIBoard(GridPane opponentPane, Board playerBoard) {
    DoubleBinding BUTTON_SIZE =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    Image imgMiss =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_miss.png")
                .toExternalForm());
    Image imgHit =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_hit.png")
                .toExternalForm());

    for (Field[] row : playerBoard.board) {
      for (Field field : row) {
        Coordinates coor = field.getCoordinates();
        StackPane cell = (StackPane) GridPaneUtils.getNodeByRowColumn(opponentPane, coor.y, coor.x);
        PlayerBoardCellContext context = (PlayerBoardCellContext) cell.getUserData();
        FieldDisplayState fieldState = FieldUtils.getTheoreticalStateOfField(field, playerBoard);
        if (context.state != fieldState) {
          context.state = fieldState;
          cell.setUserData(context);
          // update ui according to changes
          ImageViews iv;
          switch (fieldState) {
            case MISS -> {
              iv = new ImageViews(imgMiss);

              iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
              iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
              iv.setPreserveRatio(false);

              cell.getChildren().clear();
              cell.getChildren().add(iv);
              break;
            }
            case HIT -> {
              iv = new ImageViews(imgHit);

              iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
              iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
              iv.setPreserveRatio(false);

              cell.getChildren().clear();
              cell.getChildren().add(iv);
              break;
            }
            case SUNK -> {
              Ship ship = playerBoard.getShipByCoordinates(coor);
              if (ship == null) {
                throw new NullPointerException(
                    "The ship at " + coor.toString() + " can't be null!");
              }

              Coordinates[] fieldsOfShip = ShipUtils.getFieldsOfShip(playerBoard, ship);
              for (Coordinates shipCoordinates : fieldsOfShip) {
                StackPane shipCell =
                    (StackPane)
                        GridPaneUtils.getNodeByRowColumn(
                            opponentPane, shipCoordinates.y, shipCoordinates.x);
                PlayerBoardCellContext shipCellContext =
                    (PlayerBoardCellContext) shipCell.getUserData();
                shipCellContext.state = FieldDisplayState.SUNK;
                shipCell.setUserData(shipCellContext);
                shipCell.getChildren().clear();
                shipCell.setStyle("-fx-background-color: red;");
              }
              break;
            }
            case NOT_SET -> {
              throw new UnknownError(
                  "State of a field changed back to NOT_SET! Field at: " + coor.toString());
            }
          }
        }
      }
    }
  }

  /**
   * Initializes the opponent's game board grid within the provided GridPane.
   *
   * <p>This method dynamically creates and adds cell representations for each field on the
   * opponent's grid. Each cell is a StackPane with size bindings that adapt to the current board
   * size and selected field size, ensuring the grid is responsive and scales appropriately.
   *
   * <p>The process involves:
   *
   * <ul>
   *   <li>Creating a DoubleBinding that calculates cell size based on the current board size and
   *       field size, scaled by 0.9 for padding.
   *   <li>Iterating over the game board's fields, retrieving their coordinates.
   *   <li>For each field, creating a StackPane that represents a cell, binding its width and height
   *       to the dynamic size binding.
   *   <li>Styling each cell with a border and background color (light blue).
   *   <li>Adding each cell to the GridPane at the appropriate X (column) and Y (row) positions.
   * </ul>
   *
   * This setup ensures the opponent's grid is visually consistent and responsive to changes in
   * board or field sizes.
   *
   * @param pane the GridPane into which opponent grid cells will be added.
   */
  private void initializePlayingBoardOpponentGrid(GridPane pane) {
    DoubleBinding cs =
        Bindings.createDoubleBinding(
            () -> (boardSize.get() / selected_field_size) * 0.9, boardSize);
    for (Field[] row : board.board) {
      for (Field field : row) {
        int X = field.getCoordinates().x;
        int Y = field.getCoordinates().y;

        StackPane cell = new StackPane();

        // NEW: Zellgröße dynamisch
        cell.prefWidthProperty().bind(cs);
        cell.prefHeightProperty().bind(cs);
        cell.minWidthProperty().bind(cs);
        cell.minHeightProperty().bind(cs);

        cell.setStyle(
            "-fx-border-color: black;"
                + "-fx-background-image: url('/com/matti/battleship/images/cell.png');"
                + "-fx-background-size: cover;"
                + "-fx-background-repeat: no-repeat;"
                + "-fx-background-position: center center;");

        cell.setUserData(new PlayerBoardCellContext());

        pane.add(cell, X, Y);
      }
    }
  }

  /**
   * Initializes the playing board for a player by creating a grid of buttons in the specified
   * GridPane. Each button represents a field on the board, and its size is determined by the
   * current board size and selected field size.
   *
   * @param pane The GridPane where the player's playing board will be rendered.
   * @param opponentPane The GridPane where the opponent's playing board will be rendered.
   * @param root The parent Pane of the player's playing board.
   */
  private void initializePlayingBoardPlayerGrid(GridPane pane, GridPane opponentPane, Pane root) {
    DoubleBinding BUTTON_SIZE =
        Bindings.createDoubleBinding(
            () -> (boardSize.get() / selected_field_size) * 0.9, boardSize);

    Image imgMiss =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_miss.png")
                .toExternalForm());
    Image imgHit =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_hit.png")
                .toExternalForm());

    for (int r = 0; r < selected_field_size; r++) {
      for (int c = 0; c < selected_field_size; c++) {
        Coordinates coordinates = new Coordinates(c, r);
        Buttons btn = new Buttons();
        btn.setStyle(
            "-fx-border-color: black;"
                + "-fx-background-image: url('/com/matti/battleship/images/cell.png');"
                + "-fx-background-size: cover;"
                + "-fx-background-repeat: no-repeat;"
                + "-fx-background-position: center center;");

        // NEW: Button skaliert mit boardSize
        btn.prefWidthProperty().bind(BUTTON_SIZE);
        btn.prefHeightProperty().bind(BUTTON_SIZE);
        btn.minWidthProperty().bind(BUTTON_SIZE);
        btn.minHeightProperty().bind(BUTTON_SIZE);

        btn.setOnAction(
            ev -> {
              ImageViews iv;

              // logic for playing against the AI
              // shot add the field of the opponent
              ShotAttemptResult res = this.game.shotShot(coordinates, root);
              // process the shot response
              switch (res) {
                case MISS:
                  iv = new ImageViews(imgMiss);

                  iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.setPreserveRatio(false);

                  btn.setGraphic(iv);

                  break;
                case HIT:
                  iv = new ImageViews(imgHit);

                  iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.setPreserveRatio(false);

                  btn.setGraphic(iv);
                  break;
                case SUNK:
                  applyChangesToButtonsAfterShipSunk(pane, coordinates);
                  break;
                case INVALID:
                  System.out.println("Invalid shot! Please try again!");
                  break;
              }

              if (this.game.getWhoseTurn() == PlayerTurn.OPPONENT) {
                // wait for the opponents move
                this.aIAlgorithm.takeAShot(this.game, root);
                applyBoardUpdatesToOpponentsUIBoard(opponentPane, this.game.player.board);
              }
            });

        pane.add(btn, c, r);
      }
    }
  }

  /**
   * Updates the visual representation of the game board after a ship has been sunk.
   *
   * <p>This method changes the style of the buttons corresponding to the sunk ship's fields to
   * indicate they are sunk, and overlays surrounding fields with an image (e.g., a "miss" marker)
   * to show the area around the sunk ship.
   *
   * @param gridPane the {@link GridPane} containing the buttons representing the game board.
   * @param coordinates the {@link Coordinates} of the sunk ship's position.
   * @throws NullPointerException if the ship at the specified coordinates cannot be found.
   */
  private void applyChangesToButtonsAfterShipSunk(GridPane gridPane, Coordinates coordinates) {
    Board targettedBoard = game.opponent.board;
    // get coordinates of surrounding fields and fields of ship
    Ship ship = targettedBoard.getShipByCoordinates(coordinates);
    if (ship == null) {
      throw new NullPointerException("The ship at " + coordinates.toString() + " can't be null!");
    }
    Coordinates[] fieldsOfShip = ShipUtils.getFieldsOfShip(targettedBoard, ship);
    Coordinates[] fieldsAroundShip = ShipUtils.getFieldsAroundShip(targettedBoard, ship);
    // apply changes to fields of ship
    DoubleBinding BUTTON_SIZE =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    for (Coordinates coor : fieldsOfShip) {
      Buttons btn = (Buttons) GridPaneUtils.getNodeByRowColumn(gridPane, coor.y, coor.x);
      btn.setGraphic(null);
      btn.setStyle("-fx-background-color: red;");
    }
    Image imgMiss =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/game/tile_miss.png")
                .toExternalForm());

    for (Coordinates coor : fieldsAroundShip) {
      ImageViews iv = new ImageViews(imgMiss);

      iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
      iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
      iv.setPreserveRatio(false);
      Buttons btn = (Buttons) GridPaneUtils.getNodeByRowColumn(gridPane, coor.y, coor.x);
      btn.setGraphic(iv);
    }
  }

  /**
   * Rotates and positions a Rectangle representing a ship within a GridPane layout based on the
   * specified direction.
   *
   * <p>This method adjusts the rectangle's row and column indices, span, and size bindings to
   * visually rotate the ship within the grid. It ensures the ship remains within grid boundaries
   * before applying changes. If rotation is not possible due to boundary constraints, an
   * informative message is printed and the operation is aborted.
   *
   * <p>The size of the rectangle is dynamically bound to the current board size, ensuring
   * responsive resizing. The rectangle is centered within its grid cell after positioning.
   *
   * @param shipRect the Rectangle object representing the ship to be rotated and positioned.
   * @param row the current row index of the ship's starting position.
   * @param col the current column index of the ship's starting position.
   * @param shipLength the length of the ship in grid units.
   * @param boardSize the total size (number of cells) of the game board.
   * @param newDirection the direction to rotate the ship to (UP, DOWN, LEFT, RIGHT).
   */
  private void rotateRectangleOnGridPane(
      Rectangle shipRect, int row, int col, int shipLength, int boardSize, Direction newDirection) {
    DoubleBinding cs =
        Bindings.createDoubleBinding(
            () -> this.boardSize.get() / selected_field_size, this.boardSize);
    double rotationAngle = 0.;

    switch (newDirection) {
      case DOWN:
        if (row + (shipLength - 1) > boardSize - 1) {
          System.out.println("Couldn't rotate the ship to" + newDirection.toString());
          return;
        }
        GridPane.setRowIndex(shipRect, row);
        GridPane.setColumnIndex(shipRect, col);
        GridPane.setRowSpan(shipRect, shipLength);
        GridPane.setColumnSpan(shipRect, 1);

        shipRect.heightProperty().bind(cs.multiply(shipLength * 0.94));
        shipRect.widthProperty().bind(cs.multiply(0.8));

        rotationAngle = 90.;

        break;
      case UP:
        if (row - (shipLength - 1) < 0) {
          System.out.println("Couldn't rotate the ship to" + newDirection.toString());
          return;
        }
        GridPane.setRowIndex(shipRect, row - (shipLength - 1));
        GridPane.setColumnIndex(shipRect, col);
        GridPane.setRowSpan(shipRect, shipLength);
        GridPane.setColumnSpan(shipRect, 1);
        shipRect.heightProperty().bind(cs.multiply(shipLength * 0.94));
        shipRect.widthProperty().bind(cs.multiply(0.8));

        rotationAngle = 270.;

        break;
      case RIGHT:
        if (col + (shipLength - 1) > boardSize) {
          System.out.println("Couldn't rotate the ship to" + newDirection.toString());
          return;
        }
        GridPane.setRowIndex(shipRect, row);
        GridPane.setColumnIndex(shipRect, col);
        GridPane.setRowSpan(shipRect, 1);
        GridPane.setColumnSpan(shipRect, shipLength);
        shipRect.widthProperty().bind(cs.multiply(shipLength * 0.94));
        shipRect.heightProperty().bind(cs.multiply(0.8));

        rotationAngle = 0.;

        break;
      case LEFT:
        if (col - (shipLength - 1) < 0) {
          System.out.println("Couldn't rotate the ship to" + newDirection.toString());
          return;
        }
        GridPane.setRowIndex(shipRect, row);
        GridPane.setColumnIndex(shipRect, col - (shipLength - 1));
        GridPane.setRowSpan(shipRect, 1);
        GridPane.setColumnSpan(shipRect, shipLength);
        shipRect.widthProperty().bind(cs.multiply(shipLength * 0.94));
        shipRect.heightProperty().bind(cs.multiply(0.8));

        rotationAngle = 180.;

        break;
    }
    String imagePath = new ResourceProfiler().getPictureOfShip(shipLength);
    Image ship_image =
        ShipUtils.rotateImage(
            new Image(getClass().getResource(imagePath).toExternalForm()), rotationAngle);
    shipRect.setFill(new ImagePattern(ship_image));
    GridPane.setHalignment(shipRect, javafx.geometry.HPos.CENTER);
    GridPane.setValignment(shipRect, javafx.geometry.VPos.CENTER);
  }

  /**
   * Prepares and initializes the ship rectangles within the provided root pane.
   *
   * <p>This method creates a visual representation of ships based on predefined ship lengths,
   * binding their size dynamically to the current board size for responsiveness. Each ship is
   * represented by a Rectangle with styling and transformation capabilities, including
   * drag-and-drop and rotation via keyboard input (pressing 'R').
   *
   * <p>During drag detection, the method sets up event handlers to handle ship rotation, which
   * involves removing the ship from the board, updating its direction, and attempting to re-place
   * it. If placement fails, the ship's direction is reverted, and it is re-added to the original
   * position.
   *
   * <p>The rectangles are added to the root pane, positioned with margins and translation bindings
   * to display multiple ships in a row. They also support dragging with a visual snapshot.
   *
   * @param root the Pane to which the ship rectangles will be added and displayed.
   */
  private void prepareShipRectangles(Pane root) {
    ShipLength[] allLengths = this.initialShipSetup;

    DoubleBinding cs =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    int offsetUnits = 0;

    for (int i = 0; i < allLengths.length; i++) {
      ShipLength length = allLengths[i];

      Rectangle ship = new Rectangle();

      String imagePath = new ResourceProfiler().getPictureOfShip(length.getValue());

      Image ship_image = new Image(getClass().getResource(imagePath).toExternalForm());

      ship.setFill(new ImagePattern(ship_image));
      ship.setArcWidth(10);
      ship.setArcHeight(10);

      ship.widthProperty().bind(cs.multiply(length.getValue()).multiply(0.94)); // 0.94
      ship.heightProperty().bind(cs.multiply(0.8)); // 0.8

      ship.setUserData(
          new ShipGridElement(
              new Coordinates(0, 0), Direction.RIGHT, length, boardSize.intValue()));

      ship.setOnDragDetected(
          ev -> {
            // ---------- rotate logic
            scene1.getRoot().requestFocus();
            scene1.setOnKeyPressed(
                e -> {
                  if (e.getCode() == KeyCode.R) { // Wenn R gedrückt
                    ShipGridElement data = (ShipGridElement) ship.getUserData();
                    Direction oldDirection = data.getDirection();
                    Direction newDir = // neue direction
                        (oldDirection == Direction.RIGHT) ? Direction.DOWN : Direction.RIGHT;

                    data.setDirection(newDir);
                    ship.setUserData(data); // neue data für ship speichern

                    // first remove ship
                    Ship removedShip = this.board.removeShip(data.getCoordinates());
                    if (removedShip == null) {
                      throw new IllegalStateException(
                          "While trying to rotate a ship in the process the aimed ship could not be temporarilly removed from the board!");
                    }
                    // update the ship and try to place it if not valid revert the changes
                    removedShip.setDirection(newDir);
                    if (!this.board.addShip(removedShip)) {
                      removedShip.setDirection(oldDirection);
                      data.setDirection(oldDirection);
                      ship.setUserData(data);

                      if (!this.board.addShip(removedShip)) {
                        throw new IllegalStateException(
                            "Failed to place a ship back on its old position after rotation attempt!");
                      }
                    } else {
                      rotateRectangleOnGridPane(
                          ship,
                          data.getCoordinates().y,
                          data.getCoordinates().x,
                          data.getLength().getValue(),
                          this.board.getSize(),
                          newDir);
                    }
                  }
                });

            // -----------
            Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            WritableImage snapshot = ship.snapshot(params, null);
            db.setDragView(snapshot);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.format("SHIP_WIDTH_%d", PlayingUtils.getRandomInt()));
            db.setContent(content);
            ev.consume();
          });

      root.getChildren().add(ship);

      StackPane.setAlignment(ship, Pos.BOTTOM_LEFT);

      int finalOffsetUnits = offsetUnits;
      DoubleBinding desiredX = cs.multiply(finalOffsetUnits).multiply(0.6);

      // WICHTIG: boundsInLocal statt boundsInParent (verhindert Rekursion)
      DoubleBinding shipW =
          Bindings.createDoubleBinding(
              () -> ship.getBoundsInLocal().getWidth(),
              ship.boundsInLocalProperty(),
              ship.scaleXProperty());

      DoubleBinding maxX =
          Bindings.createDoubleBinding(
              () -> Math.max(0, root.getWidth() - shipW.get()), root.widthProperty(), shipW);

      DoubleBinding clampedX =
          Bindings.createDoubleBinding(
              () -> {
                double x = desiredX.get();
                return Math.max(0, Math.min(x, maxX.get()));
              },
              desiredX,
              maxX);

      ship.translateXProperty().bind(clampedX);
      ship.setTranslateY(0);

      // TODO: Right size
      ship.setScaleX(0.6);
      ship.setScaleY(0.6);

      offsetUnits += length.getValue();
    }
  }

  /**
   * Initializes the placement board by creating and configuring grid cells within the provided
   * GridPane.
   *
   * <p>This method dynamically binds each cell's size to the current board size, ensuring
   * responsiveness. Each cell is styled with borders and background color, and set up to handle
   * drag-and-drop operations for placing ships. When a ship is dropped onto a cell, the method
   * validates the placement, updates the internal board data structure, and visually positions the
   * ship rectangle within the grid.
   *
   * <p>If the placement is invalid, the method reverts changes to maintain consistency.
   * Successfully placed ships are repositioned within the grid, and their user data is updated
   * accordingly.
   *
   * @param grid the GridPane representing the placement board where cells and ships are
   *     initialized.
   */
  private void initializePlacementBoard(GridPane grid) {
    DoubleBinding cs =
        Bindings.createDoubleBinding(() -> boardSize.get() / selected_field_size, boardSize);

    for (Field[] row : board.board) {
      for (Field field : row) {
        int X = field.getCoordinates().x;
        int Y = field.getCoordinates().y;

        StackPane cell = new StackPane();

        // NEW: Zellgröße dynamisch
        cell.prefWidthProperty().bind(cs);
        cell.prefHeightProperty().bind(cs);
        cell.minWidthProperty().bind(cs);
        cell.minHeightProperty().bind(cs);

        cell.setStyle(
            "-fx-border-color: black;"
                + "-fx-background-image: url('/com/matti/battleship/images/cell.png');"
                + "-fx-background-size: cover;"
                + "-fx-background-repeat: no-repeat;"
                + "-fx-background-position: center center;");

        cell.setOnDragOver(
            ev -> {
              if (ev.getGestureSource() != cell) {
                ev.acceptTransferModes(TransferMode.MOVE);
              }
              ev.consume();
            });

        cell.setOnDragDropped(
            ev -> {
              if (!ev.getDragboard().hasString()) return;
              Coordinates coords = new Coordinates(X, Y);

              Rectangle shipNode = (Rectangle) ev.getGestureSource();
              ShipGridElement shipData = (ShipGridElement) shipNode.getUserData();

              // first operate on the board data structure -> check if placement is valid
              Ship ship;
              Coordinates previousShipCoords = shipData.getCoordinates();
              boolean freshlyPlaced = false;
              if (shipData.isPlaced()) {
                ship = this.board.removeShip(shipData.getCoordinates());
                previousShipCoords = ship.getStartCoordinates();
                // update the new ship
                if (!ship.setStart(coords, this.board.getSize())) {
                  System.out.println("Failed to update ship start to " + coords);
                }
              } else {
                ship = new Ship(coords, shipData.getDirection(), shipData.getLength());
                shipData.setPlaced(true);
                freshlyPlaced = true;
              }

              if (!this.board.addShip(ship)) {
                // revert changes in ship
                ship.setStart(previousShipCoords, this.board.getSize());
                // try to add back the ship to the old position
                // if a ship was just placed freshly, no need to place is somewhere back on the
                // board
                if (!freshlyPlaced) {
                  if (!this.board.addShip(ship)) {
                    throw new IllegalStateException(
                        "Could not revert ship placement on the board!");
                  }
                } else {
                  shipData.setPlaced(false);
                }

                System.out.println(
                    "Could not place the ship back on its old field due to unknown reasons! Invalid placement at "
                        + coords);

                shipNode.translateXProperty().unbind();
                shipNode.setTranslateX(0);
                shipNode.setTranslateY(0);

                ev.setDropCompleted(false);
                ev.consume();
                return;
              }

              shipNode.translateXProperty().unbind();
              shipNode.setTranslateX(0);
              shipNode.setTranslateY(0);
              shipNode.setScaleX(1);
              shipNode.setScaleY(1);

              if (shipNode.getParent() != null) {
                ((javafx.scene.layout.Pane) shipNode.getParent()).getChildren().remove(shipNode);
              }

              // apply grid layout to rectangle
              applyGridLayoutToRectangle(
                  shipNode,
                  grid,
                  Y,
                  X,
                  shipData.getDirection(),
                  shipData.getLength().getValue(),
                  this.board.getSize());

              // update "userData" of the rectangle
              shipData.setCoordinates(coords);
              shipNode.setUserData(shipData);

              ev.setDropCompleted(true);
              ev.consume();
            });

        grid.add(cell, X, Y);
      }
    }
  }

  /**
   * Applies a grid layout to the given rectangle within the specified GridPane, positioning and
   * spanning it based on the ship's direction, length, and the board size.
   *
   * <p>This method adds the rectangle to the grid and sets its row and column indices along with
   * row and column spans to visually represent the ship's placement. It ensures that ships do not
   * extend beyond the grid boundaries by adjusting starting positions accordingly.
   *
   * <p>The rectangle is centered within its grid cell both horizontally and vertically.
   *
   * @param rect the Rectangle representing the ship to be placed on the grid.
   * @param grid the GridPane in which the rectangle will be positioned.
   * @param row the starting row index for the ship placement.
   * @param col the starting column index for the ship placement.
   * @param direction the direction in which the ship extends (UP, DOWN, LEFT, RIGHT).
   * @param length the length of the ship.
   * @param boardSize the size of the board (number of rows/columns).
   */
  private void applyGridLayoutToRectangle(
      Rectangle rect,
      GridPane grid,
      int row,
      int col,
      Direction direction,
      int length,
      int boardSize) {
    grid.getChildren().add(rect);
    switch (direction) {
      case DOWN:
        int finalColD = col;
        int finalRowD = row;
        if (row + (length - 1) > boardSize - 1) {
          finalRowD = boardSize - length;
        }
        GridPane.setRowIndex(rect, finalRowD);
        GridPane.setColumnIndex(rect, finalColD);
        GridPane.setRowSpan(rect, length);
        GridPane.setColumnSpan(rect, 1);
        break;
      case UP:
        int finalColU = col;
        int finalRowU = row;
        if (row - (length - 1) < 0) {
          finalRowU = length;
        }
        GridPane.setRowIndex(rect, finalRowU);
        GridPane.setColumnIndex(rect, finalColU);
        GridPane.setRowSpan(rect, length);
        GridPane.setColumnSpan(rect, 1);
        break;
      case RIGHT:
        int finalColR = col;
        if (col + (length - 1) > boardSize - 1) {
          finalColR = boardSize - length;
        }
        int finalRowR = row;
        GridPane.setRowIndex(rect, finalRowR);
        GridPane.setColumnIndex(rect, finalColR);
        GridPane.setRowSpan(rect, 1);
        GridPane.setColumnSpan(rect, length);
        break;
      case LEFT:
        int finalColL = col;
        if (col - (length - 1) < 0) {
          finalColL = length;
        }
        int finalRowL = row;
        GridPane.setRowIndex(rect, finalRowL);
        GridPane.setColumnIndex(rect, finalColL);
        GridPane.setRowSpan(rect, 1);
        GridPane.setColumnSpan(rect, length);
        break;
    }
    GridPane.setHalignment(rect, javafx.geometry.HPos.CENTER);
    GridPane.setValignment(rect, javafx.geometry.VPos.CENTER);
  }

  private void discover_servers(Pane root) {
    EnvConfig config = new EnvConfig();
    int port = config.getPort();
    ClientDiscoveryScanner scanner = new ClientDiscoveryScanner(port);
    List<DiscoveredServer> list_of_discovered_servers = new ArrayList<>();
    try {
      list_of_discovered_servers = scanner.discover(500);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    int amount_of_discovered_servers = list_of_discovered_servers.size();
    if (amount_of_discovered_servers == 0) {
      System.out.println("No servers found");
      Labels labelNoServersFound = new Labels("No servers found");
      labelNoServersFound.setId("labelsNormal");
      labelNoServersFound.position(root, 0, 0);
      labelNoServersFound.fontsize(root, 0.015);
      labelNoServersFound.size(root, 0.3, 0.07);
      root.getChildren().add(labelNoServersFound);
    } else {
      for (int i = 0; i < amount_of_discovered_servers && i < 9; i++) {
        String server_name = list_of_discovered_servers.get(i).name();
        // String host_name = list_of_discovered_servers.get(i).host(); // optional, if
        // needed
        Buttons join_server_button = new Buttons(server_name);
        int row = i / 3;
        int col = i % 3;
        double[] pos = {-0.2, 0.0, 0.2};
        double x_pos = pos[col];
        double y_pos = pos[row];
        join_server_button.position(root, x_pos, y_pos);
        join_server_button.fontsize(root, 0.02);
        join_server_button.size(root, 0.13, 0.05);
      }
    }
  }

  // Entry Point -> main function

  public static void main(String[] args) {
    launch(args);
  }
}
