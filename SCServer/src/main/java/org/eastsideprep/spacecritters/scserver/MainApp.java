package org.eastsideprep.spacecritters.scserver;

import java.util.ArrayList;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.eastsideprep.spacecritters.gamelog.GameLog;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.scgamelog.SCGameLogEntry;
import org.eastsideprep.spacecritters.scgamelog.SCGameState;
import org.eastsideprep.spacecritters.spacecritters.SpaceCritters;
import spark.Request;
import static spark.Spark.*;

public class MainApp extends Application {

    static SpaceCritters sc;
    public static SCGameState scState;
    public static GameLog log;

    @Override
    public void start(Stage stage) throws Exception {
        sc = new SpaceCritters();

        try {
            sc.start(stage);
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }
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
        initLog();

        launch(args);
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

    private static void initLog() {
        scState = new SCGameState(0, 0);
        log = new GameLog(scState);
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
                ctx.observer = log.addObserver(ctx);
            }
            SCGameState state = SCGameState.safeGetNewState(ctx.observer);
            return new int[]{ctx.observer.hashCode(), state.entries, state.totalTurns};

        } catch (Exception e) {
            System.out.println("exception in GIS");
        }
        return null;
    }
   private static String detach(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer != null) {
                 log.removeObserver(ctx);
                 ctx.observer = null;
            }
        } catch (Exception e) {
            System.out.println("detach exception");
            return "detach: exception in RO";
        }
        return "detach success";
    }

 
    private static int[] updates(Request req) {
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {
                return null;
            }

            ArrayList<GameLogEntry> list = ctx.observer.getNewItems();
            // Convert list into something GSOn can convert;
            int[] a = new int[list.size()];
            for (int i = 0; i < a.length; i++) {
                SCGameLogEntry item = (SCGameLogEntry) list.get(i);
                a[i] = item.turn;
            }
            return a;

        } catch (Exception e) {
            System.out.println("exception in GU");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
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

}
