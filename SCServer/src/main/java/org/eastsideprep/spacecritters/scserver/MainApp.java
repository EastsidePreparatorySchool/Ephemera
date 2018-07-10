package org.eastsideprep.spacecritters.scserver;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eastsideprep.spacecritters.gameengineimplementation.GameEngineV2;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommand;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommandCode;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameElementSpec;
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
    static GameEngineV2 ge;
    static HashMap<String, GameEngineV2> engines;

    @Override
    public void start(Stage stage) throws Exception {
        MainApp.engines = new HashMap<>();
        MainApp.ge = createDesktopGameEngine(stage);
        engines.put("main", MainApp.ge);
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
        get("/start", "application/json", (req, res) -> doStart(req), new JSONRT());
        get("/pause", "application/json", (req, res) -> doPause(req), new JSONRT());
        get("/listengines", "application/json", (req, res) -> doListEngines(req), new JSONRT());
        get("/create", "application/json", (req, res) -> doCreateEngine(req), new JSONRT());
        get("/attach", "application/json", (req, res) -> doAttach(req), new JSONRT());
        get("/detach", "application/json", (req, res) -> doDetach(req), new JSONRT());
        get("/updates", "application/json", (req, res) -> doUpdates(req), new JSONRT());
    }

    // ROUTES
    private static String doStart(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "SC: doStart: not attached";
        }
        ctx.engine.queueCommand(new GameCommand(GameCommandCode.Resume));

//        Platform.runLater(() -> sc.startOrResumeGame());
        return "SC: Start/resume request queued";
    }

    private static String doPause(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "SC: doStart: not attached";
        }
        ctx.engine.queueCommand(new GameCommand(GameCommandCode.Pause));
//        Platform.runLater(() -> sc.pauseGame());
        return "SC: Pause request queued";
    }

    private static String[] doListEngines(Request req) {
        String[] result = new String[engines.size()];
        for (String s : engines.keySet()) {
            System.out.println("Listing engine: '" + s + "'");
        }
        return engines.keySet().toArray(result);
    }

    private static String doCreateEngine(Request req) {
        String name = req.queryParams("name");

        // cannot create main engine after the fact todo: rethink that.
        if (name.equalsIgnoreCase("main")) {
            System.err.println("Exception in doCreateEngine: can't create main engine from web interface");
            return null;
        }

        GameEngineV2 eng = MainApp.engines.get(name);
        if (eng != null) {
            System.err.println("Exception in doCreateEngine: engine '" + name + "'exists already");
            return null;
        }

        try {
            eng = createServerGameEngine(name);
        } catch (Exception e) {
            System.err.println("Exception in doCreateEngine: " + e.getMessage());
        }
        engines.put(name, eng);
        return name;
    }

    public static class AttachRecord {

        String engine;
        int observer;
        int turns;

        AttachRecord(String n, int o, int t) {
            engine = n;
            observer = o;
            turns = t;
        }
    }

    private static AttachRecord doAttach(Request req) {
        String engineRequest;
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {

                engineRequest = req.queryParams("engine");
                if (engineRequest == null || engineRequest.equals("")) {
                    engineRequest = "main";
                }

                ctx.engine = MainApp.engines.get(engineRequest);
                if (ctx.engine == null) {
                    System.out.println("doAttach: Engine '" + engineRequest + "'not found");
                    return null;
                }
                ctx.observer = ctx.engine.log.addObserver(ctx);
            }
            SCGameState state = (SCGameState) ctx.observer.getInitialState();
            return new AttachRecord(ctx.engine.name, ctx.observer.hashCode(), state.totalTurns);

        } catch (Exception e) {
            System.out.println("exception in doAttach");
            e.printStackTrace(System.out);
        }
        return null;
    }

    private static String doDetach(Request req) {
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

    private static SCGameLogEntry[] doUpdates(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {
                return null;
            }

            ArrayList<GameLogEntry> list = ctx.observer.getNewItems();

            if (req.queryParams("compact").equals("yes")) {
                // if the client requests it, make a new game state, 
                // we will use it to compact the entries
                
                SCGameState compactor = new SCGameState();
                for (GameLogEntry item : list) {
                    compactor.addEntry(item);
                }
                list = compactor.getCompactedEntries();
            }
            // now convert into array to JSON knows what to do
            SCGameLogEntry[] array = new SCGameLogEntry[list.size()];
            return list.toArray(array);

        } catch (Exception e) {
            System.out.println("exception in GU");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

    static private GameEngineV2 createDesktopGameEngine(Stage stage) {
        MainApp.sc = new SpaceCritters();
        try {
            MainApp.sc.start(stage);
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        System.out.println("createDesktopEngine: created and initialized, processing");

        return sc.engine;
    }

    static private GameEngineV2 createServerGameEngine(String name) {
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
        System.out.println("createServerEngine: created folders and paths");

        //
        // initialize Ephemera game engine and visualizer
        //
        //get some objects created (not initialized, nothing important happens here)
        GameEngineV2 engine = new GameEngineV2(name);
        System.out.println("createServerEngine: created game engine");
        //
        // test: viz streamer for web version
        //
        LoggingVisualizer streamer = new LoggingVisualizer(engine.log);
        streamer.init();
        System.out.println("createServerEngine: initialized streamer log");

        // and engine
        engine.init(streamer, gamePath, alienPath);
        System.out.println("createServerEngine: initialized game engine");

        // read config
        GameElementSpec[] elements = engine.readConfigFile("sc_config.json");
        engine.processGameElements(elements);
        // load a game and process it
        elements = engine.readConfigFile(Constants.gameMode);
        engine.processGameElements(elements);
        System.out.println("createServerEngine: processing configuration files");

        engine.queueCommand(new GameCommand(GameCommandCode.Ready));
        System.out.println("createServerEngine: queued ready");

        return engine;
    }

}
