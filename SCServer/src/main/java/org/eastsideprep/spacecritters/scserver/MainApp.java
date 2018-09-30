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
import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.spacecritters.SpaceCritters;
import org.eastsideprep.spacecritters.spacecritters.Utilities;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import spark.servlet.SparkApplication;

public class MainApp implements SparkApplication {

    static GameEngineV2 geMain;
    static MainApp app;
    static Thread mainThread;
    static boolean createServerEngine = true;

    // desktop initialization
    public static void main(String[] args) {
        
        
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
            staticFiles.location("/static");

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
        
        
        
        
        
        if (MainApp.createServerEngine) {
            MainApp.geMain = MainApp.createServerGameEngine("main");

            if (MainApp.geMain == null) {
                System.out.println("Could not create initial server game engine");
                return;
            }
        }

        
        
        
        WebSocketHandler wsh = new WebSocketHandler();
        webSocket("/ws/main/updates", wsh);
                
        
        /*
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
        get("/protected/serverlog", (req, res) -> getServerLogFile(req, res));
        get("/protected/slowmode", (req, res) -> doSlowMode(req));
        post("/protected/upload", (req, res) -> uploadFile(req, res));
        */
        wsh.beginUpdateLoop();
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

            try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(responce.raw().getOutputStream()));
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
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
        
        return name;
    }

    private static String doKillEngine(Request req) {
        System.out.println("Kill Engine request");
        if (!isAdmin(req)) {
            return "client not authorized to use this API";
        }

        String name = req.queryParams("name");
        
        return "Killed: " + name;
    }

    

    static private void createDesktopGameEngine() {
        try {
            System.out.println("SCServer: Creating desktop game engine");
            SpaceCritters.main(new String[]{});
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
        WebVisualizer streamer = new WebVisualizer();
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

    public static long uploadFile(Request request, Response response) {
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
            
            //TODO
            String domainFolder = "FOLDER LOCATION";//ctx.engine.getAlienPath() + System.getProperty("file.separator") + "uploadedaliens";
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
            
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return size;
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
    
    
    
    public static class EngineRecord {

        String name;
        boolean isAlive;
        int turns;
        int observers;
        int logSize;
    }

    public static class ObserverRecord {

        String name;
        int maxRead;

        ObserverRecord(String name, int maxRead) {
            this.name = name;
            this.maxRead = maxRead;
        }
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
    public class Stats {

        public String memStats;
        public int logSize;
        public boolean isAlive;
        public int sleepTime;
    }
    public class AllStats {

        public String freeHeapSpace;
        public String currentHeapSize;
        public String maxHeapSize;
        public int numEngines;
        public int totalObservers;
    }

}
