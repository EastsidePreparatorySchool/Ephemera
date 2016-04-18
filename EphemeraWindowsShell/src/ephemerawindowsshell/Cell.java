/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ephemerawindowsshell;

import java.util.HashMap;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
class Cell {

    int alienCount;
    int fightCountDown;
    HashMap<String, CellInfo> speciesMap;
    SpeciesSet speciesSet;  // master set of species
    boolean cellChanged;
    CellInfo color1;
    CellInfo color2;

    public Cell(SpeciesSet s) { // Constructor
        alienCount = 0;
        fightCountDown = 0;
        cellChanged = true;
        this.speciesSet = s;
    }

    public int addSpecies(String speciesName) {
        CellInfo ci;

        if (speciesMap == null) {
            speciesMap = new HashMap<>();
        }

        ci = speciesMap.get(speciesName);

        if (ci == null) {
            ci = new CellInfo(0, Color.BLACK, speciesName);
            speciesMap.put(speciesName, ci);
        }

        ci.count++;
        alienCount++;
        cellChanged = true;

        if (color1 == null) {
            color1 = ci;
        } else if (color2 == null) {
            color2 = ci;
        }

        return ci.count;
    }

    public boolean removeSpecies(String speciesName) {
        CellInfo ci = speciesMap.get(speciesName);
        if (ci != null) {
            if (ci.count > 1) {
                ci.count--;
                speciesMap.put(speciesName, ci);
            } else {
                speciesMap.remove(speciesName);
            }
        }
        alienCount--;
        boolean empty = speciesMap.isEmpty();
        if (empty) {
            speciesMap = null;
        }
        if (alienCount == 0) {
            color1 = null;
            color2 = null;
        } else if (alienCount == 1) {
            color2 = null;
        }

        cellChanged = true;
        return empty;
    }

    public Color getColor(int i) {
        // color for cells is MOSTLY right.
        // when more than one aliens get in or out of the cell, this cache will become stale
        // - but it gets reset when the count goes to 0

        Color color = Color.BLACK;

        if (i == 1) {
            if (color1.color == color.BLACK) {
                // not initialized yet
                color1.color = this.speciesSet.getColor(color1.speciesName);
            }
            color = color1.color;

        } else if (i == 2) {
            if (color2.color == color.BLACK) {
                // not initialized yet
                color2.color = this.speciesSet.getColor(color2.speciesName);
            }
            color = color2.color;
        }

        return color;
    }

    public void fight() {
        fightCountDown = 5; // show this for 5 iterations
        cellChanged = true;
    }

    public boolean isFighting() {
        if (fightCountDown > 0) {
            fightCountDown--;
            if (fightCountDown == 0) {
                cellChanged = true;
                return false;
            }
            return true;
        }

        return false;
    }
}
