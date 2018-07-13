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

    public GameEngineV2(String name) {
        this.gson = new Gson();
        this.queue = new ConcurrentLinkedQueue<>();
        this.state = new SCGameState(0, 0);
        this.log = new GameLog(state);
        this.name = name;
    }

    @Override
    public boolean isAlive() {
        return gameThread.isAlive();
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
        FileReader in;

        try {
            in = new FileReader(this.gamePath + fileName);
            char[] buffer = new char[65000];
            int n = in.read(buffer);
            String s = new String(buffer).trim();
            elements = gson.fromJson(s, GameElementSpec[].class);
            in.close();
        } catch (JsonSyntaxException e) {
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

    @Override
    public void processGameElements(GameElementSpec[] elements) {
        //
        // queue every game element
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

}
