/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemerawindowsshell;

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

    public void notifyListeners(AlienSpecies as) {
        int i = speciesList.indexOf(as);
        speciesList.set(i, null);
        speciesList.set(i, as);
    }

    public void addAlien(String packageName, String className) {
        for (AlienSpecies as : speciesList) {
            if (as.packageName.equals(packageName) && as.className.equals(className)) {
                as.count++;
                notifyListeners(as);
                return;
            }
        }

        // if we got here, no matching species found
        speciesList.add(new AlienSpecies(packageName, className));
    }

    public void removeAlien(String packageName, String className) {
        for (AlienSpecies as : speciesList) {
            if (as.packageName.equals(packageName) && as.className.equals(className)) {
                as.count--;
                notifyListeners(as);

                // if this is the last one, take it out
                if (as.count == 0) {
                    speciesList.remove(as);
                }
                return;
            }
        }

        // if we got here, no matching species found
        // debugErr something
    }
    
    
    public Color getColor(String packageName, String className) {
        for (AlienSpecies as : speciesList) {
            if (as.packageName.equals(packageName) && as.className.equals(className)) {
                return as.color;
            }
        }
        
        return Color.PINK;
    }


}
