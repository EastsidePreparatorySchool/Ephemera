/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineimplementation;

import org.eastsideprep.spacecritters.gameengineinterfaces.GameElementKind;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameState;
import org.eastsideprep.spacecritters.gamelogic.*;
import org.eastsideprep.spacecritters.gameengineinterfaces.*;
import java.lang.reflect.Field;

/**
 *
 * @author gmein
 */
public class GameEngineThread extends Thread {

    final private GameEngineV2 engine;
    boolean pastReady = false;
    public int sleepTime = 50;
    int lastLogSize = -1;

    public GameEngineThread(GameEngineV2 ge) {
        engine = ge;

        setName("GameEngineThread:" + ge.name);
    }

    @Override
    public void run() {
        GameCommand gc;
        int totalTurns = 0;
        boolean endGame = false;
        try {
            //Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            Achievement[] achievements = new Achievement[1];
            achievements[0] = new Achievement(1);
            AchievementReq req = new AchievementReq(AchievementFlag.AlienKilledByFighting);
            achievements[0].addReq(req, 0);

            engine.grid = new SpaceGrid(engine, engine.vis, Constants.width, Constants.height, achievements);

            engine.vis.debugOut("GameEngineThread: Started");
            do {
                try {
                    do {
                        // do we have work requests? Only peek, don't wait
                        while (!engine.queue.isEmpty()) {
                            gc = (GameCommand) engine.queue.remove();
                            // Process the work item, true means exit

                            endGame = processCommand(gc);
                            if (endGame) {
                                break;
                            }
                        }

                        if (engine.gameState == GameState.Paused) {
                            // show idle updates
                            if (pastReady) {
                                engine.vis.showIdleUpdate(engine.grid.getNumAliens());
                            }
                            try {
                                synchronized (engine.queue) {
                                    engine.queue.wait();
                                }
                            } catch (InterruptedException e) {
                                System.err.println("GEThread interrupted: " + e.getMessage());
                                //e.printStackTrace(System.err);
                            }
                        }
                    } while (engine.gameState == GameState.Paused);

                    // Execute game turn
                    long startTurnTime = System.nanoTime();
                    try {
                        if (engine.grid.executeGameTurn()) {
                            // return true: game over because at most one species left
                            engine.gameState = GameState.Paused;
                            engine.vis.showGameOver();
                        }
                    } catch (Exception e) {
                        engine.vis.debugErr("GameEngineThread: Unhandled exception during turn: " + e.getMessage());
                        e.printStackTrace();
                    }
                    totalTurns++;
                    engine.vis.showCompletedTurn(totalTurns, engine.grid.getNumAliens(), System.nanoTime() - startTurnTime, engine.grid.getTech());

                    // adjustable sleeping, to make sure the log does not grow too big
//                    if (lastLogSize == -1) {
//                        // start of game, not important
//                        lastLogSize = engine.log.getLogSize();
//                    }
//
//                    int newLogSize = engine.log.getLogSize();
//                    if (newLogSize > (this.lastLogSize * 1.5)) {
//                        // 50% bigger -> twice the waiting
//                        this.sleepTime *= 2;
//                        this.sleepTime = Math.min(this.sleepTime, 10000);
//                    } else if (newLogSize < (this.lastLogSize / 1.1)) {
//                        // 10% smaller -> a little less waiting
//                        this.sleepTime = (int) (this.sleepTime * 0.9);
//                        this.sleepTime = Math.max(this.sleepTime, 50);
//                    }
//                    this.lastLogSize = newLogSize;
                    this.sleepTime = this.engine.log.getLogSize() / 50;
                    this.sleepTime = Math.min(this.sleepTime, 5000);
                    this.sleepTime = Math.max(this.sleepTime, 20);
                    Thread.sleep(this.sleepTime);

                } catch (Exception e) {
                    e.printStackTrace();
                    engine.vis.debugErr("GameThread: Unknown exception: " + e.getMessage());
                    break;
                }

            } while (!endGame);
            engine.vis.debugOut("GameThread: Exit");
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private boolean processCommand(GameCommand gc) throws Exception {
        GameElementSpec element;
        boolean gameOver = false;
        switch (gc.code) {
            case SetConstant:
                String variable = (String) gc.parameters[0];
                String value = (String) gc.parameters[1];

                for (Field field : Constants.class
                        .getDeclaredFields()) {
                    if (variable.equalsIgnoreCase(field.getName())) {
                        if (field.getType().getName().equals("String")) {
                            field.set(Constants.class,
                                    value);

                        } else if (field.getType().getName().equals("int") || field.getType().getName().equals("Integer")) {
                            field.set(Constants.class,
                                    Integer.parseInt(value));
                        } else if (field.getType().getName().equalsIgnoreCase("boolean")) {
                            value = value.equalsIgnoreCase("on") ? "true" : value;
                            value = value.equalsIgnoreCase("off") ? "false" : value;
                            field
                                    .set(Constants.class,
                                            Boolean.parseBoolean(value));
                        }
                    }
                }

                break;

            case AddElement:
                element = (GameElementSpec) gc.parameters[0];
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

                    if (element.kind == GameElementKind.CONSTANT) {
                        for (Field field : Constants.class
                                .getDeclaredFields()) {
                            if (element.className.equalsIgnoreCase(field.getName())) {
                                if (field.getType().getName().equals("java.lang.String")) {
                                    field.set(Constants.class,
                                            element.state);

                                } else if (field.getType().getName().equals("int") || field.getType().getName().equals("Integer")) {
                                    field.set(Constants.class,
                                            Integer.parseInt(element.state));

                                } else if (field.getType().getName().equalsIgnoreCase("double")) {
                                    field.set(Constants.class,
                                            Double.parseDouble(element.state));
                                } else if (field.getType().getName().equalsIgnoreCase("boolean")) {
                                    element.state = element.state.equalsIgnoreCase("on") ? "true" : element.state;
                                    element.state = element.state.equalsIgnoreCase("off") ? "false" : element.state;
                                    field
                                            .set(Constants.class,
                                                    Boolean.parseBoolean(element.state));
                                }
                            }
                        }
                    }
                }
                break;

            case KillElement:
                element = (GameElementSpec) gc.parameters[0];
//                engine.vis.debugOut("GameEngineThread: Processing Kill of GameElement " + element.kind.name() + " "
//                        + element.packageName + ":" + element.className + ", " + element.state);

                if (element.kind != GameElementKind.INVALID) {
                    // kill all aliens of a certain species
                    if (element.kind == GameElementKind.ALIEN) {
                        engine.grid.killAll(element);
                    }
                }
                break;

            case Pause:
                engine.vis.debugOut("GameEngineThread: Pausing");
                engine.gameState = GameState.Paused;
                engine.vis.showEngineStateChange(GameState.Paused);
                break;

            case Resume:
                engine.vis.debugOut("GameEngineThread: Resuming");
                engine.vis.debugOut("---------------------------------- Game segment starts here:");
                engine.gameState = GameState.Running;
                engine.vis.showEngineStateChange(GameState.Running);
                break;

            case List:
                engine.grid.listStatus();
                break;

            case Ready:
                engine.grid.addAllCustomAliens();
                engine.grid.ready(); // will cause vis.showReady
                pastReady = true;
                break;

            case End:
                engine.vis.debugOut("GameEngineThread: End request, thread closing");
                gameOver = true;
                engine.vis.showGameOver();
                break;

            default:
                break;
        }

        return gameOver;
    }

}
