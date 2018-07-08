/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.scgamelog;

import org.eastsideprep.spacecritters.gamelog.GameLog;
import org.eastsideprep.spacecritters.gamelog.GameLogEntry;
import org.eastsideprep.spacecritters.gamelog.GameLogObserver;
import org.eastsideprep.spacecritters.gamelog.GameLogState;

/**
 *
 * @author gmein
 */
public class SCGameState implements GameLogState {

    public int totalTurns;
    public int entries;
    public int numAliens;

    // what the console uses initially, 
    // and what "copy" uses internally
    public SCGameState(int total, int entries) {
        this.totalTurns = total;
        this.entries = entries;
    }

    // this is used by the log to hand a new copy to a client
    @Override
    public GameLogState copy() {
        return new SCGameState(totalTurns, entries);
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
        if (sge.type.equals("TURN")) {
            totalTurns++;
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

}
