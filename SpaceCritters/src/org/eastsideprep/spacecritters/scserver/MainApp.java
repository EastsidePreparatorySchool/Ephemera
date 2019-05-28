package org.eastsideprep.spacecritters.scserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
import org.eastsideprep.spacecritters.gameengineimplementation.Utilities;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import spark.servlet.SparkApplication;

public class MainApp implements SparkApplication {

    static GameEngineV2 geMain;
    static Governor engines;
    static MainApp app;
    static Thread mainThread;
    static boolean createServerEngine = true;

    // desktop initialization
    public static void main(String[] args) {

        //run code in the testing class
        //Testing.run();

        System.out.println("SC main() (scserver) " + getDateString());
        String userDir = System.getProperty("user.dir");

        //System.setProperty("javafx.preloader", "org.eastsideprep.spacecritters.spacecritters.SplashScreenLoader");
        // invoked from main, start Spark Jetty server
        //staticFiles.location("/static");
        if (args.length == 0 || !args[0].equalsIgnoreCase("desktoponly")) {
            MainApp.app = new MainApp();

            if (userDir.toLowerCase().contains("ephemera")) {
                userDir += System.getProperty("file.separator") + "target";
            }
            userDir += System.getProperty("file.separator") + "static";

            String port = System.getenv("PORT");
            if (port != null) {
                System.out.println("SC: environment prescribes using port " + port);
                port(Integer.parseInt(port));
            } else {
                System.out.println("SC: listening on port 8080");
                port(8080);
            }
//            staticFiles.externalLocation(userDir);
            staticFiles.location("/resources/static");

            if (!(args.length == 0 || args[0].equalsIgnoreCase("serveronly"))) {
                MainApp.createServerEngine = false; // will cause init() to not create server engine, default running mode
            }
            MainApp.app.init();
            mainThread = Thread.currentThread();
        } else {
            System.out.println("SC running in desktop-only mode");
        }

        if ((args.length > 0 && args[0].equalsIgnoreCase("serveronly"))
                || args.length == 0) {
            System.out.println("SC running in server-only mode");

            try {
                // loop until interrupted
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                // catch and be quiet, then exit
            }
        } else if (args.length > 0
                && (args[0].equalsIgnoreCase("desktoponly")
                || args[0].equalsIgnoreCase("both"))) {
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
        String os = System.getProperty("os.name");
        String userDir = System.getProperty("user.dir");
        String homeDir = System.getProperty("user.home");
        String javaHomeDir = System.getProperty("java.home");
        String cp = System.getProperty("java.class.path");
        System.out.println(" os.name " + os);
        System.out.println(" user.dir " + userDir);
        System.out.println(" user.home " + homeDir);
        System.out.println(" jave.home " + javaHomeDir);
        System.out.println(" java.class.path ");
        for (String s : cp.split(";")) {
            System.out.println("   " + s);
        }

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
//        get("/protected/allstatus", "application/json", (req, res) -> allStatus(req), new JSONRT());
        get("/protected/allstatus2", "application/json", (req, res) -> allStatus2(req), new JSONRT());
        get("/protected/queryadmin", "application/json", (req, res) -> queryAdmin(req));
        get("/protected/serverlog", (req, res) -> getServerLogFile(req, res));
        get("/protected/slowmode", (req, res) -> doSlowMode(req));
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
//            return null;
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

    private static String doSlowMode(Request req) {
        ServerContext ctx = getCtx(req);
        if (ctx.observer == null) {
            return "SC: slowmode: not attached";
        }
        String state = req.queryParams("state");
        if (state != null && state.equalsIgnoreCase("on")) {
            ctx.engine.queueCommand(new GameCommand(GameCommandCode.SlowModeOn));
        } else {
            ctx.engine.queueCommand(new GameCommand(GameCommandCode.SlowModeOff));
        }

        return "SC: slowmode " + state + " requested";
    }

    private static String queryAdmin(Request req) {
        return (isAdmin(req) ? "yes" : "no");
    }

    private static boolean isAdmin(Request req) {
        String name = req.headers("X-MS-CLIENT-PRINCIPAL-NAME");
        return (name == null || name.equalsIgnoreCase("gmein@eastsideprep.org"));
    }

    private static Object getServerLogFile(Request request, Response responce) {
        System.out.println("GetServerLog");
        File file = getJettyLogFile();
        if (file == null) {
            halt(505, "file not found");
        }
        responce.raw().setContentType("application/octet-stream");
        responce.raw().setHeader("Content-Disposition", "attachment; filename=" + file.getName() + ".zip");
        try {

            try ( ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(responce.raw().getOutputStream()));  BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                ZipEntry zipEntry = new ZipEntry(file.getName());

                zipOutputStream.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bufferedInputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                zipOutputStream.flush();
                zipOutputStream.close();
            }
        } catch (Exception e) {
            halt(505, "server error");
        }

        return null;
    }

    public static class EngineRecord {

        String name;
        boolean isAlive;
        int turns;
        int observers;
        int logSize;
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
                er.logSize = e.getValue().log.getLogSize();

                result[i++] = er;
                //System.out.println("  ER: "+er.name+", "+(er.isAlive?"alive":"dead"));
            }
            Arrays.sort(result, (a, b) -> b.name.compareTo(a.name));
        }

        return result;
    }

    public static class ObserverRecord {

        String name;
        int maxRead;

        ObserverRecord(String name, int maxRead) {
            this.name = name;
            this.maxRead = maxRead;
        }
    }

    private static ObserverRecord[] doListObservers(Request req) {
        if (!isAdmin(req)) {
            System.out.println("listObservers: client not authorized");
            return null;
        }

        LinkedList<GameEngineV2> theseEngines = new LinkedList<>();

        String name = req.queryParams("name");
        if (name != null && !name.equals("")) {
            synchronized (engines) {
                for (Entry<String, GameEngineV2> e : engines.entrySet()) {
                    if (name.equals("*") || Pattern.matches(name, e.getKey())) {
                        theseEngines.add(e.getValue());
                    }
                }
            }
        } else {
            ServerContext ctx = getCtx(req);
            if (ctx == null || ctx.observer == null) {
                synchronized (engines) {
                    theseEngines = new LinkedList<>(engines.values());
                }
            } else {
                theseEngines.add(ctx.engine);
            }
        }

        LinkedList<ObserverRecord> result = new LinkedList<>();
        for (GameEngineV2 e : theseEngines) {
            LinkedList<GameLogObserver> observers = e.log.getObservers();
            observers.forEach((o) -> result.add(new ObserverRecord(e.name + ":" + o.client, o.getMaxRead())));
        }

        ObserverRecord[] data = new ObserverRecord[result.size()];
        return result.toArray(data);
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

        return "Killed: " + name;

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
            System.out.println("Creating a desktop game engine is disabled due to the JavaFX client being too far behind - GM 02/04/2019");
//            System.out.println("SCServer: Creating desktop game engine");
//            SpaceCritters.main(new String[]{});
        } catch (Exception e) {
            System.err.println("scserver desktop engine: " + e.getMessage());
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
            Constants.searchProjectForAliens = false;
        } else {
            // probably started from other folder
            gamePath = System.getProperty("user.dir");
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
        if (!isAdmin(request)) {
            System.out.println("uploadFile: client not authorized");
            return -1;
        }

        System.out.println("upload ...");
        MultipartConfigElement multipartConfigElement
                = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
        request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

        long size = 0;
        try {
            Part file = request.raw().getPart("alienfile");
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
                    + file.getSubmittedFileName()
            );
            final InputStream in = file.getInputStream();
            Files.copy(in, path);
            System.out.println("successfully uploaded alien file " + file.getSubmittedFileName());

            GameElementSpec element = new GameElementSpec(
                    "SPECIES",
                    domain + System.getProperty("file.separator") + file.getSubmittedFileName(),
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

    public class Stats {

        public String memStats;
        public int logSize;
        public boolean isAlive;
        public int sleepTime;
    }

    public Stats status(Request req) {
        ArrayList<String> status = new ArrayList<>();

        ServerContext ctx = getCtx(req);
        if (ctx == null) {
            System.out.println("status: no context");
            return null;
        }

        if (ctx.observer == null) {
            System.out.println("status: observer not initialized for" + ctx.client);
            return null;
        }

        if (ctx.observer.isStale()) {
            System.out.println("status: observer stale for " + ctx.client);
            return null;
        }

        Stats result = new Stats();
        result.isAlive = ctx.engine.isAlive();
        result.memStats = getHeapStats();
        result.logSize = ctx.engine.log.getLogSize();
        if (ctx.engine.gameThread != null) {
            result.sleepTime = ctx.engine.gameThread.sleepTime;
        } else {
            result.sleepTime = 0;
        }

        return result;
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

        public String freeHeapSpace;
        public String currentHeapSize;
        public String maxHeapSize;
        public int numEngines;
        public int totalObservers;
    }

    public AllStats allStatus2(Request req) {

        AllStats as = new AllStats();

        // Get current size of heap in bytes
        as.currentHeapSize = formatNumber(Runtime.getRuntime().totalMemory());
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        as.maxHeapSize = formatNumber(Runtime.getRuntime().maxMemory());
        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        as.freeHeapSpace = formatNumber(Runtime.getRuntime().freeMemory());

        synchronized (engines) {
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

    private static String getDateString() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return dateFormat.format(date);
    }

    private static File getJettyLogFile() {
        Path logFolder = Paths.get(System.getProperty("user.dir")).getRoot();
        logFolder = Paths.get(logFolder.toString(), "home\\LogFiles");
        if (!logFolder.toFile().exists()) {
            // not in standard azure log setup, exit
            System.out.println("getJettyLogFile: not in Azure");
            return null;
        }
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");

        String name = "jetty_" + dateFormat.format(date) + ".stderrout.log";
        Path logFile = Paths.get(logFolder.toString(), name);
        System.out.println("GetJettyLog:" + logFile);
        File f = logFile.toFile();
        if (f.exists()) {
            return f;
        }

        // try GMT
        // convert date to localdatetime
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // plus one
        ZonedDateTime gmt = localDateTime.atZone(ZoneId.of("GMT"));

        name = "jetty_" + dateFormat.format(gmt) + ".stderrout.log";
        logFile = Paths.get(logFolder.toString(), name);
        System.out.println("GetJettyLog:" + logFile);
        f = logFile.toFile();
        if (f.exists()) {
            return f;
        }
        return null;

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
                // wait for first engine
                while (MainApp.geMain == null) {
                    Thread.sleep(1000);
                }

                // now loop and react if they all died
                while (true) {
                    int alive = 0;
                    synchronized (this) {
                        for (GameEngineV2 e : this.values()) {
                            if (e.isAlive()) {
                                e.log.removeStaleObservers();
                                alive++;
                            } else {
                                if (e.timeOfDeath < (System.currentTimeMillis() - (1000L * 60L * 60L * 24L))) {
                                    // engine has been dead for 24 hours, remove from Governor map
                                    this.remove(e.name);
                                    System.gc();

                                    System.out.println("Removed engine " + e.name + " time of death " + e.timeOfDeath);
                                    System.out.println(" Current time " + System.currentTimeMillis());
                                }
                            }
                        }
                    }
                    if (alive == 0) {
                        // no live engines left, make a new one
                        System.gc();
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
