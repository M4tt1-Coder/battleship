package com.matti.battleship;

/*
 * BattleShipApp
 * -------------
 * JavaFX application entry point.
 *
 * UI concept:
 * - One Scene (scene1); the root is switched depending on the current screen (scene1.setRoot(...)).
 * - Multiple "rootX" StackPanes represent individual screens (game mode, settings, placement, playing, etc.).
 *
 * Data / logic:
 * - Game, board, and AI are stored in the application object and updated when switching screens.
 *
 * Where to find:
 *  -   JavaFX lifecycle/roots -> line 130
 *  -   Button actions / navigation -> 558
 *  -   Stage / SceneSetup -> 880
 *  -   Helper functions -> 905
 *
 * root naming convention:
 *  - Structure:
 *      root<number><what_it_does>
 *  - Example:
 *      root2SingleplayerSettings
 *
 * object naming convention:
 *  - Structure:
 *      <type_of_object><what_it_does>R<number_of_root>
 *  - Example:
 *      buttonStartPlacingShipsR2
 *
 *
 */

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
import com.matti.battleship.enums.ShipBoardShare;
import com.matti.battleship.enums.ShipLength;
import com.matti.battleship.enums.ShotAttemptResult;
import com.matti.battleship.socket.GlobalConnector;
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

public class BattleShipApp extends Application {

  // -------------------------------------------------------------------
  // Global UI state
  // -------------------------------------------------------------------

  // Main scene: the currently visible screen is controlled by replacing the root
  private Scene scene1;

  // Settings / input: selected board size (default = 10)
  private int selected_field_size = 10;

  // Responsive board pixel size (bound to the Scene size later)
  private final DoubleProperty boardSize = new SimpleDoubleProperty(400);

  // -------------------------------------------------------------------
  // Temporary game state (reset on end / save / etc.)
  // -------------------------------------------------------------------
  private Game game; // current game instance
  private PlayingMode playingMode; // VS_AI or VS_PLAYER
  @Nullable private AIDifficulty difficulty; // selected AI difficulty (VS_AI only)
  @Nullable private Algorithm aIAlgorithm; // concrete AI strategy instance
  @Nullable private Role playerRole; // player role (client/server)
  @Nullable private GlobalConnector connector;

  // Occupancy rule: initial ship setup derived from board size / occupancy %
  private ShipLength[] initialShipSetup;

  private Board board;

  // -------------------------------------------------------------------
  // JavaFX lifecycle/roots
  // -------------------------------------------------------------------

  @Override
  public void start(Stage primaryStage) {

    // ================================================================
    // ROOT 1: Game mode selection (Singleplayer / Multiplayer)
    // ================================================================

    // Button: go to singleplayer settings screen
    Buttons buttonSingleplayerR1 = new Buttons("Singleplayer");
    buttonSingleplayerR1.setId("buttonSingleplayerR1");

    // Button: go to multiplayer server list screen
    Buttons buttonMultiplayerR1 = new Buttons("Multiplayer");
    buttonMultiplayerR1.setId("buttonMultiplayerR1");

    // Screen root container for root1
    StackPane root1GamemodeSelection = new StackPane(buttonSingleplayerR1, buttonMultiplayerR1);
    root1GamemodeSelection.setId("root1GamemodeSelection");

    // Layout root1: !position/size relative to the root pane
    buttonSingleplayerR1.position(root1GamemodeSelection, -0.25, 0.35);
    buttonSingleplayerR1.fontsize(root1GamemodeSelection, 0.05);
    buttonSingleplayerR1.size(root1GamemodeSelection, 0.5, 0.3);

    buttonMultiplayerR1.position(root1GamemodeSelection, 0.25, 0.35);
    buttonMultiplayerR1.fontsize(root1GamemodeSelection, 0.05);
    buttonMultiplayerR1.size(root1GamemodeSelection, 0.5, 0.3);

    // ================================================================
    // ROOT 2: Singleplayer settings (AI image + settings + start/load)
    // ================================================================

    // Background / preview image (player vs AI)
    Image imagePlayerVsAiR2 =
        new Image(
            getClass()
                .getResource("/com/matti/battleship/images/player_vs_ai.png")
                .toExternalForm());
    ImageViews imageviewPlayerVsAiR2 = new ImageViews(imagePlayerVsAiR2);

    // all the actions what the buttons do can be found in button actions

    // Navigation button: go back to root1
    Buttons buttonGoBackR2 = new Buttons();
    buttonGoBackR2.setId("buttonsGoBack");

    // Start flow: go to ship placement (root4)
    Buttons buttonStartPlacingShipsR2 = new Buttons("Start Game");
    buttonStartPlacingShipsR2.setId("buttonsNormal");

    // Load saved game and jump directly into playing screen (root5)
    Buttons buttonLoadGameR2 = new Buttons("Load Game");
    buttonLoadGameR2.setId("buttonsSaveAndLoad");

    // Difficulty selection for AI (Easy/Medium/Hard)
    ComboBoxes comboboxesDifficultySelectionR2 = new ComboBoxes();
    comboboxesDifficultySelectionR2.setId("comboboxes");

    // Ship occupancy selection (15% / 20% / 30%)
    ComboBoxes comboboxesAmountOfShipsR2 = new ComboBoxes();
    comboboxesAmountOfShipsR2.setId("comboboxes");

    // Input: field size text (validated later)
    TextFields textfieldSelectFieldSizeR2 = new TextFields();
    textfieldSelectFieldSizeR2.setId("textfields");
    textfieldSelectFieldSizeR2.setPromptText("Type in field size");

    Labels labelSettingsR2 = new Labels("Settings");
    labelSettingsR2.setId("labelBackgroundBlueTopCenter");

    Labels labelTextSelectDifficultyR2 = new Labels("Difficulty:");
    labelTextSelectDifficultyR2.setId("labelsNormal");

    Labels labelTextSelectFieldSizeR2 = new Labels("Field Size:");
    labelTextSelectFieldSizeR2.setId("labelsNormal");

    Labels labelTextSelectAmountOfShipsR2 = new Labels("Occupancy\n Rate of Ships:");
    labelTextSelectAmountOfShipsR2.setId("labelsNormal");

    // Screen root container for root2 (order matters for z-index / stacking)
    StackPane root2SingleplayerSettings =
        new StackPane(
            imageviewPlayerVsAiR2,
            buttonGoBackR2,
            labelSettingsR2,
            buttonStartPlacingShipsR2,
            buttonLoadGameR2,
            textfieldSelectFieldSizeR2,
            comboboxesDifficultySelectionR2,
            comboboxesAmountOfShipsR2,
            labelTextSelectDifficultyR2,
            labelTextSelectFieldSizeR2,
            labelTextSelectAmountOfShipsR2);
    root2SingleplayerSettings.setId("root2SingleplayerSettings");

    // Layout root2:

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
    comboboxesDifficultySelectionR2.position(root2SingleplayerSettings, -0.2, -0.18);
    comboboxesDifficultySelectionR2.fontsize(root2SingleplayerSettings, 0.01);
    comboboxesDifficultySelectionR2.size(root2SingleplayerSettings, 0.15, 0.05);

    comboboxesAmountOfShipsR2.set_selections("30%", "15%", "20%", "30%");
    comboboxesAmountOfShipsR2.position(root2SingleplayerSettings, -0.2, 0.12);
    comboboxesAmountOfShipsR2.fontsize(root2SingleplayerSettings, 0.009);
    comboboxesAmountOfShipsR2.size(root2SingleplayerSettings, 0.15, 0.05);

    textfieldSelectFieldSizeR2.position(root2SingleplayerSettings, -0.2, -0.03);
    textfieldSelectFieldSizeR2.fontsize(root2SingleplayerSettings, 0.01);
    textfieldSelectFieldSizeR2.size(root2SingleplayerSettings, 0.15, 0.05);

    labelSettingsR2.position(root2SingleplayerSettings, -0.255, -0.01);
    labelSettingsR2.fontsize(root2SingleplayerSettings, 0.04);
    labelSettingsR2.size(root2SingleplayerSettings, 0.3, 0.65);

    labelTextSelectDifficultyR2.position(root2SingleplayerSettings, -0.347, -0.18);
    labelTextSelectDifficultyR2.fontsize(root2SingleplayerSettings, 0.02);
    labelTextSelectDifficultyR2.size(root2SingleplayerSettings, 0.2, 0.07);

    labelTextSelectFieldSizeR2.position(root2SingleplayerSettings, -0.343, -0.03);
    labelTextSelectFieldSizeR2.fontsize(root2SingleplayerSettings, 0.02);
    labelTextSelectFieldSizeR2.size(root2SingleplayerSettings, 0.2, 0.07);

    labelTextSelectAmountOfShipsR2.position(root2SingleplayerSettings, -0.343, 0.12);
    labelTextSelectAmountOfShipsR2.fontsize(root2SingleplayerSettings, 0.015);
    labelTextSelectAmountOfShipsR2.size(root2SingleplayerSettings, 0.2, 0.1);

    // ================================================================
    // ROOT 3: Multiplayer server list / join screen
    // ================================================================

    // go back to root1
    Buttons buttonGoBackR3 = new Buttons();
    buttonGoBackR3.setId("buttonsGoBack");

    // go to multiplayer settings screen
    Buttons buttonCreateOwnGameR3 = new Buttons("Create own Game");
    buttonCreateOwnGameR3.setId("buttonsNormal");

    // calls discover function to search for available servers
    Buttons buttonRefreshServersR3 = new Buttons("Refresh Servers");
    buttonRefreshServersR3.setId("buttonsNormal");

    Labels labelJoinOtherPlayersR3 = new Labels("Join other players");
    labelJoinOtherPlayersR3.setId("labelBackgroundBlueTopCenter");

    StackPane root3JoinOtherServers =
        new StackPane(
            buttonGoBackR3, labelJoinOtherPlayersR3, buttonCreateOwnGameR3, buttonRefreshServersR3);
    root3JoinOtherServers.setId("root3JoinOtherServers");

    // Layout root3:

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

    // ================================================================
    // ROOT 4: Ship placement screen
    // ================================================================

    // go back to root1
    Buttons buttonEndGameR4 = new Buttons("End Game");
    buttonEndGameR4.setId("buttonsEndGame");

    // go to root5 to shoot
    Buttons buttonStartShootingR4 = new Buttons("Start");
    buttonStartShootingR4.setId("buttonsNormal");

    Labels labelSelectPositionOfYourBoatsR4 = new Labels("Select the position of your boats");
    labelSelectPositionOfYourBoatsR4.setId("labelBackgroundBlueTopCenter");

    Labels labelShipsSpawnWhenBoardInitializedR4 = new Labels("");
    labelShipsSpawnWhenBoardInitializedR4.setId("labelsBackgroundBlueCenter");

    Labels labelPressRToRotateR4 =
        new Labels("Press R after the ship\n has been dropped\n onto the board to Rotate");
    labelPressRToRotateR4.setId("labelsNormal");

    // objects are added in button action of buttonStartGameR2
    StackPane root4PlaceShips = new StackPane();
    root4PlaceShips.setId("root4PlaceShips");

    // Layout root4

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
    labelPressRToRotateR4.fontsize(root4PlaceShips, 0.012);
    labelPressRToRotateR4.size(root4PlaceShips, 0.25, 0.3);

    // ================================================================
    // ROOT 5: Playing screen (shooting boards + save/end)
    // ================================================================

    // go to root1
    Buttons buttonEndGameR5 = new Buttons("End Game");
    buttonEndGameR5.setId("buttonsEndGame");

    // Saves game into a .txt file
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

    // Layout root5

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

    // ================================================================
    // ROOT 6: Multiplayer settings (field size + server name)
    // ================================================================

    // Background / preview image (player vs player)
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

    // Layout root6

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

    // ================================================================
    // ROOT 7: Loading / waiting screen (multiplayer)
    // ================================================================

    // Animated GIF
    Image imageGifOfJetR7 =
        new Image(getClass().getResource("/com/matti/battleship/images/jet.gif").toExternalForm());
    ImageViews imageviewGifOfJetR7 = new ImageViews(imageGifOfJetR7);

    Buttons buttonGoBackR7 = new Buttons();
    buttonGoBackR7.setId("buttonsGoBack");

    Labels labelWaitingForOtherPlayerToJoinR7 = new Labels("Waiting for other player to join");
    labelWaitingForOtherPlayerToJoinR7.setId("labelWaitingScreen1");

    // Text that changes every start with loading animation
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

    // dot loading animation
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

    // Layout root7

    imageviewGifOfJetR7.position(root7LoadingScreen, 0, 0.2);
    imageviewGifOfJetR7.size(root7LoadingScreen, 0.5, 0.5);

    buttonGoBackR7.position(root7LoadingScreen, -0.45, -0.43);
    buttonGoBackR7.fontsize(root7LoadingScreen, 0.01);
    buttonGoBackR7.size(root7LoadingScreen, 0.07, 0.1);

    labelWaitingForOtherPlayerToJoinR7.position(root7LoadingScreen, 0, -0.2);
    labelWaitingForOtherPlayerToJoinR7.fontsize(root7LoadingScreen, 0.035);
    labelWaitingForOtherPlayerToJoinR7.size(root7LoadingScreen, 0.7, 0.2);
    labelWaitingForOtherPlayerToJoinR7.setAlignment(Pos.CENTER);

    labelFunnyInfosR7.position(root7LoadingScreen, 0, 0);
    labelFunnyInfosR7.fontsize(root7LoadingScreen, 0.02);
    labelFunnyInfosR7.size(root7LoadingScreen, 0.4, 0.2);
    labelFunnyInfosR7.setAlignment(Pos.CENTER);

    // ================================================================
    // Button actions / navigation
    // ================================================================

    // ---------------------- Root 1 actions ----------------------
    // Singleplayer: go to settings screen and set playing mode
    buttonSingleplayerR1.setOnAction(
        e -> {
          scene1.setRoot(root2SingleplayerSettings);
          this.playingMode = PlayingMode.VS_AI;
          // this.game = new Game(PlayingMode.VS_AI, new Player("Player", boardSize), ,
          // turn, initialShipSetup)
        });

    // Multiplayer: go to server list and start discovery function
    buttonMultiplayerR1.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
          this.playingMode = PlayingMode.VS_PLAYER;
          discover_servers(root3JoinOtherServers, scene1, root4PlaceShips);
        });

    // ---------------------- Root 2 actions ----------------------
    // Back to root1
    buttonGoBackR2.setOnAction(e -> scene1.setRoot(root1GamemodeSelection));

    // Load saved singleplayer game and jump to playing screen (root5)
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
            root5ShootOnShips.getChildren().clear();
            root5ShootOnShips
                .getChildren()
                .addAll(
                    buttonEndGameR5,
                    labelBackgroundShootingR5,
                    buttonSafeGameR5,
                    labelTextYourSideR5,
                    labelTextEnemySideR5);

            // Create + initialize the playing grids for loaded state
            preparePlayingGridPanes(root5ShootOnShips, this.game);

            // Ensure buttons stay visible on top
            buttonEndGameR5.toFront();
            buttonSafeGameR5.toFront();

            scene1.setRoot(root5ShootOnShips);
          } catch (Exception ex) {
            System.out.println(ex.toString());
          }
        });

    // Shared start handler: used by singleplayer start (root2) and intended for
    // multiplayer start
    // (root6)
    final EventHandler<ActionEvent> startHandler =
        (ActionEvent e) -> {
          Buttons source = (Buttons) e.getSource();

          // Read board size from root2 textfield (singleplayer)
          if (source == buttonStartPlacingShipsR2) {
            if (!textfieldSelectFieldSizeR2.getText().isEmpty()) {
              try {
                this.selected_field_size = Integer.parseInt(textfieldSelectFieldSizeR2.getText());
              } catch (NumberFormatException ex) {
                System.out.println("Invalid field size, default value 10");
                this.selected_field_size = 10;
              }
            }
            // Read board size from root6 textfield (multiplayer)
          } else if (source == buttonStartGameR6) {
            if (!textfieldSelectFieldSizeR6.getText().isEmpty()) {
              try {
                this.selected_field_size = Integer.parseInt(textfieldSelectFieldSizeR6.getText());
              } catch (NumberFormatException ex) {
                System.out.println("Invalid field size, default value 10");
                this.selected_field_size = 10;
              }
            }

            // Sets server Name
            if (!textfieldSelectServerNameR6.getText().isEmpty()) {
              GlobalConnector global = new GlobalConnector();
              global.setServerName(textfieldSelectServerNameR6.getText());
            }
          }

          // Validate board size input
          if (!BoardUtils.isValidBoardSize(this.selected_field_size)) {
            System.out.println("Please select a valid Board size!");
            PlayingUtils.show_pop_up_information(
                root2SingleplayerSettings,
                "Invalid field size,\n Enter a value between 5 and 30",
                3000,
                false);
            return;
          }

          // Rebuild root4 base UI elements
          root4PlaceShips.getChildren().clear();
          root4PlaceShips
              .getChildren()
              .addAll(
                  labelSelectPositionOfYourBoatsR4,
                  labelShipsSpawnWhenBoardInitializedR4,
                  buttonStartShootingR4,
                  labelPressRToRotateR4,
                  buttonEndGameR4);

          String selectedShipShareString =
              comboboxesAmountOfShipsR2.getSelectionModel().getSelectedItem();
          ShipBoardShare tempShipShare = BoardUtils.getShipShareFromString(selectedShipShareString);
          // prepare ship setup for ship placement
          // Generate ship setup for placement (based on board size / occupancy)
          this.initialShipSetup =
              BoardUtils.generateShipSetupForPlacement(this.selected_field_size, tempShipShare);

          // Read selected difficulty from combobox and convert to enum
          String selectedDifficultyString =
              comboboxesDifficultySelectionR2.getSelectionModel().getSelectedItem();
          this.difficulty = GameUtils.getDifficultyFromString(selectedDifficultyString);

          // Create the logical board for placement
          this.board = new Board(selected_field_size);
          this.board.setShipShare(tempShipShare);

          // Create placement grid (visual)
          GridPane battleGrid = new GridPane();

          // Bind grid size to responsive boardSize
          battleGrid.prefWidthProperty().bind(boardSize);
          battleGrid.prefHeightProperty().bind(boardSize);
          battleGrid.minWidthProperty().bind(boardSize);
          battleGrid.minHeightProperty().bind(boardSize);
          battleGrid.maxWidthProperty().bind(boardSize);
          battleGrid.maxHeightProperty().bind(boardSize);

          battleGrid.setStyle("-fx-background-color: transparent;");

          // Create placement cells and drag/drop handlers
          initializePlacementBoard(battleGrid);

          // Create draggable ship rectangles (outside the grid)
          prepareShipRectangles(root4PlaceShips);

          // Add grid to the placement screen and navigate to root4
          root4PlaceShips.getChildren().add(battleGrid);
          StackPane.setAlignment(battleGrid, Pos.CENTER);
          scene1.setRoot(root4PlaceShips);
        };

    buttonStartPlacingShipsR2.setOnAction(startHandler);

    // ---------------------- Root 3 actions ----------------------
    // Back to root1
    buttonGoBackR3.setOnAction(e -> scene1.setRoot(root1GamemodeSelection));
    // Create own game -> open multiplayer settings (root6)
    buttonCreateOwnGameR3.setOnAction(e -> scene1.setRoot(root6MultiplayerSettings));
    // Refresh discovered servers list
    buttonRefreshServersR3.setOnAction(
        e -> discover_servers(root3JoinOtherServers, scene1, root4PlaceShips));

    buttonGoBackR3.setOnAction(e -> scene1.setRoot(root1GamemodeSelection));
    buttonCreateOwnGameR3.setOnAction(e -> scene1.setRoot(root6MultiplayerSettings));
    buttonRefreshServersR3.setOnAction(
        e -> discover_servers(root3JoinOtherServers, scene1, root4PlaceShips));

    // --------------------------------- root 4
    // ---------------------- Root 4 actions ----------------------
    // End placement/game: clear roots and return to root1
    buttonEndGameR4.setOnAction(
        e -> {
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();
          this.board = null;
          scene1.setRoot(root1GamemodeSelection);
        });

    // Start shooting: validate placement, create game, place AI ships, go to root5
    buttonStartShootingR4.setOnAction(
        e -> {
          // prevent starting a game when not all ships have been placed
          if (this.board.getNumberOfOccupiedFields()
              != BoardUtils.getExactNumberOfMandatoryOccupiedFields(
                  this.board.getSize(), this.board.getShipShare())) {
            System.out.println(
                "You can't start a game if you don't have placed all ships on the board!");
            PlayingUtils.show_pop_up_information(
                root4PlaceShips,
                "You can't start a game \n if you don't have placed all ships on the board!",
                4000,
                false);
            e.consume();
            return;
          }

          root5ShootOnShips
              .getChildren()
              .setAll(
                  buttonEndGameR5,
                  labelBackgroundShootingR5,
                  buttonSafeGameR5,
                  labelTextYourSideR5,
                  labelTextEnemySideR5);

          // TODO: Add the case for playing against another player -> no board needs to be
          // added

          // Create opponent board and auto-place ships
          Board opponentBoard = new Board(this.selected_field_size);
          PlacementAlgorithm.placeShipsWithBacktracking(opponentBoard, this.initialShipSetup);
          opponentBoard.setShipShare(this.board.getShipShare());
          Player tempOpponent = new Player("Opponent", this.selected_field_size);
          Player tempPlayer = new Player("Player", this.selected_field_size);
          tempOpponent.board = opponentBoard;
          tempPlayer.board = this.board;

          this.game =
              new Game(
                  this.playingMode,
                  tempPlayer,
                  tempOpponent,
                  PlayerTurn.PLAYER,
                  this.initialShipSetup);

          // determine AI algorithm for the 'VS_AI' playing mode
          if (this.game.getPlayingMode() == PlayingMode.VS_AI) {
            this.aIAlgorithm =
                GameUtils.determineAlgorithmForTheGame(this.difficulty, selected_field_size);
            this.game.setDifficulty(this.difficulty);
          }

          // Build playing grids and sync UI to logical state
          preparePlayingGridPanes(root5ShootOnShips, this.game);

          buttonEndGameR5.toFront();
          buttonSafeGameR5.toFront();
          scene1.setRoot(root5ShootOnShips);
        });

    // ---------------------- Root 5 actions ----------------------
    // End game from playing screen
    buttonEndGameR5.setOnAction(
        e -> {
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();
          this.board = null;
          scene1.setRoot(root1GamemodeSelection);
        });

    // Save game state and fully reset game-related fields
    buttonSafeGameR5.setOnAction(
        e -> {
          // TODO: Get back the file path for multiplayer
          if (!FileWriterService.safeGameStateToFile(this.game, null)) {
            System.out.println("Failed to properly safe the gamestate!");
            PlayingUtils.show_pop_up_information(
                root5ShootOnShips, "Failed to properly safe the gamestate!", 3000, false);
            return;
          }
          // Clear UI roots
          root4PlaceShips.getChildren().clear();
          root5ShootOnShips.getChildren().clear();

          // Reset all gameplay state
          this.board = null;
          this.game = null;
          this.playingMode = null;
          this.difficulty = null;
          this.aIAlgorithm = null;
          this.playerRole = null;
          this.initialShipSetup = null;

          scene1.setRoot(root1GamemodeSelection);
        });

    // ---------------------- Root 6 actions ----------------------
    // Back to server list
    buttonGoBackR6.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
        });

    // Start multiplayer -> show waiting screen (root7)
    // TODO: Add Multiplayer Connection logic
    buttonStartGameR6.setOnAction(
        e -> {
          scene1.setRoot(root7LoadingScreen);
        });

    // TODO: If player joined switch to root5

    // start_game_button_r6.setOnAction(startHandler);

    // ---------------------- Root 7 actions ----------------------
    // Back from waiting screen to server list
    buttonGoBackR7.setOnAction(
        e -> {
          scene1.setRoot(root3JoinOtherServers);
        });

    // ================================================================
    // Stage / Scene setup
    // ================================================================
    // Create window with size 800x600
    scene1 = new Scene(root1GamemodeSelection, 800, 600);

    // include stylesheet
    scene1.getStylesheets().add(getClass().getResource("css/style.css").toExternalForm());

    // Bind boardSize to the smaller window dimension (responsive square board),
    // then scale to 65% of that dimension.
    boardSize.bind(Bindings.min(scene1.widthProperty(), scene1.heightProperty()).multiply(0.65));

    // Stage title and (fav)icon.
    primaryStage.setTitle("Battleship");
    Image icon =
        new Image(
            getClass().getResource("/com/matti/battleship/images/favicon.png").toExternalForm());
    primaryStage.getIcons().add(icon);

    // Attach the scene and show the stage.
    primaryStage.setScene(scene1);
    primaryStage.show();
  }

  // _________________________________________________________________
  // ----- Helper functions -----
  // _________________________________________________________________

  // TODO: Outsource helper functions

  /**
   * Prepares and initializes the playing grid panes for both the player and the opponent within the
   * specified root pane.
   *
   * <p>This method performs the following steps:
   *
   * <ul>
   *   <li>Removes all existing GridPane children from the root pane.
   *   <li>Creates and configures new GridPane instances for the player and opponent boards.
   *   <li>Initializes the opponent's and player's game boards via dedicated methods.
   *   <li>Positions the grids within the UI, binding their size and position properties to scene
   *       dimensions for responsiveness.
   *   <li>Synchronizes the visual representation of the boards with the current game state.
   *   <li>Adds the configured grids to the root pane.
   * </ul>
   *
   * @param root The parent Pane that contains the game boards. Existing GridPane children will be
   *     removed.
   * @param game The current Game instance containing game state information, including player and
   *     opponent boards.
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
   * Updates the opponent's UI board (GridPane) to reflect the current game state of the player's
   * board.
   *
   * <p>The method iterates through each field of the player's board and updates the corresponding
   * UI cell in the opponent's GridPane based on the theoretical state of each field (miss, hit,
   * sunk). It updates visual indicators such as images for misses and hits and styles for sunk
   * ships. If a ship is sunk, it highlights all related cells accordingly.
   *
   * @param opponentPane The GridPane representing the opponent's board UI, which will be updated.
   * @param playerBoard The Board object containing the current state of the player's board.
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
   * Initializes the opponent's game board UI grid within the specified GridPane.
   *
   * <p>This method creates a grid of StackPane cells, binds their sizes to the overall board size
   * for responsiveness, and applies styling such as borders and background images. Each cell is
   * associated with a {@link PlayerBoardCellContext} for storing state information.
   *
   * @param pane The GridPane to populate with cells representing the opponent's game board.
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
   * Initializes the player's game board UI grid within the specified GridPane.
   *
   * <p>This method creates a grid of Buttons representing each field on the player's board. Each
   * button is styled, bound to the board size for responsiveness, and configured with an action to
   * handle shooting at the opponent when clicked. It also loads images for hit and miss indicators
   * and handles the game logic for shooting, including updating the UI based on shot results.
   *
   * @param pane The GridPane where the player's game board buttons will be added.
   * @param opponentPane The GridPane of the opponent's board, which will be updated after each
   *     shot.
   * @param root The root Pane of the scene, used for accessing game state and managing game flow.
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
                case MISS -> {
                  iv = new ImageViews(imgMiss);

                  iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.setPreserveRatio(false);

                  btn.setGraphic(iv);
                }
                case HIT -> {
                  iv = new ImageViews(imgHit);

                  iv.fitWidthProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.fitHeightProperty().bind(BUTTON_SIZE.multiply(0.4));
                  iv.setPreserveRatio(false);

                  btn.setGraphic(iv);
                }
                case SUNK -> applyChangesToButtonsAfterShipSunk(pane, coordinates);
                case INVALID -> System.out.println("Invalid shot! Please try again!");
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
   * Updates the UI buttons on the player's grid after a ship has been sunk.
   *
   * <p>This method highlights the fields of the sunk ship and marks the surrounding fields with a
   * miss indicator. It modifies the style of the ship's fields to indicate they are sunk and sets
   * miss images around the ship.
   *
   * @param gridPane The GridPane containing the buttons representing the player's board.
   * @param coordinates The coordinates of the sunk ship's position.
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
   * Rotates and positions a Rectangle representing a ship on the GridPane based on the specified
   * direction.
   *
   * <p>This method adjusts the rectangle's position, size, and rotation to match the new
   * orientation, ensuring the ship fits within the grid boundaries. It also updates the ship's
   * image to reflect the rotation.
   *
   * @param shipRect The Rectangle representing the ship on the grid.
   * @param row The starting row index for positioning the ship.
   * @param col The starting column index for positioning the ship.
   * @param shipLength The length of the ship.
   * @param boardSize The size of the game board (number of rows/columns).
   * @param newDirection The new direction (orientation) for the ship (UP, DOWN, LEFT, RIGHT).
   */
  private void rotateRectangleOnGridPane(
      Rectangle shipRect, int row, int col, int shipLength, int boardSize, Direction newDirection) {
    DoubleBinding cs =
        Bindings.createDoubleBinding(
            () -> this.boardSize.get() / selected_field_size, this.boardSize);
    double rotationAngle = 0.;

    switch (newDirection) {
      case DOWN -> {
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
      }
      case UP -> {
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
      }
      case RIGHT -> {
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
      }
      case LEFT -> {
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
      }
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
   * Prepares and initializes the ship rectangles for the initial setup of the game.
   *
   * <p>This method creates draggable ship rectangles representing each ship based on their lengths,
   * applies visual styling, binds their size properties to the grid, and sets up drag-and-drop
   * behavior including rotation via keyboard input (R key). Ships can be rotated during placement
   * by pressing R, which temporarily removes the ship, updates its orientation, and attempts to
   * place it again.
   *
   * @param root The Pane container to which the ship rectangles are added.
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
                  if (e.getCode() == KeyCode.R) {
                    ShipGridElement data = (ShipGridElement) ship.getUserData();
                    if (!data.isPlaced()) {
                      System.out.println("Can't rotate ship if it wasn't placed on the board yet!");
                      PlayingUtils.show_pop_up_information(
                          root,
                          "Only try to rotate ships after you placed them on the board!",
                          3000,
                          false);
                      return;
                    }
                    Direction oldDirection = data.getDirection();
                    Direction newDir =
                        (oldDirection == Direction.RIGHT) ? Direction.DOWN : Direction.RIGHT;

                    data.setDirection(newDir);
                    ship.setUserData(data);

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
      case DOWN -> {
        int finalColD = col;
        int finalRowD = row;
        if (row + (length - 1) > boardSize - 1) {
          finalRowD = boardSize - length;
        }
        GridPane.setRowIndex(rect, finalRowD);
        GridPane.setColumnIndex(rect, finalColD);
        GridPane.setRowSpan(rect, length);
        GridPane.setColumnSpan(rect, 1);
      }
      case UP -> {
        int finalColU = col;
        int finalRowU = row;
        if (row - (length - 1) < 0) {
          finalRowU = length;
        }
        GridPane.setRowIndex(rect, finalRowU);
        GridPane.setColumnIndex(rect, finalColU);
        GridPane.setRowSpan(rect, length);
        GridPane.setColumnSpan(rect, 1);
      }
      case RIGHT -> {
        int finalColR = col;
        if (col + (length - 1) > boardSize - 1) {
          finalColR = boardSize - length;
        }
        int finalRowR = row;
        GridPane.setRowIndex(rect, finalRowR);
        GridPane.setColumnIndex(rect, finalColR);
        GridPane.setRowSpan(rect, 1);
        GridPane.setColumnSpan(rect, length);
      }
      case LEFT -> {
        int finalColL = col;
        if (col - (length - 1) < 0) {
          finalColL = length;
        }
        int finalRowL = row;
        GridPane.setRowIndex(rect, finalRowL);
        GridPane.setColumnIndex(rect, finalColL);
        GridPane.setRowSpan(rect, 1);
        GridPane.setColumnSpan(rect, length);
      }
    }
    GridPane.setHalignment(rect, javafx.geometry.HPos.CENTER);
    GridPane.setValignment(rect, javafx.geometry.VPos.CENTER);
  }

  /**
   * Discovers available servers on the network, displays the results on the provided root pane, and
   * creates buttons for each discovered server to allow the user to connect.
   *
   * <p>This method uses a client discovery scanner to find servers within a specified timeout. If
   * no servers are found, it displays a message indicating so. If servers are discovered, it
   * creates up to nine buttons arranged in a grid, each representing a server, with an action to
   * initiate connection (currently commented out).
   *
   * @param root The Pane container where server discovery results and buttons will be displayed.
   * @param scene The Scene associated with the UI, used for scene management.
   * @param destinationPane The Pane to switch to upon connecting to a selected server.
   */
  private void discover_servers(Pane root, Scene scene, Pane destinationPane) {
    int port = EnvConfig.getPort();
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
        String host_name = list_of_discovered_servers.get(i).host(); // optional, if
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
        join_server_button.setOnAction(
            e -> {
              // TODO: Complete socket implementation needs to be reworked -> can't get data
              // out of scope IMessageListener.onMessageReceived

              // this.connector = new GlobalConnector();
              // try {
              // this.connector.startAsClient(host_name);
              // int currentSetup = 1;
              // final int[] tempBoardSize = { -1 };
              // IMessageListener listener = new IMessageListener() {
              // @Override
              // public void onMessageReceived(String mes) {
              // Message message = MessageParser.parse(mes);
              // switch (message.getType()) {
              // case SIZE -> {
              // tempBoardSize[0] = message.getIntArg(0);
              //
              // break;
              // }
              // }
              // }
              //
              // @Override
              // public void onConnectionClosed(Exception e) {
              // }
              //
              // };
              // this.connector.setMessageListener(listener);
              // this.connector.listenLoop();
              //
              // scene.setRoot(destinationPane);
              // } catch (Exception ex) {
              // System.out.println(ex.getMessage());
              // return;
              // }
            });
      }
    }
  }

  // Entry Point -> main function
  public static void main(String[] args) {
    launch(args);
  }
}
