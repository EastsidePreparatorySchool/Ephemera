package org.eastsideprep.spacecritters.scserver;

import java.util.ArrayList;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eastsideprep.spacecritters.gameengineimplementation.GameEngineV2;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameElementSpec;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameEngine;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.scgamelog.SCGameLogEntry;
import org.eastsideprep.spacecritters.scgamelog.SCGameState;
import org.eastsideprep.spacecritters.spacecritters.SpaceCritters;
import org.eastsideprep.spacecritters.spacecritters.Utilities;
import spark.Request;
import static spark.Spark.*;

public class MainApp extends Application {

    static SpaceCritters sc;
    static GameEngine ge;

    @Override
    public void start(Stage stage) throws Exception {
        this.ge = createDesktopGameEngine(stage);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "org.eastsideprep.spacecritters.spacecritters.SplashScreenLoader");

        initSpark();

        launch(args);
    }

    // context helper
    private static ServerContext getCtx(Request req) {
        ServerContext ctx = req.session().attribute("ServerContext");
        if (req.session().isNew()) {
            ctx = new ServerContext();
            req.session().attribute("ServerContext", ctx);
        }

        return ctx;
    }

    /// initialization
    private static void initSpark() {
        staticFiles.location("/static");
        get("/start", "application/json", (req, res) -> doStart(), new JSONRT());
        get("/pause", "application/json", (req, res) -> doPause(), new JSONRT());
        get("/shutdown", "application/json", (req, res) -> shutdown(), new JSONRT());
        get("/attach", "application/json", (req, res) -> attach(req), new JSONRT());
        get("/detach", "application/json", (req, res) -> detach(req), new JSONRT());
        get("/updates", "application/json", (req, res) -> updates(req), new JSONRT());
    }

    // ROUTES
    private static String doStart() {
        Platform.runLater(() -> sc.startOrResumeGame());
        return "SC: Start/resume request queued";
    }

    private static String doPause() {
        Platform.runLater(() -> sc.pauseGame());
        return "SC: Pause request queued";
    }

    private static String shutdown() {
        Platform.runLater(() -> sc.handleExit());
        return "SC: shutdown initiated";
    }

    private static int[] attach(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {
                // todo: let the user submit an engine to attach to, in the request
                ctx.engine = sc.engine;
                ctx.observer = ctx.engine.log.addObserver(ctx);
            }
            SCGameState state = SCGameState.safeGetNewState(ctx.observer);
            return new int[]{ctx.observer.hashCode(), state.totalTurns, state.numAliens};

        } catch (Exception e) {
            System.out.println("exception in GIS");
        }
        return null;
    }

    private static String detach(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer != null) {
                ctx.engine.log.removeObserver(ctx);
                ctx.observer = null;
            }
        } catch (Exception e) {
            System.out.println("detach exception");
            return "detach: exception in RO";
        }
        return "detach success";
    }

    private static SCGameLogEntry[] updates(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {
                return null;
            }

            ArrayList<GameLogEntry> list = ctx.observer.getNewItems();
            // Convert list into something GSOn can convert;
//            int[] a = new int[list.size()];
//            for (int i = 0; i < a.length; i++) {
//                SCGameLogEntry item = (SCGameLogEntry) list.get(i);
//                a[i] = item.turn;
//            }
//            return a;
            SCGameLogEntry[] array = new SCGameLogEntry[list.size()]; 
            return list.toArray(array);

        } catch (Exception e) {
            System.out.println("exception in GU");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

    private GameEngine createDesktopGameEngine(Stage stage) {
        this.sc = new SpaceCritters();
        try {
            sc.start(stage);
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        return sc.engine;
    }

    private GameEngine createServerGameEngine() {
        // construct path to important game folders
        String gamePath = System.getProperty("user.dir");
        gamePath = gamePath.toLowerCase();

        if (gamePath.contains("ephemera" + System.getProperty("file.separator") + "scserver")) {
            // probably started from netbeans
            gamePath = gamePath.substring(0, gamePath.toLowerCase().indexOf("scserver"));
            //TODO: Can't rely on constants before reading config file
            // read config files earlier.Read constants first, then game constants, then init, then read stock elements, game elements
            Constants.searchParentForAliens = false;
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

        //
        // initialize Ephemera game engine and visualizer
        //
        //get some objects created (not initialized, nothing important happens here)
        GameEngineV2 engine = new GameEngineV2();
        //
        // test: viz streamer for web version
        //
        LoggingVisualizer streamer = new LoggingVisualizer(engine.log);
        streamer.init();

        // and engine
        engine.init(streamer, gamePath, alienPath);

        // read config
        GameElementSpec[] elements = engine.readConfigFile("sc_config.json");
        engine.processGameElements(elements);
        // load a game and process it
        elements = engine.readConfigFile(Constants.gameMode);
        engine.processGameElements(elements);

        return engine;
    }

}
