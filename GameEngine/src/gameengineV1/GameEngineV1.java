/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineV1;

import gameengineinterfaces.GameState;
import gamelogic.*;
import gameengineinterfaces.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;
import java.io.FileWriter;

/**
 *
 * @author gmein
 */
public class GameEngineV1 implements GameEngine {

    public SpaceGrid grid;
    GameVisualizer vis;
    Queue<GameCommand> queue;
    GameEngineThread gameThread;
    GameState gameState;
    public String gamePath;
    public String alienPath;

    @Override
    public boolean isAlive() {
        return gameThread.isAlive();
    }

    @Override
    public void initFromFile(GameVisualizer v, String gameJarPath, String alienPath, String savedGameFile) {
        GameElementSpec[] savedGame;
        GameElementSpec element = null;

        Gson gson = new Gson();

        this.vis = v;
        this.gamePath = gameJarPath;
        this.alienPath = alienPath;

        //
        // read initial config from file
        //
        v.debugOut("GameEngineV1: read config file: " + savedGameFile);

        ArrayList<GameElementSpec> elements = new ArrayList<GameElementSpec>(50);
        //
        // Every line has kind, package, class, state in csv format
        //
        BufferedReader in;
        
        try {
            in = new BufferedReader(new FileReader(this.gamePath + savedGameFile));
           
            String strLine;
            while ((strLine = in.readLine()) != null) {
                try {
                    //vis.debugOut("GameEngineV1: read config line: " + strLine);
                 
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
                    v.debugOut("GameEngineV1:init:File parse error");
                    v.debugOut("GameEngineV1:init:     " + e.getMessage());
                }
               
            }

            in.close();

        } catch (Exception e) {
            v.debugOut("GameEngineV1:init:File-related error");
            v.debugOut("GameEngineV1:init:     " + e.getMessage());
            return;
        }

        // convert list into array to process as if it had been saved
        savedGame = new GameElementSpec[1]; // this array will be grown automatically by "toArray" below
        savedGame = elements.toArray(savedGame);

        this.init(v, gameJarPath, savedGame);
    }

    @Override
    public void init(GameVisualizer v, String gameJarPath, GameElementSpec[] savedGame) {
        // 
        // save the visualizer (how we report status)
        // create a new Spacegrid (the game board)
        // set up a transfer queue that we will use to post commands to the game thread
        //

        this.vis = v;
        this.queue = new ConcurrentLinkedQueue<GameCommand>();
        gameState = GameState.Paused;
        this.gamePath = gameJarPath;

        vis.debugOut("GameEngine: Creating thread");
        this.gameThread = new GameEngineThread(this);

        //
        // queue every game element
        //
        vis.debugOut("GameEngineV1: Elements in config: " + Integer.toString(savedGame.length));
        for (GameElementSpec element : savedGame) {
            GameCommand gc = new GameCommand(GameCommandCode.AddElement);
            GameElementSpec[] elements = new GameElementSpec[1];
            elements[0] = element;
            gc.parameters = elements;

            //vis.debugOut("GameEngine: Queueing new game element");
            queueCommand(gc);
        }

        //
        // start the thread, return to shell
        //
        vis.debugOut("GameEngine: Starting thread");
        this.gameThread.start();
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

    @Override
    public GameElementSpec[] saveGame() {
        return new GameElementSpec[1];
    }
}
