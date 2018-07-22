package org.eastsideprep.spacecritters.scserver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Pattern;
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
    static Governor engines;
    static MainApp app;
    static Thread mainThread;
    static boolean createServerEngine = true;

    private static String getDateString() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(date);
    }

    // desktop initialization
    public static void main(String[] args) {
        System.out.println("SC main() (scserver) " + getDateString());

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
            port(8080);
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
        get("/protected/kill", "application/json", (req, res) -> doKillEngine(req), new JSONRT());
        get("/protected/attach", "application/json", (req, res) -> doAttach(req), new JSONRT());
        get("/protected/detach", "application/json", (req, res) -> doDetach(req), new JSONRT());
        get("/protected/updates", "application/json", (req, res) -> doUpdates(req), new JSONRT());
        get("/protected/check", "application/json", (req, res) -> doCheck(req), new JSONRT());
        get("/protected/listobservers", "application/json", (req, res) -> doListObservers(req), new JSONRT());
        get("/protected/status", "application/json", (req, res) -> status(req), new JSONRT());
        get("/protected/allstatus", "application/json", (req, res) -> allStatus(req), new JSONRT());
        get("/protected/allstatus2", "application/json", (req, res) -> allStatus2(req), new JSONRT());
        get("/protected/queryadmin", "application/json", (req, res) -> queryAdmin(req));
        post("/protected/upload", (req, res) -> uploadFile(req, res));

        MainApp.engines = new Governor();
        MainApp.engines.init();

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

    private static String queryAdmin(Request req) {
        return (isAdmin(req) ? "yes" : "no");
    }

    private static boolean isAdmin(Request req) {
        String name = req.headers("X-MS-CLIENT-PRINCIPAL-NAME");
        return (name == null || name.equalsIgnoreCase("gmein@eastsideprep.org"));
    }

    public static class EngineRecord {

        String name;
        boolean isAlive;
        int turns;
        int observers;

    }

    private static EngineRecord[] doListEngines(Request req) {
        //System.out.println("ListEngines:");
        EngineRecord[] result;
        synchronized (engines) {
            result = new EngineRecord[engines.size()];
            int i = 0;
            for (Entry<String, GameEngineV2> e : engines.entrySet()) {
                EngineRecord er = new EngineRecord();
                er.isAlive = e.getValue().isAlive();
                er.name = e.getKey();
                er.observers = e.getValue().log.getObservers().size();
                er.turns = e.getValue().log.turnsCompleted;
                result[i++] = er;
                //System.out.println("  ER: "+er.name+", "+(er.isAlive?"alive":"dead"));
            }
            Arrays.sort(result, (a, b) -> b.name.compareTo(a.name));
        }

        return result;
    }

    private static Object[] doListObservers(Request req) {
        LinkedList<GameEngineV2> theseEngines = new LinkedList<>();

        String name = req.queryParams("name");
        if (name != null && !name.equals("")) {
            synchronized (engines) {
                for (Entry<String, GameEngineV2> e : engines.entrySet()) {
                    if (Pattern.matches(name, e.getKey())) {
                        theseEngines.add(e.getValue());
                    }
                }
            }

        } else {
            ServerContext ctx = getCtx(req);
            if (ctx == null || ctx.observer == null) {
                theseEngines = new LinkedList<>(engines.values());
            } else {

                theseEngines.add(ctx.engine);
            }
        }

        LinkedList<String> result = new LinkedList<>();
        for (GameEngineV2 e : theseEngines) {
            LinkedList<GameLogObserver> observers = e.log.getObservers();
            observers.forEach((o) -> result.add(e.name + ":" + o.client));
        }

        return result.toArray();
    }

    private static String doCreateEngine(Request req) {
        System.out.println("Create Engine request");
        if (!isAdmin(req)) {
            return "client not authorized to use this API";
        }

        String name = req.queryParams("name");

        // cannot create main engine after the fact todo: rethink that.
        if (name.equalsIgnoreCase("main")) {
            System.out.println("Exception in doCreateEngine: can't create main engine from web interface");
            return null;
        }

        GameEngineV2 eng = MainApp.engines.get(name);
        if (eng != null) {
            System.out.println("Exception in doCreateEngine: engine '" + name + "' already exists");
            return null;
        }

        try {
            eng = createServerGameEngine(name);
        } catch (Exception e) {
            System.out.println("Exception in doCreateEngine: " + e.getMessage());
        }
        synchronized (engines) {
            engines.put(name, eng);
        }
        return name;

    }

    private static String doKillEngine(Request req) {
        System.out.println("Kill Engine request");
        if (!isAdmin(req)) {
            return "client not authorized to use this API";
        }

        String name = req.queryParams("name");

        GameEngineV2 eng = MainApp.engines.get(name);
        if (eng == null) {
            System.out.println("Exception in doKillEngine: engine '" + name + "' does not exist");
            return "Exception in doKillEngine: engine '" + name + "' does not exist";
        }

        try {
            eng.shutdown();
        } catch (Exception e) {
            System.out.println("Exception in doKillEngine: " + e.getMessage());
            return "Exception in doKillEngine: " + e.getMessage();
        }
        
        return "Killed: "+name;

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

    public class AllStats {

        public long freeHeapSpace;
        public long currentHeapSize;
        public long maxHeapSize;
        public int numEngines;
        public int totalObservers;
    }

    public AllStats allStatus2(Request req) {

        AllStats as = new AllStats();

        // Get current size of heap in bytes
        as.currentHeapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        as.maxHeapSize = Runtime.getRuntime().maxMemory();
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        as.freeHeapSpace = Runtime.getRuntime().freeMemory();
        
        synchronized(engines) {
            as.numEngines = engines.size();
            as.totalObservers = doListObservers(req).length;
        }
       
        return as;
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
    
    
      private class Governor extends HashMap<String, GameEngineV2> {

        Governor() {
        }

        private void init() {
            new Thread(() -> {
                watchDogThread();
            }).start();

        }

        private void watchDogThread() {
            try {
                Thread.sleep(300000);
                while (true) {
                    int alive = 0;
                    synchronized (this) {
                        for (GameEngineV2 e : this.values()) {
                            if (e.isAlive()) {
                                alive++;
                            } else if (e.timeOfDeath < (System.currentTimeMillis()-(1000*60*60*24))) {
                                // engine has been dead for 24 hours, remove from Governor map
                                this.remove(e.name);
                            }
                        }
                    }
                    if (alive == 0) {
                        // no live engines left, make a new one
                        System.out.println("Governor: There were none alive, so I am creating another one.");
                        String name = getDateString();
                        GameEngineV2 eng = MainApp.createServerGameEngine(name);
                        synchronized (engines) {
                            engines.put(name, eng);
                        }
                        Thread.sleep(5000);
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {

            }

        }

    }


}
