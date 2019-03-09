/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gameengineV1;

import gameengineinterfaces.GameState;
import gamelogic.*;
import gameengineinterfaces.*;
import java.io.FileReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author gmein
 */
public class GameEngineV1 implements GameEngine {

    public SpaceGrid grid;
    GameVisualizer vis;
    final Queue<GameCommand> queue;
    GameEngineThread gameThread;
    GameState gameState;
    public String gamePath;
    public String alienPath;
    Gson gson;

    public GameEngineV1() {
        this.gson = new Gson();
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean isAlive() {
        return gameThread.isAlive();
    }

    @Override
    public void init(GameVisualizer v, String gamePath, String alienPath) {
        // 
        // save the visualizer (how we report status)
        // create a new Spacegrid (the game board)
        // set up a transfer queue that we will use to post commands to the game thread
        //

        this.vis = v;
        gameState = GameState.Paused;
        this.gamePath = gamePath;
        this.alienPath = alienPath;

        //
        // start the thread, return to shell
        //
        vis.debugOut("GameEngine: Starting thread");
        this.gameThread = new GameEngineThread(this);
        this.gameThread.start();
    }

    @Override
    public GameElementSpec[] readConfigFile(String fileName) {
        return (GameElementSpec[]) readCustomConfigFile(fileName, GameElementSpec.class);
    }
    public Object readCustomConfigFile(String fileName, Class c) {
        //ArrayList<GameElementSpec> elements = new ArrayList<>(100);
        Object elements;
        //
        // Every line has kind, package, class, state in csv format
        //
        FileReader in;

        try {
            in = new FileReader(this.gamePath + fileName);
            char[] buffer = new char[65000];
            int n = in.read(buffer);
            String s = new String(buffer).trim();
            elements = gson.fromJson(s, c);
            in.close();
        }catch (JsonSyntaxException e) {
            vis.debugErr("GameEngineV1:init:File parse error");
            vis.debugErr("GameEngineV1:init:     " + e.getMessage() + e.toString());
            return null;
        } catch (Exception e) {
            vis.debugErr("GameEngineV1:init:File parse error");
            vis.debugErr("GameEngineV1:init:     " + e.getMessage() + e.toString());
            return null;
        }
        return elements;
    }
    
    /*public Achievement[] readAchievements(String filename) {*/

    @Override
    public void processGameElements(GameElementSpec[] elements) {
        System.out.println("Processing game elements!");
        //
        // queue every game element
        //
        for (GameElementSpec element : elements) {
            GameCommand gc = new GameCommand(GameCommandCode.AddElement);
            GameElementSpec[] elementContainer = new GameElementSpec[1];
            elementContainer[0] = element;
            gc.parameters = elementContainer;

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

    public void processAchievements(Achievement[] achievements) {
        
    }
    @Override
    public void queueCommand(GameCommand gc) {
        //
        // queue alien info to synchronized queue
        //

        vis.debugOut("GameEngineV1: Queueing command " + gc.code.toString());

        synchronized (this.queue) {
            this.queue.add(gc);
            this.queue.notifyAll();
        }
    }
}
