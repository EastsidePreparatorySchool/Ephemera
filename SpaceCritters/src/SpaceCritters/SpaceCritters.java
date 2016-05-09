/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import gameengineV1.GameEngineV1;
import gameengineinterfaces.GameCommand;
import gameengineinterfaces.GameCommandCode;
import gameengineinterfaces.GameElementSpec;
import gamelogic.Constants;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import static javafx.application.Application.launch;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 *
 * @author gmein
 */
public class SpaceCritters extends Application {

    public static boolean gameOver = false;
    public static GameEngineV1 engine;
    public static VisualizationGrid field;
    public static SpeciesSet species;
    public static SpectrumColor spectrum = new SpectrumColor();
    public static Stage dstage;
    Stage stage;
    static SpaceCritters currentInstance; // kludge until rework is done
    ControlPane controlPane; // view and game controls
    Stage consoleStage; // the window for the console
    ConsolePane consolePane; // the console object with the print API
    Scene3D mainScene; // the object controlling the main 3D display, not actually a scene (yet)

    @Override
    public void start(Stage stage) throws IOException {

        try {
            dstage = createSplashScreen();

            // kludge to enable otherwise static methods
            currentInstance = this;

            // Constants from current Ephemera game
            int width = Constants.width;
            int height = Constants.height;
            this.stage = stage;

            // keep track of species, need this before constructing UI
            species = new SpeciesSet();

            // get screen geometry
            javafx.geometry.Rectangle2D screenBounds;
            screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setTitle("SpaceCritters V0.90");
            setSize(stage, screenBounds);
            

            BorderPane border = new BorderPane();
            border.setStyle("-fx-background-color: black;");
            controlPane = new ControlPane(this);
            border.setRight(controlPane);

            // debug output console
            consoleStage = new Stage();
            consolePane = new ConsolePane(this);
            Scene cScene = new Scene(consolePane);
            consoleStage.setScene(cScene);
            consoleStage.setAlwaysOnTop(true);

            // construct path to important game folders
            String gamePath = System.getProperty("user.dir");
            gamePath = gamePath.toLowerCase();

            if (gamePath.contains("ephemera" + System.getProperty("file.separator") + "spacecritters")) {
                // probably started from netbeans
                gamePath = gamePath.substring(0, gamePath.toLowerCase().indexOf("spacecritters"));
            } else {
                // probably started from other folder
                gamePath = System.getProperty("user.dir");
                gamePath = gamePath.toLowerCase();
                gamePath += System.getProperty("file.separator");
            }

            String alienPath = gamePath + "aliens";
            String logPath = gamePath + "logs";

            // make sure these exist
            Utilities.createFolder(logPath);
            Utilities.createFolder(alienPath);

            alienPath += System.getProperty("file.separator");
            logPath += System.getProperty("file.separator");

            //
            // initialize Ephemera game engine and visualizer
            //
            //get some objects created (not initialized, nothing important happens here)
            engine = new GameEngineV1();
            this.field = new VisualizationGrid();

            // now initialize
            this.field.init(this, engine, consolePane, species, logPath, width, height);
            engine.initFromFile(field, gamePath, alienPath, "sc_config.csv");

            // need field to be alive before constructing this
            mainScene = new Scene3D(this, (int)screenBounds.getWidth() - 220, (int)screenBounds.getHeight() - 20);

            // set a hook to shut down engine on game exit
            stage.setOnCloseRequest(e -> handleExit());

            // set scene and stage

            border.setCenter(mainScene.outer);
            Scene scene = new Scene(border);
            scene.setOnKeyPressed((e) -> mainScene.controlCamera(e));
            mainScene.outer.requestFocus();

            stage.setScene(scene);

            stage.show();

            // show the splash sceen unless we have autostart
            if (!(boolean) Constants.getValue("autoStart")) {
                dstage.show();
            }
            // and tell the engine (and through it, the shell) that we are done adding elements
            engine.queueCommand(new GameCommand(GameCommandCode.Ready));
        } catch (Exception e) {
            System.out.println(e.toString());
            System.in.read();
        }
    }

    private Stage createSplashScreen() {
        Stage dialog = new Stage();
        dialog.setTitle("About SpaceCritters");
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.initModality(Modality.NONE);
        dialog.setAlwaysOnTop(true);

        javafx.geometry.Rectangle2D screenBounds;
        screenBounds = Screen.getPrimary().getBounds();

        final Rectangle rect1 = new Rectangle(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
        rect1.setFill(Color.BLACK);

        final Rectangle clip = new Rectangle(0, 0, screenBounds.getWidth(), screenBounds.getHeight());
        Image images = new Image(getClass().getResourceAsStream("backdrop" + System.nanoTime() % 4 + ".jpg"));
        ImageView vs = new ImageView();
        vs.setImage(images);
        vs.setFitWidth(screenBounds.getWidth());
        vs.setFitHeight(screenBounds.getHeight());
        vs.setPreserveRatio(true);
        vs.setSmooth(true);
        vs.setCache(true);
        vs.setOpacity(0.4);
        vs.setClip(clip);

        Image image = new Image(getClass().getResourceAsStream("splash.png"));
        ImageView v = new ImageView();
        v.setImage(image);
        v.setFitWidth(500);
        v.setPreserveRatio(true);
        v.setSmooth(true);
        v.setCache(true);

        Text t1 = new Text("SpaceCritters");
        t1.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        t1.setFill(Color.GOLD);

        Text tw = new Text("www.spacecritters.org");
        tw.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        tw.setFill(Color.GOLD);

        Text t2 = new Text("Design and code by GM, GU, MM, SFK");
        t2.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        t2.setFill(Color.GOLD);

        Text t3 = new Text("Aliens by AB, JB");
        t3.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        t3.setFill(Color.GOLD);

        Text t4 = new Text("Artwork " + '\u00A9' + " 2016 Lara Lewison");
        t4.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        t4.setFill(Color.GOLD);

        Text t5 = new Text("Backdrop courtesy of NASA");
        t5.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        t5.setFill(Color.GOLD);

        VBox vb1 = new VBox();
        vb1.setStyle("-fx-background-color: rgba(0,0,0,0.0)");
        vb1.getChildren().addAll(t1, tw, t2, t3, t4, t5);
        vb1.setPadding(new Insets(15, 12, 15, 12));
        vb1.setSpacing(8);
        vb1.setAlignment(Pos.BOTTOM_RIGHT);
        vb1.setPrefHeight(v.getFitHeight());

        StackPane sp1 = new StackPane();
        sp1.getChildren().addAll(v, vb1);
        sp1.setMaxWidth(v.getFitWidth());
        sp1.setMaxHeight(v.getFitHeight());

        VBox vb2 = new VBox();
        vb2.setStyle("-fx-background-color: rgba(0,0,0,0.0)");
        vb2.getChildren().addAll(rect1);
        vb2.setPrefSize(screenBounds.getWidth(), screenBounds.getHeight());

        StackPane sp2 = new StackPane();
        sp2.getChildren().addAll(vb2, vs, sp1);

        Scene dscene = new Scene(sp2);
        //dialog.setFullScreen(true);
        dscene.setOnMouseClicked((e) -> dialog.close());
        //dscene.setOnKeyPressed((e) -> dialog.close()); //doesn't work

        //dscene.setFill(null);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setScene(dscene);
        //dialog.show();
        //dialog.setFullScreen(true);
        return dialog;
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


    /*
     * Creates an HBox with two buttons for the top region
     */
    ListView<AlienSpeciesForDisplay> addAlienSpeciesList() {
        ListView<AlienSpeciesForDisplay> speciesView = new ListView<>(species.getObservableList());

        speciesView.setCellFactory((ListView<AlienSpeciesForDisplay> list) -> new SpeciesListCell());
        speciesView.setStyle("-fx-background-color: black;");

        return speciesView;
    }

    void startGame() {
        // first start
        Iterator<AlienSpeciesForDisplay> iter = species.speciesList.iterator();
        while (iter.hasNext()) {
            AlienSpeciesForDisplay as = iter.next();
            if (as.isOn()) {
                GameElementSpec element = new GameElementSpec("ALIEN", as.domainName, as.packageName, as.className,
                        null); // state
                engine.queueCommand(new GameCommand(GameCommandCode.AddElement, element));
            } else {
                iter.remove();
            }
        }

        engine.queueCommand(new GameCommand(GameCommandCode.Resume));
        controlPane.buttonPause.setText("Pause");

    }

    void startOrPauseGame(ActionEvent e) {
        if (controlPane.buttonPause.getText().equals("Pause")) {
            // pause
            engine.queueCommand(new GameCommand(GameCommandCode.Pause));
            controlPane.buttonPause.setText("Resume");
        } else if (controlPane.buttonPause.getText().equals("Start")) {
            startGame();
        } else {
            // regular resume
            engine.queueCommand(new GameCommand(GameCommandCode.Resume));
            controlPane.buttonPause.setText("Pause");
        }
    }


    // doesn't really work
    private void restart() {
        engine.queueCommand(new GameCommand(GameCommandCode.End));
        stage.close();
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
            for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                cmd.append(jvmArg + " ");
            }
            cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
            cmd.append(SpaceCritters.class.getName()).append(" ");
            Runtime.getRuntime().exec(cmd.toString());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}
