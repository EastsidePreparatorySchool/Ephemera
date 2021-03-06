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
    public GameEngineThread gameThread;
    GameState gameState;
    public String gamePath;
    public String alienPath;
    Gson gson;
    public SCGameState state;
    public GameLog log;
    public String name;
    private boolean dead = false;
    public long timeOfDeath;
    public boolean slow = false;
    public static String projectPath = null;

    static private URLClassLoader classLoader;
    static private Method addURL;

    public GameEngineV2(String name, String projectPath) {
        this.gson = new Gson();
        this.queue = new ConcurrentLinkedQueue<>();
        this.state = new SCGameState(0, 0);
        this.log = new GameLog(state);
        this.name = name;
        GameEngineV2.projectPath = projectPath;
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
    public GameElementSpec[] readConfigFile(String path, String fileName) {
        //ArrayList<GameElementSpec> elements = new ArrayList<>(100);
        GameElementSpec[] elements;
        GameElementSpec element;

        //
        // Every line has kind, package, class, state in csv format
        //
        String config = null;

        try {
            FileReader in = new FileReader(path + fileName);
            char[] buffer = new char[65000];
            in.read(buffer);
            config = new String(buffer).trim();
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("readConfigFile: could not read main config file " + path+ fileName + ", trying resource ...");

            try {
                byte[] buffer = new byte[65000];
                InputStream is = GameEngineV2.class.getResourceAsStream("/resources/sc_conf/" + fileName);
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
//            GameEngineV2.classLoader = (URLClassLoader) GameEngineV2.class.getClassLoader();
//            GameEngineV2.addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
//            GameEngineV2.addURL .setAccessible(true);
//            System.out.println("EngineV2: Class loader hacked successfully");
        } catch (Exception e) {
//            System.out.println("EngineV2: could not get hacked class loader");
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {
        System.out.println("GE: shutdown " + name);
        dead = true;
        this.timeOfDeath = System.currentTimeMillis();
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
    public Constructor<?> loadConstructor(String domainName, String packageName, String className, boolean msgs) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> cs = null;
        String fullName = "";
        File file = null;
        //System.out.println("EngineV2:loadConstructor: " + domainName + ":" + packageName + ":" + className);

        if (domainName.equalsIgnoreCase("org.eastsideprep.spacecritters")) {
            if (GameEngineV2.projectPath != null) {
                domainName = GameEngineV2.projectPath
                        + "stockelements"
                        + System.getProperty("file.separator")
                        + "dist"
                        + System.getProperty("file.separator")
                        + "stockelements.jar";
            } else {
                domainName = "lib" + System.getProperty("file.separator") + "stockelements.jar";
            }
        }

        fullName = domainName;
        if (fullName.toLowerCase().endsWith(".class")) {
            fullName = Paths.get(fullName).getParent().toString();
        }

        //System.out.println("EngineV2:loadConstructor: full name " + fullName);
        String fullClassName = null;
        try {
            file = new File(fullName);
            fullClassName = packageName.equals("") ? className : (packageName + "." + className);
//            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            //GameEngineV2.addURL.invoke(GameEngineV2.classLoader, (Object[]) new URL[]{file.toURI().toURL()});
            // Creating an instance of URLClassloader using the above URL and parent classloader 
            URL url = file.toURI().toURL();
            //System.out.println("Trying to obtain class loader for URL " + url);
            //System.out.println("Read privileges: " + file.canRead());
            GameEngineV2.classLoader = URLClassLoader.newInstance(new URL[]{url}, GameEngineV2.class.getClassLoader());
//            Class<?> alienClass = Class.forName(className, true, GameEngineV2.classLoader);
            Class<?> alienClass = GameEngineV2.classLoader.loadClass(className);
            cs = alienClass.getConstructor();
        } catch (Exception e) {
            if (msgs) {
                System.out.println("EngineV2: Could not get constructor: " + e.getMessage());
                System.out.println("EngineV2: Looked in file: " + file.getPath());
                System.out.println(e.toString());
            }
            throw e;
        }
        return cs;
    }

}
