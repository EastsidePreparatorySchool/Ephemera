/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelog;

import java.util.ArrayList;

/**
 *
 * @author gmein
 */
public class GameLogObserver {

    private final GameLog log;
    private GameLogState myState;
    int maxRead;
    boolean stateServed = false;

    GameLogObserver(GameLog l) {
        log = l;
        maxRead = -1;

    }

    public GameLogState getInitialState() {
        myState = log.getNewGameLogState();
        maxRead = myState.getEntryCount();
        return myState;
    }

    public ArrayList<GameLogEntry> getNewItems() {
        ArrayList<GameLogEntry> result;
        if (stateServed) {
            result = log.getNewItems(this);
            if (result.size() > 0) {
                //System.out.println("Obs " + this.hashCode() + ": Serving incremental state: " + result.size() + " items");
            }
        } else {
            stateServed = true;
            result = myState.getCompactedEntries();
            //System.out.println("Obs " + this.hashCode() + ": Serving initial state: " + result.size() + " items");
        }
        return result;
    }
}
