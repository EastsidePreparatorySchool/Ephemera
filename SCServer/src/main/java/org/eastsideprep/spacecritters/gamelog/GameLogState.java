/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelog;

/**
 *
 * @author gmein
 */

public interface GameLogState {
    GameLogState clone ();
    void addEntry(GameLogEntry ge);
    int getEntryCount();
}
