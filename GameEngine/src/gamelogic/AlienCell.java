/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author gmein
 */
public class AlienCell extends LinkedList<AlienContainer> {
    public ArrayList<String> currentMessages;
    public Planet planet;
    public Star star;
    double energy; // energy gain at this point in space
    double tech; // for planets, this is technology gain rate, otherwise 0
    double energyPerAlien; // multiple in the same spot have to share
    public boolean listening;

    public AlienCell() {
        currentMessages = null;
        star = null;
        planet = null;
        energy = Constants.emptySpaceEnergy;
        tech = 0;
        listening = false;
    }

}

