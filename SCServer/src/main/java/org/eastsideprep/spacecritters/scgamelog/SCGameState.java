/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.scgamelog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.eastsideprep.spacecritters.gamelog.GameLog;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.gamelog.GameLogState;

/**
 *
 * @author gmein
 */
public class SCGameState implements GameLogState {

    private GameLog log;
    public int entries;
    public int totalTurns;
    public boolean forUpdates;
    public SCGameLogEntry lastTurn;
    private SCGameLogEntry lastGameState;

    private HashMap<Integer, SCGameLogEntry> aliens = new HashMap<>();
    private HashMap<Integer, SCGameLogEntry> planets = new HashMap<>();
    private HashMap<Integer, SCGameLogEntry> orbits = new HashMap<>();
    private ArrayList<SCGameLogEntry> stars = new ArrayList<>();
    private ArrayList<SCGameLogEntry> species = new ArrayList<>();
    private ArrayList<SCGameLogEntry> kills = new ArrayList<>();
    private ArrayList<SCGameLogEntry> fights = new ArrayList<>();

    // what the console uses initially, 
    // and what "copy" uses internally
    public SCGameState(int total, int entries) {
        this.totalTurns = total;
        this.entries = entries;
    }

    // this is for clients who want to compact a set of log entries after getting them
    public SCGameState(boolean forUpdates) {
        this.totalTurns = 0;
        this.entries = 0;
        this.forUpdates = forUpdates; //purpose of the state, whether to record kills or just drop the alien
    }

    // this is used by the log to hand a new copy to a client
    @Override
    public GameLogState copy() {
        SCGameState sc = new SCGameState(totalTurns, entries);

        // deep-copy aliens, planets, species and stars
        sc.aliens = new HashMap<>();
        for (Entry<Integer, SCGameLogEntry> e : aliens.entrySet()) {
            sc.aliens.put(e.getKey(), new SCGameLogEntry(e.getValue()));
        }
        sc.planets = new HashMap<>();
        for (Entry<Integer, SCGameLogEntry> e : planets.entrySet()) {
            sc.planets.put(e.getKey(), new SCGameLogEntry(e.getValue()));
        }
        sc.stars = new ArrayList<>();
        for (SCGameLogEntry e : stars) {
            sc.stars.add(new SCGameLogEntry(e));
        }
        sc.species = new ArrayList<>();
        for (SCGameLogEntry e : species) {
            sc.species.add(new SCGameLogEntry(e));
        }

        sc.orbits = new HashMap<>();
        for (Entry<Integer, SCGameLogEntry> e : orbits.entrySet()) {
            sc.orbits.put(e.getKey(), new SCGameLogEntry(e.getValue()));
        }
        sc.fights = new ArrayList<>();

        if (lastTurn != null) {
            sc.lastTurn = new SCGameLogEntry(lastTurn);
        }

        if (lastGameState != null) {
            sc.lastGameState = new SCGameLogEntry(lastGameState);
        }
        return sc;
    }

    // the log uses this to compact itself into the state
    @Override
    public void addEntry(GameLogEntry ge) {
        SCGameLogEntry sge = null;
        try {
            // cast and make copy
            sge = (SCGameLogEntry) ge;
        } catch (Exception e) {
            System.err.println("GameState: Invalid log entry added to state");
            return;
        }
        switch (sge.type) {
            case SCGameLogEntry.Type.TURN:
                totalTurns = (int)sge.param1;
                lastTurn = new SCGameLogEntry(sge);
                break;

            case SCGameLogEntry.Type.STATECHANGE:
                lastGameState = new SCGameLogEntry(sge);
                break;

            case SCGameLogEntry.Type.ADD:
                aliens.put(sge.id, new SCGameLogEntry(sge));
                break;
            case SCGameLogEntry.Type.ADDSTAR:
                stars.add(new SCGameLogEntry(sge));
                break;
            case SCGameLogEntry.Type.ADDSPECIES:
                species.add(sge);
                break;
            case SCGameLogEntry.Type.ADDPLANET:
                planets.put(sge.id, new SCGameLogEntry(sge));
                break;
            case SCGameLogEntry.Type.ORBIT:
                orbits.put(sge.id, new SCGameLogEntry(sge));
                break;
            case SCGameLogEntry.Type.MOVE:
                SCGameLogEntry sgePrior = aliens.get(sge.id);
                if (sgePrior == null) {
                    // we did not find an old record,
                    // so we will make one on the fly
                    sgePrior = new SCGameLogEntry(SCGameLogEntry.Type.MOVE,
                            sge.newX, sge.newY, sge.param1, sge.param2,
                            null, sge.speciesName, sge.id, sge.speciesId,
                            0.0, 0.0);
                    aliens.put(sge.id, sgePrior);
                    break;
                }

                // if we get here, a prior record was found
                if (sgePrior.type == SCGameLogEntry.Type.ADD) {
                    // it was an ADD record
                    // we just ADD at the new position
                    // and copy the rest
                    sgePrior.newX = sge.newX;
                    sgePrior.newY = sge.newY;
                    sgePrior.energy = sge.energy;
                    sgePrior.tech = sge.tech;
                } else if (sgePrior.type == SCGameLogEntry.Type.MOVE) {
                    // it was a MOVE record
                    // we just update the new position
                    // and copy the rest
                    sgePrior.newX = sge.newX;
                    sgePrior.newY = sge.newY;
                }
                break;

            case SCGameLogEntry.Type.MOVEPLANET:
                SCGameLogEntry sgePlanet = planets.get(sge.id);
                if (sgePlanet == null) {
                    // no prior record found, file a new one
                    sgePlanet = new SCGameLogEntry(SCGameLogEntry.Type.MOVEPLANET,
                            sge.newX, sge.newY, sge.param1, sge.param2,
                            null, null, sge.id, -1,
                            0.0, 0.0);
                    planets.put(sge.id, sgePlanet);
                    break;
                }
                // there was an old record, update the position
                sgePlanet.newX = sge.newX;
                sgePlanet.newY = sge.newY;
                break;

            case SCGameLogEntry.Type.KILL:
                if (forUpdates) {
                    // in updates, we really need to show the kill action
                    // unless it just kills an alien born in the same update
                    SCGameLogEntry sgeDead = aliens.get(sge.id);
                    if (sgeDead == null) {
                        // this alien had not been mentioned in this update before
                        // so put a copy of the kill record in there
                        // the client will presumably know about this id
                        sgeDead = new SCGameLogEntry(sge);
                        kills.add(sgeDead);
                        break;
                    }
                    if (sgeDead.type == SCGameLogEntry.Type.ADD) {
                        // there was a prior ADD record
                        // let's just nix the guy
                        aliens.remove(sge.id);
                    } else if (sgeDead.type == SCGameLogEntry.Type.MOVE) {
                        // if there was a prior MOVE record,
                        // we need to display the move before the kill, 
                        // so just add the kill.
                        sgeDead = new SCGameLogEntry(sge);
                        kills.add(sgeDead);
                    }
                } else {
                    // in initial states, we just get rid of the guy altogether
                    aliens.remove(sge.id);
                }
                break;
           case SCGameLogEntry.Type.FIGHT:
                if (forUpdates) {
                        fights.add(sge);
                } 
                break;
            default:
                break;
        }
        entries++;
    }

    // log needs this
    @Override
    public int getEntryCount() {
        return entries;
    }

//    public static SCGameState safeGetNewState(GameLogObserver obs) {
//        try {
//            return (SCGameState) obs.getInitialState();
//        } catch (Exception e) {
//            System.err.println("invalid game state at safegetnewgamestate");
//        }
//        return null;
//    }
    // this is what the server will use to get entries to feed to the client. 
    @Override
    public ArrayList<GameLogEntry> getCompactedEntries() {
        ArrayList<GameLogEntry> result = new ArrayList<>();

        result.addAll(stars);
        result.addAll(planets.values());
        result.addAll(species);
        result.addAll(aliens.values());
        if (fights.size() > 100) {
            fights.subList(0, fights.size()-100-1).clear();
        }
        result.addAll(fights);
        result.addAll(kills);
        result.addAll(orbits.values());
        
        if (lastTurn != null) {
            result.add(lastTurn);
        }
        if (lastGameState != null) {
            result.add(lastGameState);
        }
        return result;
    }

    @Override
    public void onDeath() {
        // just tell the log that the game is paused, 
        // to put clients into idle mode
        log.addLogEntry(new SCGameLogEntry(SCGameLogEntry.Type.STATECHANGE,
                0, 0, 0, 0,
                null, null, 0 /* paused */, -1,
                0.0, 0.0));
    }

    @Override
    public void setLog(GameLog log) {
        this.log = log;
    }

}
