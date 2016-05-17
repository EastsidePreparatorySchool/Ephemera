/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */

package gameengineV1;

import gameengineinterfaces.GameState;
import gamelogic.*;
import gameengineinterfaces.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;

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
    public ArrayList<GameElementSpec> readConfigFile(String fileName) {
        ArrayList<GameElementSpec> elements = new ArrayList<>(100);
        GameElementSpec element;
        //
        // Every line has kind, package, class, state in csv format
        //
        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(this.gamePath + fileName));

            String strLine;
            while ((strLine = in.readLine()) != null) {
                try {
                    //vis.debugOut("GameEngineV1: read config line: " + strLine);
                    strLine = strLine.trim();
                    
                    // ignore empty lines
                    if (strLine.length() == 0) {
                        continue;
                    }

                    // ignore comments
                    if (strLine.startsWith("!")) {
                        continue;
                    }

                    element = gson.fromJson(strLine, GameElementSpec.class);

                    // add to list
                    elements.add(element);
                    //v.debugOut("GameEngineV1:init:Parsed element " + element.packageName + ":"
                    //        + element.className + ", "
                    //        + element.state);

                } catch (Exception e) {
                    vis.debugOut("GameEngineV1:init:File parse error");
                    vis.debugOut("GameEngineV1:init:     " + e.getMessage());
                    return null;
                }

            }

            in.close();

        } catch (Exception e) {
            vis.debugOut("GameEngineV1:init:File-related error");
            vis.debugOut("GameEngineV1:init:     " + e.getMessage());
            return null;
        }
        return elements;
    }

  

    @Override
    public void processGameElements(ArrayList<GameElementSpec> elements) {
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
        }

    }

    @Override
    public void queueCommand(GameCommand gc
    ) {
        //
        // queue alien info to synchronized queue
        //

        vis.debugOut("GameEngineV1: Queueing command " + gc.code.toString());

        synchronized (this.queue) {
            this.queue.add(gc);
            this.queue.notify();
        }
    }

}
