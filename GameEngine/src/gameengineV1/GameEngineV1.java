/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineV1;

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
public class GameEngineV1 implements GameEngine
{

    SpaceGrid grid;
    GameVisualizer vis;
    Queue<GameCommand> queue;
    GameEngineThread gameThread;
    GameState gameState;
    String gameJarPath;

    @Override
    public boolean isAlive()
    {
        return gameThread.isAlive();
    }
    
    @Override
    public void initFromFile(GameVisualizer v, String gameJarPath, String savedGameFile)
    {
        GameElementSpec[] savedGame;
        
        this.vis = v;
        this.gameJarPath = gameJarPath;

        //
        // read initial config from file
        //
        v.debugOut("GameEngineV1: read config file: " + savedGameFile);
        
        ArrayList<GameElementSpec> elements = new ArrayList<GameElementSpec>(50);
        try
        {
            //
            // Every line has kind, package, class, state in csv format
            //
            BufferedReader in = new BufferedReader(new FileReader(this.gameJarPath + savedGameFile));
            String strLine;
            while ((strLine = in.readLine()) != null)
            {
                vis.debugOut("GameEngineV1: read config line: " + strLine);
                String[] strElement = strLine.split(",");

                if (strElement.length == 0)
                {
                    continue;
                }
                // need at least the code
                if (strElement.length == 0 || strElement[0] == null)
                {
                    throw new ParseException(strLine, 0);
                }

                GameElementSpec element = new GameElementSpec(strElement[0]);

                // get package name
                if (strElement.length > 1 && strElement[1] != null)
                {
                    element.packageName = strElement[1].trim();
                } else
                {
                    element.packageName = "";
                }

                // get class name
                if (strElement.length > 2 && strElement[2] != null)
                {
                    element.className = strElement[2].trim();
                } else
                {
                    element.packageName = "";
                }

                // get state
                if (strElement.length > 3 && strElement[3] != null)
                {
                    element.className = strElement[3].trim();
                } else
                {
                    element.state = "<no state>";
                }

                // add to list
                elements.add(element);
                v.debugOut("GameEngineV1:init:Parsed element " + element.packageName + ":"
                        + element.className + ", "
                        + element.state);
            }
            in.close();
        } catch (Exception e)
        {
            v.debugOut("GameEngineV1:init:File parse error");
            v.debugOut("GameEngineV1:init:     " + e.getMessage());
        }

        // convert list into array to process as if it had been saved
        savedGame = new GameElementSpec[1];
        savedGame = elements.toArray(savedGame);
        
        this.init(v, gameJarPath, savedGame);
        }
    
    @Override
    public void init(GameVisualizer v, String gameJarPath, GameElementSpec[] savedGame)
    {
        // 
        // save the visualizer (how we report status)
        // create a new Spacegrid (the game board)
        // set up a transfer queue that we will use to post commands to the game thread
        //

        this.vis = v;
        this.queue = new ConcurrentLinkedQueue<GameCommand>();
        gameState = GameState.Paused;
        this.gameJarPath = gameJarPath;

       
        
        //
        // did we get a saved game?
        //
        if (savedGame == null)
        {
            //
            // read initial config from file
            //
            ArrayList<GameElementSpec> elements = new ArrayList<GameElementSpec>(50);
            try
            {
                //
                // Every line has kind, package, class, state in csv format
                //
                BufferedReader in = new BufferedReader(new FileReader(this.gameJarPath + "ephemera_initial_setup.csv"));
                String strLine;
                while ((strLine = in.readLine()) != null)
                {
                    vis.debugOut("GameEngineV1: read config line: " + strLine);
                    String[] strElement = strLine.split(",");

                    if (strElement.length == 0)
                    {
                        continue;
                    }
                    // need at least the code
                    if (strElement.length == 0 || strElement[0] == null)
                    {
                        throw new ParseException(strLine, 0);
                    }

                    GameElementSpec element = new GameElementSpec(strElement[0]);

                    // get package name
                    if (strElement.length > 1 && strElement[1] != null)
                    {
                        element.packageName = strElement[1].trim();
                    } else
                    {
                        element.packageName = "";
                    }

                    // get class name
                    if (strElement.length > 2 && strElement[2] != null)
                    {
                        element.className = strElement[2].trim();
                    } else
                    {
                        element.packageName = "";
                    }

                    // get state
                    if (strElement.length > 3 && strElement[3] != null)
                    {
                        element.className = strElement[3].trim();
                    } else
                    {
                        element.state = "<no state>";
                    }

                    // add to list
                    elements.add(element);
                    v.debugOut("GameEngineV1:init:Parsed element " + element.packageName + ":"
                            + element.className + ", "
                            + element.state);
                }
                in.close();
            } catch (Exception e)
            {
                v.debugOut("GameEngineV1:init:File parse error");
                v.debugOut("GameEngineV1:init:     " + e.getMessage());
            }

            // convert list into array to process as if it had been saved
            savedGame = new GameElementSpec[1];
            savedGame = elements.toArray(savedGame);
        }

        //
        // Right here we have a saved game to work with, no matter how we got it.
        //
        
        vis.debugOut("GameEngine: Creating thread");
        this.gameThread = new GameEngineThread(this);
        vis.debugOut("GameEngine: Starting thread");
        this.gameThread.start();

        //
        // queue every game element
        //
        vis.debugOut("GameEngineV1: size of config: " + Integer.toString(savedGame.length));
        for (GameElementSpec element : savedGame)
        {
            GameCommand gc = new GameCommand(GameCommandCode.AddElement);
            GameElementSpec[] elements = new GameElementSpec[1];
            elements[0] = element;
            gc.parameters = elements;

            vis.debugOut("GameEngine: Queueing new game element");
            queueCommand(gc);
        }

        //
        // start the game
        //
    }

    @Override
    public void queueCommand(GameCommand gc)
    {
        //
        // queue alien info to synchronized queue
        //
        
        vis.debugOut("GameEngineV1: Queueing command "+gc.code.toString());

        synchronized (this.queue)
        {
            this.queue.add(gc);
            this.queue.notify();
        }
    }

    @Override
    public GameElementSpec[] saveGame()
    {
        return new GameElementSpec[1];
    }

    
}
