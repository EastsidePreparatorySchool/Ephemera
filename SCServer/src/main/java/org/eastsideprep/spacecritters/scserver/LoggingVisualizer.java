/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.scserver;

import org.eastsideprep.spacecritters.alieninterfaces.AlienShapeFactory;
import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameState;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameVisualizer;
import java.util.List;
import org.eastsideprep.spacecritters.alieninterfaces.IntegerPosition;
import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
import org.eastsideprep.spacecritters.gamelog.GameLog;
import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import org.eastsideprep.spacecritters.orbit.Trajectory;
import org.eastsideprep.spacecritters.scgamelog.SCGameLogEntry;

/**
 *
 * @author gm
 */
public class LoggingVisualizer implements GameVisualizer {

    GameLog log;

    public LoggingVisualizer(GameLog log) {
        this.log = log;
    }

    @Override
    public void registerSpecies(AlienSpec as, AlienShapeFactory asf, boolean instantiate) {
        System.out.println("Logging visualizer: Registering species " + as.getFullSpeciesName());
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ADDSPECIES,
                0, 0, 0, instantiate ? 1 : 0,
                null, as.getFullSpeciesName(), 0, as.speciesID,
                0.0, 0.0));
    }

    @Override
    public void registerStar(int x, int y, String name, int index, double luminosity, double mass) { //[Q]
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ADDSTAR,
                x, y, (int) luminosity, 0,
                name, null,
                index, -1,
                0.0, 0.0));

    }

    @Override
    public void registerPlanet(int x, int y, String name, int index, double energy, int tech, double mass, Trajectory t) { //[Q]
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ADDPLANET,
                x, y, 0, 0,
                name, null, -index, -1,
                0.0, 0.0));

        if (t != null) {
            IntegerPosition p = t.currentFocus.getPositionAtTime(log.turnsCompleted * Constants.deltaT).round();
            log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ORBIT,
                    p.x, p.y, 0, 0,
                    Double.toString(t.conic.rotation), null, -index, -1,
                    t.conic.e, t.conic.p / Constants.deltaX));
        }

    }

    @Override
    public void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech, double time) { //[Q]
        if (oldx == x && oldy == y) {
            return;
        }
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.MOVEPLANET,
                x, y, oldx, oldy,
                name, null, -index, -1,
                0.0, 0.0));
    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
    }

    @Override
    public void showReady() {
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long timeTaken, double avgTech) {
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.TURN,
                0, 0, totalTurns, numAliens,
                null, null, -1, -1,
                0.0, 0.0));
        log.turnsCompleted = totalTurns;
    }

    @Override
    public void showIdleUpdate(int numAliens) {
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
    }

    @Override
    public void showMove(AlienSpec as, double oldX, double oldY, double energyAtNewPosition, double energyAtOldPosition, boolean update, Trajectory t, double time) { //[Q]
        //System.out.println("LOGDEBUG: MOVE" + as.hashCode);
        if (!as.isResident) {
            if (oldX == as.x && oldY == as.y) {
                return;
            }
            if (t != null) {
                Position pf = t.currentFocus.getPositionAtTime(time);
                Position p = new Position(t.getWorldPositionAtTime(time));
                WorldVector v = t.getVelocityAtTime(time);
                
                 log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.MOVE,
                        p.x, p.y, oldX, oldY,
                        null, null, as.hashCode, as.speciesID,
                        0.0, 0.0));
                 log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ORBIT,
                        pf.x, pf.y, (int) (v.x * 10000), (int) (v.y * 10000),
                        Double.toString(t.conic.rotation), null, as.hashCode, as.speciesID,
                        t.conic.e, t.conic.p / Constants.deltaX));
            } else {
                log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.MOVE,
                        as.x, as.y, oldX, oldY,
                        null, null, as.hashCode, as.speciesID,
                        0.0, 0.0));
            }
        }
    }

    @Override
    public void showFight(int x, int y) {
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.FIGHT,
                x, y, 0, 0,
                null, null, -1, -1,
                0.0, 0.0));
    }

    @Override
    public void showSpawn(AlienSpec as, double energyAtPos, Trajectory t) {
        //System.out.println("LOGDEBUG: ADD" + as.hashCode);
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ADD,
                as.x, as.y, 0, 0,
                null, as.getFullSpeciesName(), as.hashCode, as.speciesID,
                0.0, 0.0));

        if (t != null) {
            IntegerPosition p = t.currentFocus.getPositionAtTime(log.turnsCompleted * Constants.deltaT).round();
            log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.ORBIT,
                    p.x, p.y, 0, 0,
                    Double.toString(t.conic.rotation), null, as.hashCode, as.speciesID,
                    t.conic.e, t.conic.p / Constants.deltaX));
        }
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
        //System.out.println("LOGDEBUG: KILL" + as.hashCode);
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.KILL,
                as.x, as.y, 0, 0,
                null, null, as.hashCode, as.speciesID,
                0.0, 0.0));
    }

    @Override
    public void showEngineStateChange(GameState gs) {
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.STATECHANGE,
                0, 0, 0, 0,
                null, null, gs == GameState.Running ? 1 : 0, -1,
                0.0, 0.0));
    }

    @Override
    public void showUpdateAfterRequestedActions() {
    }

    @Override
    public void showGameOver() {
    }

    @Override
    public void debugOut(String s) {
        //System.out.println("LV debug out: "+s);
    }

    @Override
    public void debugErr(String s) {
        System.out.println("LV debug out: " + s);
    }

    @Override
    public void setFilter(String s) {
    }

    @Override
    public void setChatter(boolean f) {
    }

    @Override
    public boolean showContinuePrompt() {
        return true;
    }

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

}
