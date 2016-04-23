/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import gameengineinterfaces.AlienSpec;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import gameengineinterfaces.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author gmein
 */
class VisualizationGrid implements GameVisualizer {

    public Cell[][] grid;
    public Canvas canvas;
    public SpeciesSet speciesSet;
    public ArrayList<StarForDisplay> stars;
    public ArrayList<PlanetForDisplay> planets;

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
    private final Object monitor = new Object();

    public void init(GameEngine eng, ConsolePane console, SpeciesSet species, String logPath, int width, int height, int cellWidth, int cellHeight, Canvas canvas) {
        Date date = new Date();
        this.engine = eng;

        // Set up properties
        this.width = width;
        this.height = height;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.speciesSet = species;
        this.console = console;

        widthPX = width * cellWidth;
        heightPX = height * cellHeight;

        // Store ref to canvas for updating
        this.canvas = canvas;

        // cet up a grid with cells holding alien counts to display in a color code
        grid = new Cell[height][width];
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[i].length; k++) {
                grid[i][k] = new Cell(speciesSet);
            }
        }

        stars = new ArrayList<>();
        planets = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "";
        try {
            fileName = logPath + "game" + dateFormat.format(date) + ".txt";
            logFile = new BufferedWriter(new FileWriter(fileName));
        } catch (Exception e) {
            System.err.println("visgrid: Cannot create log file " + fileName);
        }
    }

    public void renderAlienView(GraphicsContext gc) {
        Color[] color = {Color.BLACK, Color.BLACK, Color.BLACK};

        gc.setFill(Color.BLACK);
        gc.fillRect(0.5, 0.5, widthPX - 0.5, heightPX - 0.5);

        for (StarForDisplay st : stars) {
            st.draw(gc, cellWidth, cellHeight);
        }

        for (PlanetForDisplay p : planets) {
            p.draw(gc, cellWidth, cellHeight);
        }

        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineWidth(1);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {

                Cell cell = grid[k][i];
                boolean isFighting = cell.isFighting();

                if (cell.cellChanged) {
                    int x = cellWidth * k;
                    int y = cellHeight * i;

                    color[1] = (isFighting ? Color.RED : Color.BLACK);
                    if (cell.alienCount == 0) {
                        if (!isFighting) {
                            //gc.setStroke(Color.BLACK);
                            //gc.strokeLine(x + 0.5, y + 0.5, x + cellWidth + 0.5, y + 0.5);
                            cell.cellChanged = false;
                            continue;
                        }
                        color[0] = Color.BLACK;
                        color[2] = Color.BLACK;
                    } else if (cell.alienCount == 1) {
                        color[0] = cell.getColor(1);
                        color[2] = Color.BLACK;
                    } else if (cell.alienCount == 2) {
                        color[0] = cell.getColor(1);
                        color[2] = cell.getColor(2);
                    } else {
                        color[0] = Color.WHITE;
                        color[2] = Color.WHITE;
                    }

                    if (cellWidth == 3) {
                        // hires displays, HD1080 or better
                        if (color[1] == Color.RED) {
                            gc.setStroke(Color.RED);
                            gc.strokeLine(x + 1.5, y + 1.5, x + cellWidth + 1.5, y + 1.5);
                        } else {
                            for (int l = 0; l < 3; l++) {
                                if (color[l] != Color.BLACK) {
                                    gc.setStroke(color[l]);
                                    gc.strokeLine(x + 1.5, y + 1.5, x + cellWidth / 3 + 1.5, y + 1.5);
                                }
                                x++;
                            }
                        }
                    } else {
                        // lowres displays
                        if (color[1] == Color.RED) {
                            // for fighting cells, color the whole thing red
                            color[0] = color[1];
                            color[2] = color[1];
                        }

                        gc.setStroke(color[0]);
                        gc.strokeLine(x + 1.5, y + 1.5, x + 2.5, y + 1.5);
                        x++;
                        gc.setStroke(color[2]);
                        gc.strokeLine(x + 1.5, y + 1.5, x + 2.5, y + 2.5);
                        x++;
                    }

                    cell.cellChanged = false;
                }
            }
        }
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);
        gc.strokeRect(0.5, 0.5, (width * cellWidth) + 1.5, heightPX + 1.5);
    }

    public void renderEnergyMap(GraphicsContext gc) {
        Color color = Color.BLACK;

        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineWidth(1);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {

                Cell cell = grid[k][i];
                boolean isFighting = cell.isFighting(); // doing this so fight counter goes down

                //if (cell.cellChanged) {
                int x = cellWidth * k;
                int y = cellHeight * i;
                int j = 1; // draw 1 cell

                if (cell.alienCount == 0) {
                    color = new Color(0, 0, Math.min(cell.energy / (double) 12 + 0.05, 1), 1.0);

                    while (j + k < width) {
                        if (grid[k + j][i].alienCount != 0 || grid[k + j][i].energy != cell.energy) {
                            break;
                        }
                        j++;
                    }

                } else {
                    //color = Color.RED;
                    color = new Color(Math.min(Math.max(cell.alienCount / (double) 8, 0.2), 1), 0, 0, 1.0);
                }

                gc.setStroke(color);
                gc.strokeLine(x + 1.5, y + 1.5, x + cellWidth * j + 1.5, y + 1.5);
                k += j - 1;
                //    cell.cellChanged = false;
                //}
            }
        }
        for (StarForDisplay st : stars) {
            st.draw(gc, cellWidth, cellHeight);
        }

        for (PlanetForDisplay p : planets) {
            p.draw(gc, cellWidth, cellHeight);
        }
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1.0);
        gc.strokeRect(0.5, 0.5, (width * cellWidth) + 1.5, heightPX + 1.5);
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long time
    ) {
        ++totalTurnCounter;
        this.numAliens = numAliens;
        debugOut("Turn #" + totalTurnCounter + " complete.");
        Utilities.runAndWait(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            renderField();

            String text = "Turns completed: " + paddedString(totalTurnCounter, 6)
                    + ", Total aliens: " + paddedString(numAliens, 7);
            //if (time > 100000000L) {
            text += ", time for turn: " + paddedTimeString(time)
                    + (numAliens > 0 ? ", time/#aliens: " + paddedTimeString(((long) time) / (((long) numAliens))) : "");
            //        + ", time/#aliens²: " + paddedTimeString(((long) time) / (((long) numAliens * (long) numAliens)));
            //}
            GUIShell.turnCounterText.setText(text);
            speciesSet.notifyListeners();
        });

        if (totalTurnCounter % 20 == 0) {
            try {
                //Thread.sleep(200);
            } catch (Exception e) {
            }
        }
    }

    public void incrementCell(int x, int y, String speciesName) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }
        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        cell.addSpecies(speciesName);
    }

    public void decrementCell(int x, int y, String speciesName) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }

        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        if (cell.alienCount > 0) {
            cell.removeSpecies(speciesName);
        } else {
            debugOut("winvis: cell underflow at (" + x + "," + y + ")");
        }
    }

    @Override
    public void showMove(AlienSpec as, int oldx, int oldy) {
        if (showMove) {
            print("Vis.showMove: " + as.toString() + ",  moved from (");
            print(oldx + "," + oldy + ")");
        }

        String speciesName = as.getFullSpeciesName();
        int x = as.x;
        int y = as.y;

        //    Utilities.runSafe(new Runnable() {
        //        @Override
        //        public void run() {
        decrementCell(oldx, oldy, speciesName);
        incrementCell(x, y, speciesName);
        //        }
        //    });
    }

    public void markFight(int x, int y) {
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("winvis: Out of bounds: (" + x + ":" + y + ")");
            return;
        }
        Cell cell = grid[x + (width / 2)][y + (height / 2)];
        cell.fight();
    }

    @Override
    public void showFightBefore(int x, int y, List<AlienSpec> participants) {
        if (showFights) {
            println("Vis.showFightBefore at (" + x + "," + y + "):");
            for (AlienSpec as : participants) {
                print("Alien \"" + as.getFullName() + "\" ");
                print(" is fighting with power " + as.actionPower);
                println("");
            }
        }
        markFight(x, y);
    }

    @Override
    public void showFightAfter(int x, int y, List<AlienSpec> participants) {
        if (showFights) {
            println("");
            println("Here is what is left from that fight:");
            for (AlienSpec as : participants) {
                print("Alien \"" + as.getFullName() + "\" ");
                print(" now has tech/energy " + as.getTechEnergyString());
                println("");
            }
        }
    }

    @Override
    public void showSpawn(AlienSpec as) {
        if (showSpawn) {

            print("Spawn: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        } else {
            debugOut("Spawn: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        }
        //  Platform.runLater(new Runnable() {
        //      @Override
        //      public void run() {
        speciesSet.addAlien(as.getFullSpeciesName());
        incrementCell(as.x, as.y, as.getFullSpeciesName());
        //      }
        //  });
    }

    public void showDeath(AlienSpec as) {
        if (showDeath) {

            print("Death: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        } else {
            debugOut("Death: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        }
        //    Platform.runLater(new Runnable() {
        //        @Override
        //        public void run() {
        decrementCell(as.x, as.y, as.getFullSpeciesName());
        speciesSet.removeAlien(as.getFullSpeciesName());
        //        }
        //    });
        //    Thread.yield();
    }

    @Override
    public void showGameOver() {
        GUIShell.gameOver = true;
        try {
            logFile.close();
        } catch (Exception e) {
        }
        Utilities.runSafe(() -> GUIShell.startOrPauseGame(new ActionEvent()));
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
        Thread.yield();
    }

    @Override
    public void debugOut(String s) {
        if (filter != null) {
            if (s.toLowerCase().contains(filter.toLowerCase())) {
                println(s);
                Thread.yield();
            }
        } else {
            printlnLogOnly(s);
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
        //Thread.yield();
        return true;
    }

    @Override
    public void showEngineStateChange(GameState gs) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Thread.yield();
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    private void printlnLogOnly(String s) {
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
    public void registerSpecies(AlienSpec as) {
        speciesSet.addAlienSpecies(as);
    }

    @Override
    public void registerStar(int x, int y, String name, int magnitude) {
        Utilities.runSafe(() -> stars.add(new StarForDisplay(x + this.width / 2, y + this.height / 2, name, magnitude)));
    }

    @Override
    public void registerPlanet(int x, int y, String name, int energy, int tech) {
        Utilities.runSafe(() -> planets.add(new PlanetForDisplay(x + this.width / 2, y + this.height / 2, name, energy, tech)));
    }

    @Override
    public void mapEnergy(int x, int y, int energy) {
        this.grid[(x + (width / 2))][((y + height / 2))].energy = energy;
    }

    @Override
    public void showReady() {
        Utilities.runAndWait(() -> renderField());

    }

    public void renderField() {
        if (GUIShell.renderSelectorAliens.isSelected()) {
            renderAlienView(this.canvas.getGraphicsContext2D());
        } else {

            renderEnergyMap(this.canvas.getGraphicsContext2D());
        }

    }

}
