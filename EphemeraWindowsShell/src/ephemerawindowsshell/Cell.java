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
    boolean cellChanged;
    Color color1;
    Color color2;

    public Cell() { // Constructor
        alienCount = 0;
        fightCountDown = 0;
        cellChanged = true;
    }

    public int addSpecies(String packageName, String className, Color color) {
        int count;
        if (speciesMap == null) {
            speciesMap = new HashMap<>();
        }
        CellInfo ci = speciesMap.get(packageName + ":" + className);
        if (ci == null) {
            ci = new CellInfo(0, color);
        }
        ci.count++;
        speciesMap.put(packageName + ":" + className, ci);
        alienCount++;
        cellChanged = true;
        
        if (color1 == null) {
            color1 = color;
        } else if (color2 == null) {
            color2 = color;
        }
        
        return ci.count;
    }

    public boolean removeSpecies(String packageName, String className) {
        CellInfo ci = speciesMap.get(packageName + ":" + className);
        if (ci != null) {
            if (ci.count > 1) {
                ci.count--;
                speciesMap.put(packageName + ":" + className, ci);
            } else {
                speciesMap.remove(packageName + ":" + className);
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

        Color color = Color.BLACK;

        if (i == 1) {
            color = color1;
        } else if (i == 2) {
            color = color2;
        }

        return color;
    }

    public void fight() {
        fightCountDown = 5; // show this for 5 iterations
        cellChanged = true;
    }

    public boolean isFighting() {
        if (fightCountDown > 0) {
            --fightCountDown;
            return true;
        }

        cellChanged = true;
        return false;
    }
}
