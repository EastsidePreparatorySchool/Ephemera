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
    
    GameLogObserver(GameLog l) {
        log = l;
        maxRead = -1;
        
    }
    
    public GameLogState getInitialState () {
        myState  = log.getNewGameLogState();
        maxRead = myState.getEntryCount();
        return myState;
    }
    
    public ArrayList<GameLogEntry> getNewItems() {
        ArrayList<GameLogEntry> result = log.getNewItems(this);
        return result;
    }
}
