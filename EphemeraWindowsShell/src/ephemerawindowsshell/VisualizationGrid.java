/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemerawindowsshell;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import gameengineinterfaces.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author gmein
 */
class VisualizationGrid implements GameVisualizer {

    public Cell[][] grid;
    public Canvas canvas;
    public SpeciesSet species;

    private int width;
    private int height;
    private int cellWidth;
    private int cellHeight;
    public int widthPX;
    public int heightPX;

    public ConsolePane console;

    private static final Color[] colors = {Color.BLACK, Color.SLATEBLUE, Color.DARKVIOLET, Color.MAGENTA, Color.PURPLE, Color.RED};

    int turnCounter = 1;
    int totalTurnCounter = 0;
    int numTurns = 1;
    int numAliens = 0;
    boolean showMove = false;
    boolean showFights = false;
    boolean showSpawn = false;
    boolean showDeath = false;
    String filter = null;

    BufferedWriter logFile;
    GameEngine engine;

    public void init(GameEngine eng, ConsolePane console, SpeciesSet species, String path, int width, int height, int cellWidth, int cellHeight, Canvas canvas) {
        Date date = new Date();
        this.engine = eng;

        // Set up properties
        this.width = width;
        this.height = height;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.species = species;
        this.console = console;

        widthPX = width * cellWidth;
        heightPX = height * cellHeight;

        // Store ref to canvas for updating
        this.canvas = canvas;

        // cet up a grid with cells holding alien counts to display in a color code
        grid = new Cell[height][width];
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[i].length; k++) {
                grid[i][k] = new Cell();
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        try {
            logFile = new BufferedWriter(new FileWriter(path + "game" + dateFormat.format(date) + ".txt"));
        } catch (Exception e) {
            System.err.println("visgrid: Cannot create log file");
        }
    }

    public void renderOnScreen(GraphicsContext gc) {
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[i].length; k++) {
                Cell cell = grid[i][k];
                boolean isFighting = cell.isFighting();
                if (cell.cellChanged) {
                    if (cell.alienCount == 0) {
                        // no aliens, just paint fighting residue
                        if (isFighting) {
                            gc.setFill(Color.BLACK);
                            gc.fillRect(cellWidth * k, cellHeight * i, cellWidth / 3, cellHeight);
                            gc.setFill(Color.RED);
                            gc.fillRect(cellWidth * k + 1, cellHeight * i, cellWidth / 3, cellHeight);
                            gc.setFill(Color.BLACK);
                            gc.fillRect(cellWidth * k + 2, cellHeight * i, cellWidth / 3, cellHeight);
                        } else {
                            gc.setFill(Color.BLACK);
                            gc.fillRect(cellWidth * k, cellHeight * i, cellWidth, cellHeight);
                        }
                    } else if (cell.alienCount == 1) {
                        // one alien, paint in color in center
                        gc.setFill(cell.getColor(1));
                        gc.fillRect(cellWidth * k + 1, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(isFighting ? Color.RED : Color.BLACK);
                        gc.fillRect(cellWidth * k, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(Color.BLACK);
                        gc.fillRect(cellWidth * k + 2, cellHeight * i, cellWidth / 3, cellHeight);
                    } else if (cell.alienCount == 2) {
                        // two aliens, paint left and right in their own color
                        gc.setFill(cell.getColor(1));
                        gc.fillRect(cellWidth * k, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(isFighting ? Color.RED : Color.BLACK);
                        gc.fillRect(cellWidth * k + 1, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(cell.getColor(2));
                        gc.fillRect(cellWidth * k + 2, cellHeight * i, cellWidth / 3, cellHeight);
                    } else {
                        // more than two aliens, paint left and right in white
                        gc.setFill(Color.WHITE);
                        gc.fillRect(cellWidth * k, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(isFighting ? Color.RED : Color.BLACK);
                        gc.fillRect(cellWidth * k + 1, cellHeight * i, cellWidth / 3, cellHeight);
                        gc.setFill(Color.WHITE);
                        gc.fillRect(cellWidth * k + 2, cellHeight * i, cellWidth / 3, cellHeight);
                    }
                    cell.cellChanged = false;
                }
            }
        }
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);
        gc.strokeOval(0.5, 0.5, widthPX - 0.5, heightPX - 0.5);
    }

    public Color getColor(int alienCount) {
        if (alienCount < 0) {
            return colors[0];
        }
        if (alienCount >= colors.length) {
            return colors[colors.length - 1];
        } else {
            return colors[alienCount];
        }
    }

    public void incrementCell(int x, int y, String packageName, String className) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }
        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        cell.addSpecies(packageName, className, species.getColor(packageName, className));
    }

    public void decrementCell(int x, int y, String packageName, String className) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }

        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        if (cell.alienCount > 0) {
            cell.removeSpecies(packageName, className);
        } else {
            debugOut("winvis: cell underflow at (" + x + "," + y + ")");
        }
    }

    public void markFight(int x, int y) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }
        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        cell.fight();
    }

    private void print(String s) {
        console.print(s);
        try {
            logFile.write(s);
            logFile.flush();
        } catch (Exception e) {
        }
    }

    private void println(String s) {
        console.println(s);
        try {
            logFile.write(s);
            logFile.newLine();
            logFile.flush();
        } catch (Exception e) {
        }
    }

    String paddedString(String s, int space) {
        s = "          ".substring(0, space) + s;
        return s.substring(s.length() - space);
    }

    String paddedString(long i, int space) {
        return paddedString(Long.toString(i), space);
    }

    String paddedTimeString(long ns) {
        return paddedString(timeString(ns), 7);
    }

    String timeString(long ns) {
        if (ns > 10000000000L) {
            return (ns / 1000000000L) + "s";
        }
        if (ns > 10000000L) {
            return (ns / 1000000L) + "ms";
        }
        if (ns > 10000L) {
            return (ns / 1000L) + (((char) 181) + "s");
        }
        return ns + "ns";
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long time) {
        ++totalTurnCounter;
        this.numAliens = numAliens;
        debugOut("Turn #" + totalTurnCounter + " complete.");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                renderOnScreen(gc);
                EphemeraWindowsShell.turnCounterText.setText("Turns completed: " + paddedString(totalTurnCounter, 6)
                        + ", Total aliens: " + paddedString(numAliens, 7)
                        + ", time for turn: " + paddedTimeString(time)
                        + ", time/#aliens: " + paddedTimeString(((long) time) / (((long) numAliens)))
                        + ", time/#aliens²: " + paddedTimeString(((long) time) / (((long) numAliens * (long) numAliens)))
                );
                //EphemeraWindowsShell.armPauseButton();
            }
        });
        
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        
        Thread.yield();
    }

    @Override
    public void showMove(String packageName, String className, int id, int oldx, int oldy, int newX, int newY, int energy, int tech) {
        if (showMove) {
            print("Vis.showMove: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") moved from (");
            print(oldx + "," + oldy + ") to (");
            print(newX + "," + newY);
            println("), E:" + energy + ", T:" + tech);
        }
        decrementCell(oldx, oldy, packageName, className);
        incrementCell(newX, newY, packageName, className);
    }

    @Override
    public void showFightBefore(int x, int y, String[] participants, Integer[] fightPowers) {
        if (showFights) {
            println("Vis.showFightBefore at (" + x + "," + y + "):");
            for (int i = 0; i < participants.length; i++) {
                print("Alien " + participants[i] + " ");
                println("is fighting with energy " + fightPowers[i]);
            }
        }
        markFight(x, y);
    }

    @Override
    public void showFightAfter(int x, int y, String[] participants, int[] newEnergy, int[] newTech) {
        if (showFights) {
            println("");
            println("Here is what is left from that fight:");
            for (int i = 0; i < participants.length; i++) {
                print("Alien \"" + participants[i] + "\" ");
                print(" now has energy " + newEnergy[i]);
                print(" and new tech level " + newTech[i]);
            }
            println("");
        }
    }

    public void showSpawn(String packageName, String className, int id, int newX, int newY, int energy, int tech) {
        if (showSpawn) {
            print("Vis.showSpawn: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            print(newX + "," + newY);
            println("), E:" + energy + ", T:" + tech);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                species.addAlien(packageName, className);
                incrementCell(newX, newY, packageName, className);
            }
        });
    }

    public void showDeath(String packageName, String className, int id, int oldX, int oldY) {
        if (showDeath) {
            print("Death: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            println(oldX + "," + oldY + ")");
        } else {
            debugOut("Death: " + packageName + ":" + className
                    + "(" + Integer.toHexString(id).toUpperCase() + ") at ("
                    + oldX + "," + oldY + ")");
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                decrementCell(oldX, oldY, packageName, className);
                species.removeAlien(packageName, className);
            }
        });
    }

    @Override
    public void showSection(int x, int y, int width) {
        println("Showing sections costs extra");
    }

    @Override
    public void highlightPositions() {
    }

    @Override
    public void showGameOver() {
        EphemeraWindowsShell.gameOver = true;
        try {
            logFile.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void debugErr(String s) {
        println(s);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                engine.queueCommand(new GameCommand(GameCommandCode.Pause));
            }
        });
    }

    @Override
    public void debugOut(String s) {
        if (filter != null) {
            if (s.toLowerCase().contains(filter.toLowerCase())) {
                println(s);
            }
        }
    }

    @Override
    public boolean showContinuePrompt() {
        /*
         --turnCounter;

         // every numTurns, display prompt, wait for exit phrase or new number of turns
         if (turnCounter == 0) {
         turnCounter = numTurns;
         engine.queueCommand(new GameCommand(GameCommandCode.Pause));
         }
         */

        // not done with number of turns before prompt, don't display anything, return "game not over"
        return true;
    }
}
