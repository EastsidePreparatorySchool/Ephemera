/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.util.HashMap;
import javafx.scene.paint.Color;

/**
 *
 * @author gmein
 */
class Cell {

    int alienCount;
    int fightCountDown;
    double energy;
    HashMap<String, CellInfo> speciesMap;
    SpeciesSet speciesSet;  // master set of species
    boolean cellChanged;
    CellInfo color1;
    CellInfo color2;
    int row;
    int col;
    int totalFighters;

    public Cell(SpeciesSet s, int row, int col) { // Constructor
        alienCount = 0;
        fightCountDown = 0;
        cellChanged = true;
        this.speciesSet = s;
        this.row = row;
        this.col = col;
    }

    public int addSpecies(String speciesName, double energy) {
        CellInfo ci;

        if (speciesMap == null) {
            speciesMap = new HashMap<>();
        }

        ci = speciesMap.get(speciesName);

        if (ci == null) {
            ci = new CellInfo(0, speciesSet.getColor(speciesName), speciesName);
            speciesMap.put(speciesName, ci);
        }

        ci.count++;
        alienCount++;
        this.energy = energy;
        cellChanged = true;

        if (color1 == null) {
            color1 = ci;
        } else if (color2 == null) {
            color2 = ci;
        }

        return ci.count;
    }

    public boolean removeSpecies(String speciesName, double energy) {
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
        this.energy = energy;
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
        totalFighters = alienCount;
    }

    public int isFighting() {
        if (fightCountDown > 0) {
            fightCountDown--;
            if (fightCountDown == 0) {
                cellChanged = true;
                return 0;
            }
            return fightCountDown;
        }

        return 0;
    }
    
    public int fightCountNoDecrement() {
        return fightCountDown;
    }
}
