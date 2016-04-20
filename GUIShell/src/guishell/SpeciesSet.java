/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
public class SpeciesSet {

    ObservableList<AlienSpecies> speciesList;

    SpeciesSet() {
        speciesList = FXCollections.observableArrayList();
    }

    public ObservableList<AlienSpecies> getObservableList() {
        return speciesList;
    }

    public void notifyListeners() {
        for (AlienSpecies as:speciesList) {
            int i = speciesList.indexOf(as);
            speciesList.set(i, null);
            speciesList.set(i, as);
        }
    }

    public void addAlien(String speciesName) {
        for (AlienSpecies as : speciesList) {
            if (as.speciesName.equalsIgnoreCase(speciesName)) {
                as.count++;
                return;
            }
        }

        // if we got here, no matching species found
        AlienSpecies as = new AlienSpecies(speciesName);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                speciesList.add(as);
            }
        });
    }

    public void removeAlien(String speciesName) {
        for (AlienSpecies as : speciesList) {
            if (as.speciesName.equals(speciesName)) {
                as.count--;

                // if this is the last one, take it out
                if (as.count == 0) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            speciesList.remove(as);
                        }
                    });

                }
                return;
            }
        }

        // if we got here, no matching species found
        // debugErr something
    }

    public Color getColor(String speciesName) {
        for (AlienSpecies as : speciesList) {
            if (as.speciesName.equals(speciesName)) {
                return as.color;
            }
        }

        return Color.SILVER;
    }

}
