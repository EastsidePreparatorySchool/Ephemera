/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import alieninterfaces.AlienShapeFactory;
import alieninterfaces.Position;
import gameengineinterfaces.AlienSpec;
import gameengineinterfaces.*;
import gamelogic.Constants;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;

/**
 *
 * @author gmein
 */
public class VisualizationGrid implements GameVisualizer {

    public SpaceCritters gameShell;
    public Cell[][] grid;
    public Canvas canvas;
    public SpeciesSet speciesSet;

    public int width;
    public int height;

    ConsolePane console;

    int turnCounter = 1;
    int totalTurnCounter = 0;
    int numTurns = 1;
    int numAliens = 0;
    boolean showMove = false;
    boolean showFights = false;
    boolean showSpawn = false;
    boolean showDeath = false;
    String filter = null;
    String[] filters = null;

    BufferedWriter logFile;
    GameEngine engine;
    long msLastTurn;

    public void initField(SpaceCritters gameShellInstance, GameEngine eng, ConsolePane console,
            SpeciesSet species, String logPath, int width, int height) {
        this.gameShell = gameShellInstance;
        this.engine = eng;

        // Set up properties
        this.width = width;
        this.height = height;
        this.speciesSet = species;
        this.console = console;

        // logfile
        createLogFile(logPath);

        // cet up a grid with cells holding alien counts to display in a color code
        grid = new Cell[height][width];
        for (int i = 0; i < grid.length; i++) {
            for (int k = 0; k < grid[i].length; k++) {
                grid[i][k] = new Cell(gameShell, speciesSet, i, k);
            }
        }
    }

    private void createLogFile(String logPath) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "";
        try {
            fileName = logPath + "game" + dateFormat.format(date) + ".txt";
            logFile = new BufferedWriter(new FileWriter(fileName));
        } catch (Exception e) {
            System.err.println("visgrid: Cannot create log file " + fileName);
        }
    }

    // translation of (x,y) to array element
    Cell getCell(int x, int y) { //[Q]
        return grid[x + (width / 2)][y + (height / 2)];
    }

    @Override
    public void showIdleUpdate(int numAliens) {
        this.numAliens = numAliens;

        Utilities.runAndWait(() -> {
            String text = "Aliens: " + paddedString(numAliens, 7);
            this.gameShell.controlPane.alienNumber.setText(text);

            speciesSet.notifyListeners();
            gameShell.mainScene.update();
        });
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long time, double tech) {
        ++totalTurnCounter;
        this.numAliens = numAliens;
        debugOut("Turn #" + totalTurnCounter + " complete.");
        Utilities.runAndWait(() -> {

            String text = "Turns:          " + paddedString(totalTurnCounter, 7);
            this.gameShell.controlPane.turnCounter.setText(text);

            text = "Aliens:         " + paddedString(numAliens, 7);
            this.gameShell.controlPane.alienNumber.setText(text);

            text = "t(turn):        " + paddedTimeString(time);
            this.gameShell.controlPane.timeForTurn.setText(text);

            text = "1000*t/#/tech^2:" + paddedTimeString((long)(1000*time/(numAliens == 0?numAliens:1)/(tech*tech)));
            this.gameShell.controlPane.timeForTurnAndAlien.setText(text);

            speciesSet.notifyListeners();
            gameShell.mainScene.update();
        });
    }

    @Override
    public void showMove(AlienSpec as, double oldx, double oldy, double energyAtNew, double energyAtOld) { //[Q]
        int x = as.p.round().x; //!!!!!!!!!!!!
        int y = as.p.round().y;

        Alien3D alien = gameShell.mainScene.aliens.get(as.hashCode);
        if (alien != null) {
            // not done for residents
            alien.recordMoveTo(x, y);
        }
    }

    @Override
    public void showFight(int x, int y) {
        getCell(x, y).fight();
    }

    @Override
    public void showSpawn(AlienSpec as, double energyAtPos) {
        debugOut("Engine reporting Spawn: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        AlienShapeFactory asf = speciesSet.addAlien(as.getFullSpeciesName(), as.speciesID);
        gameShell.mainScene.createAlien(as, as.hashCode, as.p.round().x, as.p.round().y, asf); //!!!!!!!!!!!!!!!
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
        debugOut("Engine reporting death: " + as.getFullName() + " at " + as.getXYString() + " with TE: " + as.getTechEnergyString());
        speciesSet.removeAlien(as.getFullSpeciesName(), as.speciesID);
        gameShell.mainScene.destroyAlien(as, as.hashCode, as.p.round().x, as.p.round().y); //!!!!!!!!!!!!!!!!

    }

    @Override
    public void showGameOver() {
        gameShell.gameOver = true;
        try {
            logFile.close();
        } catch (Exception e) {
        }
        debugOut("Game Over");
        Utilities.runAndWait(() -> this.gameShell.startOrPauseGame(new ActionEvent()));
    }

    @Override
    public void debugErr(String s) {
        println(s);
        System.out.println(s);
    }

    @Override
    public void debugOut(String s) {
        if (gameShell.consoleStage.isShowing()) {
            if (filter != null && !filter.trim().equals("") && filters != null) {
                for (String f : filters) {
                    if (s.toLowerCase().contains(f.toLowerCase())) {
                        println(s);
                        break;
                    }
                }
            } else {
                // no filter
                println(s);
            }
        } else {
            // console not showing
            printlnLogOnly(s);
        }
    }

    @Override
    public boolean showContinuePrompt() {
        // not done with number of turns before prompt, don't display anything, return "game not over"
        return true;
    }

    @Override
    public void showEngineStateChange(GameState gs) {
        Thread.yield();
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
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
        s = "              " + s;
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
    public void registerSpecies(AlienSpec as, AlienShapeFactory asf) {
        speciesSet.addAlienSpecies(as, asf);
    }

    @Override
    public void registerStar(Position p, String name, int index, double luminosity) { //[Q]
        Utilities.runAndWait(() -> gameShell.mainScene.createStar(p.round().x, p.round().y, name, index, luminosity)); //!!!!!!!!!!!!!!

    }

    @Override
    public void registerPlanet(Position p, String name, int index, double energy, int tech) { //[Q]
        Utilities.runAndWait(() -> gameShell.mainScene.createPlanet(p.round().x, p.round().y, name, index, energy, tech)); //!!!!!!!!!!!!!!

    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
        this.grid[(x + (width / 2))][((y + height / 2))].energy = energy;
    }

    @Override
    public void showReady() {
        Utilities.runAndWait(() -> {
            String text = "Aliens: " + paddedString(numAliens, 7);
            this.gameShell.controlPane.alienNumber.setText(text);

            speciesSet.notifyListeners();
            gameShell.mainScene.update();
            gameShell.stage.show();

            if ((boolean) Constants.getValue("autoStart")) {
                this.gameShell.startGame();
            }
        });
    }

    @Override
    public void setFilter(String s) {
        filter = s;
        filters = null;

        this.gameShell.consolePane.filter.setText(s);
        if (s != null) {
            filters = s.split(";");
            for (int i = 0; i < filters.length; i++) {
                filters[i] = filters[i].trim();
            }
        }
    }

    @Override
    public void setChatter(boolean f) {
        this.gameShell.consolePane.chatter.setSelected(f);
    }

    @Override
    public void showPlanetMove(Position oldP, Position p, String name, int index, double energy, int tech) { //[Q]
        gameShell.mainScene.planets.get(index).recordMoveTo(p.round().x, p.round().y); //!!!!!!!!!!!!
    }

    void setRenderMode(String renderMode) {
        switch (renderMode) {
            case "Aliens":
                break;
            case "Energy":
                break;
            case "Tech":
                break;
            default:
                break;
        }
    }

    @Override
    public void showUpdateAfterRequestedActions() {
        Utilities.runAndWait(() -> {
            gameShell.mainScene.update();
        });
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }
}
