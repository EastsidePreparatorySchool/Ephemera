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
    String gameJarPath;
    String alienPath;

    @Override
    public boolean isAlive() {
        return gameThread.isAlive();
    }

    @Override
    public void initFromFile(GameVisualizer v, String gameJarPath, String alienPath, String savedGameFile) {
        GameElementSpec[] savedGame;

        this.vis = v;
        this.gameJarPath = gameJarPath;
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
            in = new BufferedReader(new FileReader(this.gameJarPath + savedGameFile));

            String strLine;
            while ((strLine = in.readLine()) != null) {
                try {
                    //vis.debugOut("GameEngineV1: read config line: " + strLine);
                    String[] strElement = strLine.split(",");

                    // ignore empty lines
                    if (strElement.length == 0) {
                        continue;
                    }
                    // need at least the code
                    if (strElement[0] == null) {
                        throw new ParseException(strLine, 0);
                    }

                    // ignore comments
                    if (strElement[0].startsWith("!")) {
                        continue;
                    }

                    //if (strElement[0].equalsIgnoreCase("COMMAND")) // make new GameElement from code and next few fields
                    GameElementSpec element = new GameElementSpec(strElement[0]);

                    // get domain name
                    if (strElement.length > 1 && strElement[1] != null) {
                        element.domainName = strElement[1].trim();
                    } else {
                        element.domainName = "";
                    }

                    // get package name
                    if (strElement.length > 2 && strElement[2] != null) {
                        element.packageName = strElement[2].trim();
                    } else {
                        element.packageName = "";
                    }

                    // get class name
                    if (strElement.length > 3 && strElement[3] != null) {
                        element.className = strElement[3].trim();
                    } else {
                        element.className = "";
                    }

                    // get parent
                    if (strElement.length > 4 && strElement[4] != null) {
                        element.parent = strElement[4].trim();
                    } else {
                        element.parent = "";
                    }

                    // get pos x
                    if (strElement.length > 5 && strElement[5] != null
                            && !strElement[5].trim().isEmpty()) {
                        element.x = Integer.parseInt(strElement[5].trim());
                        
                    } else {
                        element.x = 0;
                    }

                    // get pos Y
                    if (strElement.length > 6 && strElement[6] != null && !strElement[6].trim().isEmpty()) {
                        element.y = Integer.parseInt(strElement[6].trim());
                    } else {
                        element.y = 0;
                    }

                    // get energy TODO: Deal with real doubles here if we want them
                    if (strElement.length > 7 && strElement[7] != null && !strElement[7].trim().isEmpty()) {
                        element.energy = (int) Double.parseDouble(strElement[7].trim());
                    } else {
                        element.energy = 0;
                    }

                    // get tech
                    if (strElement.length > 8 && strElement[8] != null && !strElement[8].trim().isEmpty()) {
                        element.tech = Integer.parseInt(strElement[8].trim());
                    } else {
                        element.tech = 0;
                    }

                    // get state
                    if (strElement.length > 9 && strElement[9] != null) {
                        element.state = strElement[9].trim();
                    } else {
                        element.state = "<no state>";
                    }

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
        this.gameJarPath = gameJarPath;

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
