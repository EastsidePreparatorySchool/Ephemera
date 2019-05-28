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
public interface GameLogState {

    GameLogState copy();

    void addEntry(GameLogEntry ge);

    int getEntryCount();

    ArrayList<GameLogEntry> getCompactedEntries();

    void onDeath();

    void setLog(GameLog log);
}
