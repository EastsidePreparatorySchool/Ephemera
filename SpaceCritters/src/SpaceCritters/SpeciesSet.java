/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import gameengineinterfaces.AlienSpec;
import java.util.Iterator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class SpeciesSet {

    ObservableList<AlienSpeciesForDisplay> speciesList;

    SpeciesSet() {
        speciesList = FXCollections.observableArrayList();
    }

    public ObservableList<AlienSpeciesForDisplay> getObservableList() {
        return speciesList;
    }

    public void notifyListeners() {
        boolean isOn;
        for (AlienSpeciesForDisplay as : speciesList) {
            int i = speciesList.indexOf(as);
            speciesList.set(i, null);
            speciesList.set(i, as);
        }
    }

    public void addAlien(String speciesName) {
        for (AlienSpeciesForDisplay as : speciesList) {
            if (as.speciesName.equalsIgnoreCase(speciesName)) {
                as.count++;
                return;
            }
        }

        // if we got here, no matching species found
        AlienSpeciesForDisplay as = new AlienSpeciesForDisplay(speciesName);
        Utilities.runSafe(() -> speciesList.add(as));
        as.setOn(true);

    }

    public void addAlienSpecies(AlienSpec as) {
        AlienSpeciesForDisplay asfd = new AlienSpeciesForDisplay(as);
        Utilities.runSafe(() -> speciesList.add(asfd));
        asfd.setOn(true);
    }

    public void removeAlien(String speciesName) {
        Iterator<AlienSpeciesForDisplay> iter = speciesList.iterator();
        while (iter.hasNext()) {
            AlienSpeciesForDisplay as = iter.next();
            if (as.speciesName.equals(speciesName)) {
                as.count--;

                // if this is the last one, take it out
                if (as.count == 0) {
                    Utilities.runSafe(() -> iter.remove());
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

    public Color getColor(String speciesName) {
        for (AlienSpeciesForDisplay as : speciesList) {
            if (as.speciesName.equals(speciesName)) {
                return as.color;
            }
        }

        return Color.SILVER;
    }

}
