/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import gameengineV1.GameEngineV1;
import gameengineinterfaces.GameCommand;
import gameengineinterfaces.GameCommandCode;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Screen;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;

/**
 *
 * @author gmein
 */
public class GUIShell extends Application {

    public static boolean gameOver = false;
    public static Canvas canvas;
    public static GameEngineV1 engine;
    public static VisualizationGrid field;
    public static SpeciesSet species;
    public static Text turnCounterText;
    public static Button buttonPause;

    @Override
    public void start(Stage stage) {

        // Constants from current Ephemera game
        int width = 500;
        int height = 500;

        // get screen geometry
        javafx.geometry.Rectangle2D screenBounds;
        double w = Screen.getPrimary().getDpi();
        screenBounds = Screen.getPrimary().getVisualBounds();

        // for most screens, 1500x500 will display nicely
        // TODO: Make this adapt to available space (from bounds)
        int cellWidth = screenBounds.getWidth() < 1900 ? 2 : 3;
        int cellHeight = 1;

        // keep track of species, need this before constructing UI
        species = new SpeciesSet();

        // set up main window
        stage.setTitle("Ephemera GUI Shell V0.1");
        setSize(stage, screenBounds);

        // Use a border pane as the root for scene
        BorderPane border = new BorderPane();
        border.setStyle("-fx-background-color: black;");

        // add top, left and right boxes
        border.setTop(addTopBox());
        border.setLeft(addLeftBox());
        border.setRight(addFlowPane());
        //border.setBottom(addBottomBox());
        ConsolePane console = new ConsolePane();
        border.setBottom(console);

        // add a center pane
        this.canvas = new Canvas(width * cellWidth, height * cellHeight);
        canvas.setStyle("-fx-border-color: red;");
        border.setCenter(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);
        gc.strokeOval(0.5, 0.5, width * cellWidth - 0.5, height * cellHeight - 0.5);

        //
        // initialize Ephemera game engine and visualizer
        //
        //get some objects created (not initialized, nothing important happens here)
        engine = new GameEngineV1();

        // can override the path for class jar files in arguments
        String gamePath = System.getProperty("user.dir");
        gamePath = gamePath.toLowerCase();

        if (gamePath.contains("ephemera" + System.getProperty("file.separator") + "guishell")) {
            // probably started from netbeans
            gamePath = gamePath.substring(0, gamePath.indexOf("guishell"));
        } else {
            // probably started from other folder
            gamePath = System.getProperty("user.home");
            gamePath = gamePath.toLowerCase();
            gamePath += System.getProperty("file.separator")
                    + "ephemera";
        }

        String alienPath = gamePath
                + "aliens";

        String logPath = gamePath
                + "logs";

        Utilities.createFolder(gamePath);
        Utilities.createFolder(alienPath);
        Utilities.createFolder(logPath);

        gamePath += System.getProperty("file.separator");
        alienPath += System.getProperty("file.separator");
        logPath += System.getProperty("file.separator");

        // init visualizer
        this.field = new VisualizationGrid();
        this.field.init(engine, console, species, logPath, width, height, cellWidth, cellHeight, canvas);

        // get engine up and running
        engine.initFromFile(field, gamePath, alienPath, "ephemera_initial_setup.csv");

        // set a hook to shut down engine on game exit
        stage.setOnCloseRequest(e -> handleExit());

        // set scene and stage
        Scene scene = new Scene(border);

        stage.setScene(scene);
        stage.show();

        //console.println("Test");
        // give it a go - commented out because we start this with the start button
        // engine.queueCommand(new GameCommand(GameCommandCode.Resume));
        // return to platform and wait for events
    }
    // handle shutdown gracefully

    public void handleExit() {
        engine.queueCommand(new GameCommand(GameCommandCode.End));
        field.showGameOver(); // closes logfile
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    //
    // Visual helpers
    //

    public static void setSize(Stage stage, javafx.geometry.Rectangle2D bounds) {
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

    }

    public static void armPauseButton() {
        buttonPause.setDisable(false);
    }

    /*
     * Creates an HBox with two buttons for the top region
     */
    private VBox addTopBox() {

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(15, 12, 15, 12));
        vbox.setSpacing(10);   // Gap between nodes
        vbox.setStyle("-fx-background-color: black;");

        buttonPause = new Button("Start");
        buttonPause.setPrefSize(100, 20);
        buttonPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (buttonPause.getText().equals("Pause")) {
                    engine.queueCommand(new GameCommand(GameCommandCode.Pause));
                    buttonPause.setText("Resume");
                    //buttonPause.setDisable(true);
                } else {
                    engine.queueCommand(new GameCommand(GameCommandCode.Resume));
                    buttonPause.setText("Pause");
                }
            }
        });

        //Button buttonProjected = new Button("Stop");
        //buttonProjected.setPrefSize(100, 20);
        vbox.getChildren().addAll(buttonPause);
        GUIShell.turnCounterText = new Text("Turns completed: 0");
        GUIShell.turnCounterText.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        GUIShell.turnCounterText.setStyle("-fx-background-color: black;");
        GUIShell.turnCounterText.setFill(Color.WHITE);

        vbox.getChildren().add(GUIShell.turnCounterText);

        return vbox;
    }

    /*     * Creates an HBox for the bottom region

     */
    private HBox addBottomBox() {

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);   // Gap between nodes
        hbox.setStyle("-fx-background-color: black;");
        return hbox;
    }

    ListView<AlienSpeciesForDisplay> addAlienSpeciesList() {
        ListView<AlienSpeciesForDisplay> speciesView = new ListView<>(species.getObservableList());

        speciesView.setCellFactory((ListView<AlienSpeciesForDisplay> list) -> new SpeciesListCell());
        speciesView.setStyle("-fx-background-color: black;");

        return speciesView;
    }

    /*
     * Creates a VBox for the left region
     */
    private VBox addLeftBox() {

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10)); // Set all sides to 10
        vbox.setSpacing(8);              // Gap between nodes

        vbox.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        vbox.setPrefWidth(350);

        // add list of alien species
        vbox.getChildren().add(this.addAlienSpeciesList());

        return vbox;
    }

    /*
     * Creates a horizontal flow pane for the right side
     */
    private FlowPane addFlowPane() {

        FlowPane flow = new FlowPane();
        flow.setPadding(new Insets(5, 0, 5, 0));
        flow.setVgap(4);
        flow.setHgap(4);
        flow.setPrefWrapLength(170); // preferred width allows for two columns
        flow.setStyle("-fx-background-color: black;");

        return flow;
    }

    /*
     * Creates a horizontal (default) top tile pane 
     */
    private TilePane addTilePane() {

        TilePane tile = new TilePane();
        tile.setPadding(new Insets(5, 0, 5, 0));
        tile.setVgap(4);
        tile.setHgap(4);
        tile.setPrefColumns(2);
        tile.setStyle("-fx-background-color: black;");

        return tile;
    }
}
