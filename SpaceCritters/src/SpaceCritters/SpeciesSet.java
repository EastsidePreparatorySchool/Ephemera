/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import gameengineinterfaces.AlienSpec;
import java.util.Iterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class SpeciesSet {

    SpaceCritters gameShell;
    ObservableList<AlienSpeciesForDisplay> speciesList;

    SpeciesSet(SpaceCritters gameShellInstance) {
        this.gameShell = gameShellInstance;
        speciesList = FXCollections.observableArrayList();
    }

    public ObservableList<AlienSpeciesForDisplay> getObservableList() {
        return speciesList;
    }

    public void notifyListeners() {
        boolean isOn;
        for (AlienSpeciesForDisplay as : speciesList) {
            int i = speciesList.indexOf(as);
            isOn = as.isOn();
            speciesList.set(i, null);
            speciesList.set(i, as);
            as.setOn(isOn);
        }
    }

    public void addAlien(String speciesName, int id) {
        for (AlienSpeciesForDisplay as : speciesList) {
            if (as.id == id) {
                as.count++;
                return;
            }
        }

        // if we got here, no matching species found
        AlienSpeciesForDisplay as = new AlienSpeciesForDisplay(gameShell, speciesName, id);
        Utilities.runSafe(() -> speciesList.add(as));
        as.setOn(true);

    }

    public void addAlienSpecies(AlienSpec as) {
        AlienSpeciesForDisplay asfd = new AlienSpeciesForDisplay(gameShell, as);
        Utilities.runSafe(() -> speciesList.add(asfd));
        asfd.setOn(true);
    }

    public void removeAlien(String speciesName, int speciesId) {
        Iterator<AlienSpeciesForDisplay> iter = speciesList.iterator();
        while (iter.hasNext()) {
            AlienSpeciesForDisplay as = iter.next();
            if (as.id == speciesId) {
                as.count--;

                // if this is the last one, take it out
                if (as.count == 0) {
                    //Utilities.runSafe(() -> iter.remove()); // for now, leave it in.
                }
                return;
            }
        }

        // if we got here, no matching species found
        // debugErr something
    }

    public void removeAlienSpecies(String speciesName) {
        Iterator<AlienSpeciesForDisplay> iter = speciesList.iterator();
        while (iter.hasNext()) {
            AlienSpeciesForDisplay as = iter.next();
            if (as.speciesName.equals(speciesName)) {
                Utilities.runSafe(() -> iter.remove());
            }
        }
    }

    public Color getColor(String speciesName, int id) {
        for (AlienSpeciesForDisplay as : speciesList) {
            if (as.id == id) {
                return as.color;
            }
        }
        assert false;
        return Color.SILVER;
    }
}
