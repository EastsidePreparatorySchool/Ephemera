/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import alieninterfaces.AlienShapeFactory;
import alieninterfaces.PlanetSpec;
import gameengineinterfaces.AlienSpec;
import gameengineinterfaces.GameState;
import gameengineinterfaces.GameVisualizer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.google.gson.Gson;
import alieninterfaces.StarSpec;


/**
 *
 * @author gm
 */
public class VisualizationStreamer implements GameVisualizer {

    BufferedWriter stateLog;
    BufferedWriter progressLog;
    String path;
    Gson gson;

    VisualizationStreamer(String path) {
        this.path = path;
        this.gson = new Gson();
    }

    String makeRecord(String p, Object o) {
        String r = gson.toJson(o);
        String s =  ("0000"+r.length());
        p = p+"         ";
        p = p.substring(0,9);
        return p+"("+s.substring(s.length()-4)+"):"+r;
    }
    
    @Override
    public void registerSpecies(AlienSpec as, AlienShapeFactory asf) {
        String s = makeRecord("REGSPEC",as);
        println(stateLog, s);
        println(progressLog, s);
    }

    @Override
    public void registerStar(int x, int y, String name, int index, double luminosity) { //[Q]
        String s = makeRecord("REGSTAR",new StarSpec(x, y, name, index, luminosity));
        println(stateLog, s);
        println(progressLog, s);
    }

    @Override
    public void registerPlanet(int x, int y, String name, int index, double energy, int tech) { //[Q]
        String s = makeRecord("REGPLANET",new PlanetSpec(x, y, name, index, energy, tech));
        println(stateLog, s);
        println(progressLog, s);
    }

    @Override
    public void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech) { //[Q]
    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
    }

    @Override
    public void showReady() {
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long timeTaken, double avgTech) {
    }

    @Override
    public void showIdleUpdate(int numAliens) {
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
    }

    @Override
    public void showMove(AlienSpec as, int oldX, int oldY, double energyAtNewPosition, double energyAtOldPosition) { //[Q]
    }

    @Override
    public void showFight(int x, int y) {
    }

    @Override
    public void showSpawn(AlienSpec as, double energyAtPos) {
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
    }

    @Override
    public void showEngineStateChange(GameState gs) {
    }

    @Override
    public void showUpdateAfterRequestedActions() {
    }

    @Override
    public void showGameOver() {
    }

    @Override
    public void debugOut(String s) {
    }

    @Override
    public void debugErr(String s) {
    }

    @Override
    public void setFilter(String s) {
    }

    @Override
    public void setChatter(boolean f) {
    }

    @Override
    public boolean showContinuePrompt() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void init() {
        createFiles(path);
    }

    @Override
    public void shutdown() {
        closeFiles();
    }

    private void createFiles(String path) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        String fileName = "";
        try {
            fileName = dateFormat.format(date) + ".txt";
            stateLog = new BufferedWriter(new FileWriter(path + "stateLog" + fileName));
            progressLog = new BufferedWriter(new FileWriter(path + "progressLog" + fileName));
        } catch (Exception e) {
            System.err.println("visgrid: Cannot create log file " + fileName);
        }
    }

    private void closeFiles() {
        try {
            stateLog.close();
            progressLog.close();
        } catch (Exception e) {
        }
    }

    private void print(BufferedWriter file, String s) {
        try {
            file.write(s);
            file.flush();
        } catch (Exception e) {
        }
    }

    private void println(BufferedWriter file, String s) {
        try {
            file.write(s);
            file.newLine();
            file.flush();
        } catch (Exception e) {
        }
    }

}
