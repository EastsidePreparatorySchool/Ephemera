/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineV1;

import gameengineinterfaces.GameElementKind;
import gameengineinterfaces.GameState;
import gamelogic.*;
import gameengineinterfaces.*;
import java.lang.reflect.Field;

/**
 *
 * @author gmein
 */
public class GameEngineThread extends Thread {

    final private GameEngineV1 engine;

    public GameEngineThread(GameEngineV1 ge) {
        engine = ge;

        setName("GameEngineThread");
    }

    @Override
    public void run() {
        GameCommand gc;
        boolean endGame = false;
        int totalTurns = 0;

        //Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        engine.grid = new SpaceGrid(engine, engine.vis, 500, 500);

        try {
            SpaceGrid.addClassPathFile(engine.gameJarPath, engine.alienPath, "alieninterfaces");
        } catch (Exception e) {
            engine.vis.debugErr("GameEngineThread: run() could not load alieninterfaces.jar");
            return;
        }

        engine.vis.debugOut("GameEngineThread: Started");
        do {
            try {
                if (engine.gameState == GameState.Running && !endGame) {
                    // giving the shell a chance to ask for confirmation for another turn here
                    boolean continueGame;
                    do {
                        continueGame = engine.vis.showContinuePrompt();
                        // do we have work requests? Only peek, don't wait
                        while (!engine.queue.isEmpty()) {
                            gc = (GameCommand) engine.queue.remove();
                            // Process the work item
                            endGame = processCommand(gc);
                            // endGame signifies that an "End" request has come through
                        }
                    } while (!continueGame && !endGame);

                    // Execute game turn
                    //
                    long startTurnTime = System.nanoTime();
                    try {

                        if (engine.grid.executeGameTurn()) {
                            // return true == game over because at most one species left
                            engine.gameState = GameState.Paused;
                            engine.vis.showGameOver();
                        }
                    } catch (Exception e) {
                        engine.vis.debugErr("GameEngineThread: Unhandled exception during turn: " + e.getMessage());
                        e.printStackTrace();
                    }
                    totalTurns++;
                    engine.vis.showCompletedTurn(totalTurns, engine.grid.aliens.size(), System.nanoTime() - startTurnTime);
                }

                //
                // between turns, process commands
                //
                if (engine.queue.isEmpty()) {
                    if (engine.gameState == GameState.Paused) {
                        //engine.vis.debugOut("GameEngineThread: About to wait for msgs");
                        synchronized (engine.queue) {
                            engine.queue.wait();
                        }
                    }
                }
                // do we have work requests?
                while (!engine.queue.isEmpty()) {
                    //engine.vis.debugOut("GameEngineThread: Dequeueing msg");

                    gc = (GameCommand) engine.queue.remove();

                    // Process the work item
                    //engine.vis.debugOut("GameEngineThread: Processing command");
                    endGame = processCommand(gc);
                    // endGame signifies that an "End" request has come through
                }

            } catch (Exception e) {
                e.printStackTrace();
                engine.vis.debugErr("GameThread: Unknown exception: " + e.getMessage());
                break;
            }
        } while (!endGame);
        engine.vis.debugOut("GameThread: Exit");
    }

    private boolean processCommand(GameCommand gc) throws Exception {
        boolean gameOver = false;
        switch (gc.code) {
            case SetVariable:
                String variable = (String) gc.parameters[0];
                String value = (String) gc.parameters[1];

                for (Field field : Constants.class.getDeclaredFields()) {
                    if (variable.equalsIgnoreCase(field.getName())) {
                        if (field.getType().getName().equals("String")) {
                            field.set(Constants.class, value);
                        } else if (field.getType().getName().equals("int") || field.getType().getName().equals("Integer")) {
                            field.set(Constants.class, Integer.parseInt(value));
                        } else if (field.getType().getName().equalsIgnoreCase("boolean")) {
                            value = value.equalsIgnoreCase("on") ? "true" : value;
                            value = value.equalsIgnoreCase("off") ? "false" : value;
                            field.set(Constants.class, Boolean.parseBoolean(value));
                        }
                    }
                }

                break;

            case AddElement:
                GameElementSpec element = (GameElementSpec) gc.parameters[0];

                engine.vis.debugOut("GameEngineThread: Processing GameElement " + element.kind.name() + " "
                        + element.packageName + ":" + element.className + ", " + element.state);

                if (element.kind != GameElementKind.INVALID) {
                    if (element.kind == GameElementKind.ALIEN) {
                        engine.grid.addElement(element);
                    }

                    // register a species for the click menu
                    if (element.kind == GameElementKind.SPECIES) {
                        engine.grid.addElement(element);
                    }

                    // If it is a SpaceObject (there could be a cleaner way to do this)
                    if (element.kind == GameElementKind.STAR
                            || element.kind == GameElementKind.PLANET
                            || element.kind == GameElementKind.RESIDENT) {
                        //engine.vis.debugErr(element.toString());
                        engine.grid.addElement(element);
                    }

                    if (element.kind == GameElementKind.VARIABLE) {
                        for (Field field : Constants.class.getDeclaredFields()) {
                            if (element.className.equalsIgnoreCase(field.getName())) {
                                if (field.getType().getName().equals("String")) {
                                    field.set(Constants.class, element.state);
                                } else if (field.getType().getName().equals("int") || field.getType().getName().equals("Integer")) {
                                    field.set(Constants.class, (int)element.energy);
                                } else if (field.getType().getName().equalsIgnoreCase("double")) {
                                    field.set(Constants.class, element.energy);
                                } else if (field.getType().getName().equalsIgnoreCase("boolean")) {
                                    element.state = element.state.equalsIgnoreCase("on") ? "true" : element.state;
                                    element.state = element.state.equalsIgnoreCase("off") ? "false" : element.state;
                                    field.set(Constants.class, Boolean.parseBoolean(element.state));
                                }
                            }
                        }
                    }
                }
                break;

            case Pause:
                engine.vis.debugOut("GameEngineThread: Pausing");
                engine.gameState = GameState.Paused;
                break;

            case Resume:
                engine.vis.debugOut("GameEngineThread: Resuming");
                engine.vis.debugOut("---------------------------------- Game segment starts here:");
                engine.gameState = GameState.Running;
                break;

            case List:
                engine.grid.listStatus();
                break;

            case Ready:
                engine.grid.ready();
                break;

            case End:
                engine.vis.debugOut("GameEngineThread: End request, thread closing");
                gameOver = true;
                break;

            default:
                break;
        }

        // this is also a good spot to 
        // prepare the random generator seed number
        // if not set from config file
        if (Constants.randSeed == 0) {
            Constants.randSeed = (int) System.nanoTime();
        }

        SpaceGrid.rand.setSeed(Constants.randSeed);
        // output randSeed into logfile
        engine.vis.debugOut("RandSeed: " + Constants.randSeed);

        return gameOver;
    }

}
