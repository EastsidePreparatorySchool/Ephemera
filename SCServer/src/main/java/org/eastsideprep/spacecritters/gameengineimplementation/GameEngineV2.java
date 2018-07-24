/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineimplementation;

import org.eastsideprep.spacecritters.gameengineinterfaces.GameState;
import org.eastsideprep.spacecritters.gamelogic.*;
import org.eastsideprep.spacecritters.gameengineinterfaces.*;
import java.io.FileReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.CodeSource;
import org.eastsideprep.spacecritters.gamelog.GameLog;
import org.eastsideprep.spacecritters.scgamelog.SCGameState;

/**
 *
 * @author gmein
 */
public class GameEngineV2 implements GameEngine {

    public SpaceGrid grid;
    GameVisualizer vis;
    final Queue<GameCommand> queue;
    GameEngineThread gameThread;
    GameState gameState;
    public String gamePath;
    public String alienPath;
    Gson gson;
    public SCGameState state;
    public GameLog log;
    public String name;
    private boolean dead = false;
    public long timeOfDeath;

    static private URLClassLoader classLoader;
    static private Method addURL;

    public GameEngineV2(String name) {
        this.gson = new Gson();
        this.queue = new ConcurrentLinkedQueue<>();
        this.state = new SCGameState(0, 0);
        this.log = new GameLog(state);
        this.name = name;
    }

    @Override
    public boolean isAlive() {
        if (dead) {
            return false;
        }
        if (gameThread.isAlive()) {
            return true;
        }

        timeOfDeath = System.currentTimeMillis();
        shutdown();
        return false;
    }

    @Override
    public void init(GameVisualizer vis, String gamePath, String alienPath) {

        // 
        // save the visualizer (how we report status)
        // create a new Spacegrid (the game board)
        // set up a transfer queue that we will use to post commands to the game thread
        //
        this.vis = vis;
        gameState = GameState.Paused;
        this.gamePath = gamePath;
        this.alienPath = alienPath;

        // hack the class loader so we can load alien jars
        this.getHackedClassLoader();

        //
        // start the thread, return to shell
        //
        vis.debugOut("GameEngine: Starting thread");
        this.gameThread = new GameEngineThread(this);
        this.gameThread.start();
    }

    @Override
    public GameElementSpec[] readConfigFile(String fileName) {
        //ArrayList<GameElementSpec> elements = new ArrayList<>(100);
        GameElementSpec[] elements;
        GameElementSpec element;

        //
        // Every line has kind, package, class, state in csv format
        //
        String config = null;

        try {
            FileReader in = new FileReader(this.gamePath + fileName);
            char[] buffer = new char[65000];
            in.read(buffer);
            config = new String(buffer).trim();
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("readConfigFile: could not read main config file " + fileName + ", trying resource ...");

            try {
                byte[] buffer = new byte[65000];
                InputStream is = GameEngineV2.class.getResourceAsStream("/sc_conf/" + fileName);
                is.read(buffer);
                config = new String(buffer).trim();
            } catch (Exception e2) {
                System.out.println("readConfigFile: could not read main config resource \\sc_conf\\ " + fileName);
            }
        } catch (IOException e3) {
            System.out.println("readConfigFile: general IO exception trying to read " + fileName);
        }

        try {
            //System.out.println("readConfigFile: parsing JSON string: "+config.substring(0, 100) + "...");
            elements = gson.fromJson(config, GameElementSpec[].class);
        } catch (JsonSyntaxException e) {
            vis.debugErr("GameEngineV1:readConfigFile:File parse error");
            vis.debugErr("GameEngineV1:readConfigFile:     " + e.getMessage() + e.toString());
            return null;
        } catch (Exception e4) {
            vis.debugErr("GameEngineV1:readConfigFile:File parse error");
            vis.debugErr("GameEngineV1:readConfigFile:     " + e4.getMessage() + e4.toString());
            return null;
        }
        return elements;
    }

    @Override
    public void processGameElements(GameElementSpec[] elements) {
        //
        // queue every game element

        System.out.println("EngineV2: Queueing " + elements.length + " elements");
        for (GameElementSpec element : elements) {
            //if (element.className.equals("Caelusite")) System.out.println("HELLLO!: " + element.parent);
            GameCommand gc = new GameCommand(GameCommandCode.AddElement);
            gc.parameters = new GameElementSpec[]{element};

            //vis.debugOut("GameEngine: Queueing new game element");
            queueCommand(gc);
            // this feels like cheating - 
            // I am stripping the gameMode out here, 
            // because I need it to read the right file, 
            // and I cannot wait for the engine to process it.
            if (element.kind == GameElementKind.CONSTANT
                    && element.className.equalsIgnoreCase("gameMode")) {
                Constants.gameMode = element.state;
            }
        }

    }

    @Override
    public void queueCommand(GameCommand gc) {
        if (dead) {
            return;
        }

        //
        // queue alien info to synchronized queue
        //

        /*if (gc.parameters != null && gc.parameters.length > 0 && gc.parameters[0] != null && gc.parameters[0] instanceof GameElementSpec) {
            GameElementSpec m = (GameElementSpec) gc.parameters[0];
            //System.out.println("found a guy");
            if (m.className.equals("Caelusite")) {
                System.out.println("\n\n\n\nHERES A THING BUT LATER: " + m.parent + "\n" + gc.code + "\n\n");
            }
        
        }*/
        vis.debugOut("GameEngineV1: Queueing command " + gc.code.toString());

        synchronized (this.queue) {
            this.queue.add(gc);
            this.queue.notifyAll();
        }
    }

    @Override
    public String getAlienPath() {
        return this.alienPath;
    }

    public void getHackedClassLoader() {
        try {
//            System.out.println("EngineV2: Attempting to hack class loader ...");
            this.classLoader = (URLClassLoader) GameEngineV2.class.getClassLoader();
            GameEngineV2.addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            GameEngineV2.addURL .setAccessible(true);
            System.out.println("EngineV2: Class loader hacked successfully");
        } catch (Exception e) {
//            System.out.println("EngineV2: could not get hacked class loader");
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {
        System.out.println("GE: shutdown " + name);
        dead = true;
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
            try {
                gameThread.join(5000);
            } catch (InterruptedException e) {
            }
        }
        gameThread = null;
        grid = null;
//        vis = null; // leave this alive to allow the dying thread to post messages
        System.gc();
        log.onDeath();
    }

    //
    // Dynamic class loader (.jar files)
    // stolen from StackOverflow, considered dark voodoo magic
    //
    public Constructor<?> loadConstructor(String domainName, String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> cs = null;
        String fullName = "";
        File file;
        //System.out.println("EngineV2:loadConstructor: " + domainName + ":" + packageName + ":" + className);

        if (packageName.endsWith("stockelements")
                || packageName.equalsIgnoreCase("alieninterfaces")) {
//            fullName = engine.gamePath
////                    + packageName
////                    + System.getProperty("file.separator")
//                    + "scserver"
//                    + System.getProperty("file.separator")
//                    + "target"
//                    + System.getProperty("file.separator")
//                    + "SCServer-1.0-SNAPSHOT.jar";
//            packageName = "org.eastsideprep.spacecritters.stockelements";
            CodeSource src = SpaceGrid.class.getProtectionDomain().getCodeSource();
            //System.out.println("EngineV2:loadConstructor: source name " + src.getLocation().getFile());

            fullName = src.getLocation().getFile();
        } else {
            fullName = this.alienPath + domainName;
            if (fullName.toLowerCase().endsWith(".class")) {
                fullName = Paths.get(fullName).getParent().toString();
            }
        }
        //System.out.println("EngineV2:loadConstructor: full name " + fullName);
        String fullClassName = null;
        try {
            file = new File(fullName);
            fullClassName = packageName.equals("") ? className : (packageName + "." + className);
//            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            GameEngineV2.addURL.invoke(GameEngineV2.classLoader, (Object[])new URL[]{file.toURI().toURL()});
            cs = classLoader.loadClass(fullClassName).getConstructor();
        } catch (Exception e) {
            if (!packageName.endsWith("stockelements")) {
                System.out.println("EngineV2: Could not get constructor: " + e.getMessage());
            }
            throw e;
        }
        return cs;
    }

}
