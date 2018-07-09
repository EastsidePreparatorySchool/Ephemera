/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.scgamelog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.gamelog.GameLogObserver;
import org.eastsideprep.spacecritters.gamelog.GameLogState;

/**
 *
 * @author gmein
 */
public class SCGameState implements GameLogState {

    public int entries;
    public int totalTurns;
    private HashMap<Integer, SCGameLogEntry> aliens = new HashMap<>();
    private HashMap<Integer, SCGameLogEntry> planets = new HashMap<>();
    private ArrayList<SCGameLogEntry> stars = new ArrayList<>();
    private ArrayList<SCGameLogEntry> species = new ArrayList<>();

    // what the console uses initially, 
    // and what "copy" uses internally
    public SCGameState(int total, int entries) {
        this.totalTurns = total;
        this.entries = entries;
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

        return sc;
    }

    // the log uses this to compact itself into the state
    @Override
    public void addEntry(GameLogEntry ge) {
        SCGameLogEntry sge = null;
        try {
            sge = (SCGameLogEntry) ge;
        } catch (Exception e) {
            System.err.println("GameState: Invalid log entry added to state");
            return;
        }
        switch (sge.type) {
            case "TURN":
                totalTurns = sge.param1;
                break;
            case "ADD":
                aliens.put(sge.id, sge);
                break;
            case "ADDSTAR":
                stars.add(sge);
                break;
            case "ADDSPECIES":
                species.add(sge);
                break;
            case "ADDPLANET":
                planets.put(sge.id, sge);
                break;
            case "MOVE":
                SCGameLogEntry sgeAdd = aliens.get(sge.id);
                if (sgeAdd == null) {
                    System.err.println(" LOG compact: alien not found for move, id:"+sge.id);
                    break;
                }
                sgeAdd.newX = sge.newX;
                sgeAdd.newY = sge.newY;
                sgeAdd.energy = sge.energy;
                sgeAdd.tech = sge.tech;
                break;
            case "MOVEPLANET":
                SCGameLogEntry sgePlanet = planets.get(sge.id);
                sgePlanet.newX = sge.newX;
                sgePlanet.newY = sge.newY;
                break;
            case "KILL":
                aliens.remove(sge.id);
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

    public static SCGameState safeGetNewState(GameLogObserver obs) {
        try {
            return (SCGameState) obs.getInitialState();
        } catch (Exception e) {
            System.err.println("invalid game state at safegetnewgamestate");
        }
        return null;
    }

    // this is what the server will use to get entries to feed to the client. 
    @Override
    public ArrayList<GameLogEntry> getCompactedEntries() {
        ArrayList<GameLogEntry> result = new ArrayList<>();

        result.addAll(stars);
        result.addAll(planets.values());
        result.addAll(species);
        result.addAll(aliens.values());

        return result;
    }

}
