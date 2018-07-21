package org.eastsideprep.spacecritters.scserver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import org.eastsideprep.spacecritters.gameengineimplementation.GameEngineV2;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommand;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameCommandCode;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameElementSpec;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.gamelog.GameLogObserver;
import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.scgamelog.SCGameLogEntry;
import org.eastsideprep.spacecritters.scgamelog.SCGameState;
import org.eastsideprep.spacecritters.spacecritters.SpaceCritters;
import org.eastsideprep.spacecritters.spacecritters.Utilities;
import spark.Request;
import static spark.Spark.*;
import spark.servlet.SparkApplication;

public class MainApp implements SparkApplication {

    static GameEngineV2 geMain;
    static HashMap<String, GameEngineV2> engines;
    static MainApp app;
    static Thread mainThread;
    static boolean createServerEngine = true;

    // desktop initialization
    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        System.out.println("SC main() (scserver) " + dateFormat.format(date));

        //System.setProperty("javafx.preloader", "org.eastsideprep.spacecritters.spacecritters.SplashScreenLoader");
        // invoked from main, start Spark Jetty server
        //staticFiles.location("/static");
        if (args.length == 0 || !args[0].equalsIgnoreCase("desktoponly")) {
            MainApp.app = new MainApp();
            String dir = System.getProperty("user.dir");
            if (dir.toLowerCase().contains("ephemera")) {
                dir += System.getProperty("file.separator") + "target";
            }
            dir += System.getProperty("file.separator") + "static";

            System.out.println("static dir: " + dir);
            port(80);
            staticFiles.externalLocation(dir);

            if (args.length == 0 || !args[0].equalsIgnoreCase("serveronly")) {
                MainApp.createServerEngine = false; // will cause init() to not create server engine, default running mode
            }
            MainApp.app.init();
            mainThread = Thread.currentThread();
        } else {
            System.out.println("SC running in desktop-only mode");
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("serveronly")) {
            System.out.println("SC running in server-only mode");

            try {
                // loop until interrupted
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                // catch and be quiet, then exit
            }
        } else {
            // this will stall until it is time to quit
            MainApp.createDesktopGameEngine();
        }
    }

    /// servlet initialization
    @Override
    public void init() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        System.out.println("SCServer init() " + dateFormat.format(date));

//    	before((request, response) -> {
//    	  System.out.println("Spark request: "+request.url());
//    	});
        get("/protected/start", "application/json", (req, res) -> doStart(req), new JSONRT());
        get("/protected/pause", "application/json", (req, res) -> doPause(req), new JSONRT());
        get("/protected/listengines", "application/json", (req, res) -> doListEngines(req), new JSONRT());
        get("/protected/create", "application/json", (req, res) -> doCreateEngine(req), new JSONRT());
        get("/protected/attach", "application/json", (req, res) -> doAttach(req), new JSONRT());
        get("/protected/detach", "application/json", (req, res) -> doDetach(req), new JSONRT());
        get("/protected/updates", "application/json", (req, res) -> doUpdates(req), new JSONRT());
        get("/protected/check", "application/json", (req, res) -> doCheck(req), new JSONRT());
        get("/protected/listobservers", "application/json", (req, res) -> doListObservers(req), new JSONRT());
        get("/protected/status", "application/json", (req, res) -> status(req), new JSONRT());
        get("/protected/allstatus", "application/json", (req, res) -> allStatus(req), new JSONRT());
        post("/protected/upload", (req, res) -> uploadFile(req, res));

        MainApp.engines = new HashMap<>();

        if (MainApp.createServerEngine) {
            MainApp.geMain = MainApp.createServerGameEngine("main");

            if (MainApp.geMain == null) {
                System.out.println("Could not create initial server game engine");
                return;
            }
        }

        MainApp.engines.put("main", MainApp.geMain);
    }

    // context helper
    private static ServerContext getCtx(Request req) {
        HashMap<String, ServerContext> ctxMap = req.session().attribute("ServerContexts");
        if (req.session().isNew() || ctxMap == null) {
            ctxMap = new HashMap<>();
            req.session().attribute("ServerContexts", ctxMap);
        }

        String client = req.queryParams("clientID");
        ServerContext ctx = ctxMap.get(client);
        if (ctx == null) {
            ctx = new ServerContext();
            ctx.clientSubID = client;
            ctxMap.put(client, ctx);
        }

        // blow up stale contexts
        if (ctx.observer != null && ctx.observer.isStale()) {
            ctx.observer = null;
            return null;
        }

        req.session().maxInactiveInterval(60); // kill this session afte 60 seconds of inactivity
        return ctx;
    }

// ROUTES
    private static String doStart(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "SC: doStart: not attached";
        }
        ctx.engine.queueCommand(new GameCommand(GameCommandCode.Resume));

        return "SC: Start/resume request queued";
    }

    private static String doPause(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "SC: doStart: not attached";
        }
        ctx.engine.queueCommand(new GameCommand(GameCommandCode.Pause));
        return "SC: Pause request queued";
    }

    private static String[] doListEngines(Request req) {
        String[] result = new String[engines.size()];
        for (String s : engines.keySet()) {
            System.out.println("Listing engine: '" + s + "'");
        }
        return engines.keySet().toArray(result);
    }

    private static Object[] doListObservers(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx == null || ctx.observer == null) {
            return null;
        }
        LinkedList<GameLogObserver> observers = ctx.engine.log.getObservers();
        return observers.stream().map((o) -> o.client).toArray();
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
        String observer;
        int turns;
        int observers;

        AttachRecord(String n, String o, int t, int os) {
            engine = n;
            observer = o;
            turns = t;
            observers = os;
        }
    }

    private static AttachRecord doAttach(Request req) {
//        for (String s :req.headers()) {
//            System.out.println("Attach: header: "+s+":"+req.headers(s));
//        }
        String engineRequest;
        int numObservers = 1;
        try {
            ServerContext ctx = getCtx(req);
            if (ctx.observer == null) {

                engineRequest = req.queryParams("engine");
                if (engineRequest == null || engineRequest.equals("")) {
                    engineRequest = "main";
                }

                // make a nice client id string
                ctx.client = req.headers("X-MS-CLIENT-PRINCIPAL-NAME");
                if (ctx.client == null) {
                    ctx.client = req.headers("X-FORWARDED-FOR");
                    if (ctx.client == null) {
                        ctx.client = req.raw().getRemoteAddr();
                    }
                    if (ctx.client.contains(",")) {
                        ctx.client = ctx.client.substring(0, ctx.client.indexOf(","));
                    }
                }
                ctx.client += ":" + ctx.clientSubID;

                ctx.engine = MainApp.engines.get(engineRequest);
                if (ctx.engine == null) {
                    ctx.engine = SpaceCritters.engine;;
                    MainApp.engines.put("main", ctx.engine);
                }

                if (ctx.engine == null) {
                    System.out.println("doAttach: Engine '" + engineRequest + "'not found");
                    return null;
                }
                ctx.observer = ctx.engine.log.addObserver(ctx.client);
                ctx.engineName = engineRequest;
            }
            SCGameState state = (SCGameState) ctx.observer.getInitialState();
            numObservers = ctx.engine.log.getObservers().size();
            return new AttachRecord(ctx.engine.name, ctx.client, state.totalTurns, numObservers);

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
                ctx.engine.log.removeObserver(ctx.observer);
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

                SCGameState compactor = new SCGameState(true);
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

    static private void createDesktopGameEngine() {
        try {
            SpaceCritters.main(new String[]{});
            System.out.println("createDesktopEngine: created and initialized, processing");
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }
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
            gamePath += System.getProperty("file.separator");
            gamePath += "webapps";
            gamePath += System.getProperty("file.separator");
            gamePath += "SCServer-1.0";
            gamePath += System.getProperty("file.separator");
        }

        String alienPath = gamePath + "aliens";
        String logPath = gamePath + "logs";

        // make sure these exist
        Utilities.createFolder(logPath);
        Utilities.createFolder(alienPath);

        alienPath += System.getProperty("file.separator");
        System.out.println("createServerEngine: created folders and paths");
        System.out.println("createServerEngine:alien path is " + alienPath);

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

    public static long uploadFile(spark.Request request, spark.Response response) {
        ServerContext ctx = getCtx(request);
        if (ctx.observer == null) {
            return 0;
        }

        System.out.println("upload ...");
        MultipartConfigElement multipartConfigElement
                = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

        long size = 0;
        try {
            Part file = request.raw().getPart("jarfile");
            String domain = "uploadedaliens";
//            String domain = request.raw().getPart("domain") or such; 
            // make sure domain folder exists
            String domainFolder = ctx.engine.getAlienPath()
                    + System.getProperty("file.separator")
                    + "uploadedaliens";
            Utilities.createFolder(domainFolder);

            //
            size = file.getSize();
            final Path path = Paths.get(
                    domainFolder
                    + System.getProperty("file.separator")
                    + file.getName()
            );
            final InputStream in = file.getInputStream();
            Files.copy(in, path);
            System.out.println("successfully uploaded alien jar " + file.getName());

            GameElementSpec element = new GameElementSpec(
                    "SPECIES",
                    domain + System.getProperty("file.separator") + file.getName(),
                    "",
                    "*",
                    null);
            ctx.engine.queueCommand(new GameCommand(GameCommandCode.AddElement, element));
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return size;
    }

    public static String doCheck(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "not attached";
        }

        String id = req.queryParams("id");
        boolean selected = req.queryParams("selected").equals("on");

        System.out.println("Species instantiation / kill request for id " + id);

        GameElementSpec element = new GameElementSpec("ALIEN", null, null, null, "#" + id);
        if (selected) {
            ctx.engine.queueCommand(new GameCommand(GameCommandCode.AddElement, element));
            System.out.println("Adding one of species id " + id);
        } else {
            ctx.engine.queueCommand(new GameCommand(GameCommandCode.KillElement, element));
            System.out.println("Killing all of species id " + id);
        }

        return "ok";
    }

    public Object[] status(Request req) {
        ArrayList<String> status = new ArrayList<>();

        ServerContext ctx = getCtx(req);
        if (ctx == null) {
            System.out.println("status: no context");
            status.add("no context");
            return status.toArray();
        }

        if (ctx.observer == null) {
            System.out.println("status: observer not initialized");
            status.add("status: observer not initialized");
            return status.toArray();
        }

        if (ctx.observer.isStale()) {
            System.out.println("status: observer stale");
            status.add("status: observer stale");
            return status.toArray();
        }

        status.add("status:");
        status.add(getHeapStats());

        status.add((ctx.engine.isAlive() ? "alive" : "dead"));

        return status.toArray();
    }

    private String getHeapStats() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        return formatNumber(heapFreeSize) + "/" + formatNumber(heapSize) + "/" + formatNumber(heapMaxSize);

    }

    private String formatNumber(long l) {
        if (l > 9999999999L) {
            return (l / 1000000000L) + "GB";
        } else if (l > 9999999L) {
            return (l / 1000000) + "MB";
        } else if (l > 9999L) {
            return (l / 1000) + "KB";
        } else {
            return l + "B";
        }
    }

    public Object[] allStatus(Request req) {
        ArrayList<String> status = new ArrayList<>();
        status.add("System status:");

        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        status.add(getHeapStats());

        for (Entry<String, GameEngineV2> entry : engines.entrySet()) {
            status.add("Engine: " + entry.getKey() + ", thread: " + (entry.getValue().isAlive() ? "alive" : "dead"));
        }
        return status.toArray();
    }

}
