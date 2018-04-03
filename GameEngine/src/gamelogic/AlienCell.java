/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

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
    public HashMap<AlienSpecies, Integer> speciesMap;

    public AlienCell() {
        currentMessages = null;
        star = null;
        planet = null;
        energy = Constants.emptySpaceEnergy;
        tech = 0;
        listening = false;
        speciesMap = new HashMap<>();
    }

    @Override
    public boolean add(AlienContainer ac) {
        Integer count;
        count = speciesMap.getOrDefault(ac.getAlienSpecies(), 0) + 1;
        speciesMap.put(ac.getAlienSpecies(), count);
        return super.add(ac);

    }

    public void remove(AlienContainer ac) {
        Integer count;
        count = speciesMap.getOrDefault(ac.getAlienSpecies(), 0) - 1;
        speciesMap.put(ac.getAlienSpecies(), count);
        super.remove(ac);
    }

    public LinkedList<AlienSpecies> getAllSpeciesWithPredicateAndPosition(Predicate<AlienSpecies> pred, Position pos) {
        if (this.speciesMap.isEmpty()) {
            return null;
        }
        
        LinkedList<AlienSpecies> las = null;

        for (AlienSpecies as : this.speciesMap.keySet()) {
            if (pred.test(as)) {
                if (las == null) {
                    las = new LinkedList<>();
                }
                las.add(new AlienSpecies(as.domainName, as.packageName, as.className, as.speciesID, pos.x, pos.y));
            }
        }
        return las;
    }

}
