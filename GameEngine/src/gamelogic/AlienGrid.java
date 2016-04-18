/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.util.LinkedList;

/**
 *
 * @author gmein
 */
public class AlienGrid extends LinkedList<AlienContainer> {

    AlienCell[][] acGrid;
    int centerX;
    int centerY;

    public AlienGrid(int width, int height) {
        acGrid = new AlienCell[width][height];
        centerX = width / 2;
        centerY = height / 2;
    }

    private String getXYString(int x, int y) {
        return ("(" + x +"," + y+")");
    }
    public boolean addAlienAndPlug(AlienContainer ac) {
        // add alien to grid as well as to master list
        AlienCell acs = acGrid[ac.x + centerX][ac.y + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[ac.x + centerX][ac.y + centerY] = acs;
        }
        acs.add(ac);
        //ac.debugOut("Grid: added to list " + getXYString(ac.x, ac.y));
        return super.add(ac);
    }

    public AlienContainer removeAlienAndUnplug(AlienContainer ac) {
        // remove alien from grid as well as from master list
        super.remove(ac);
        unplug(ac);
        return ac;
    }

    public void move(AlienContainer ac, int oldX, int oldY, int newX, int newY) {
        //if (oldX == newX && oldY == newY) {
        //    ac.debugOut("Grid: standing still, shouldn't be here");
        //}

        AlienCell acs = acGrid[oldX + centerX][oldY + centerY];
        //ac.debugOut("Grid: removing from list " + getXYString(oldX, oldY));
        acs.remove(ac);
        if (acs.isEmpty()) {
            acGrid[oldX + centerX][oldY + centerY] = null;
        }

        acs = acGrid[newX + centerX][newY + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[newX + centerX][newY + centerY] = acs;
        }
        acs.add(ac);
        //ac.debugOut("Grid: added to list " + getXYString(newX, newY));
    }

    public void unplug(AlienContainer ac) {
        // remove alien from grid as well as from master list
        LinkedList<AlienContainer> acs = acGrid[ac.x + centerX][ac.y + centerY];
        //ac.debugOut("Grid: removing from list " + getXYString(ac.x, ac.y));
        acs.remove(ac);
        if (acs.isEmpty()) {
            acGrid[ac.x + centerX][ac.y + centerY] = null;
        }
    }

    public AlienCell getAliensAt(int x, int y) {
        return acGrid[x + centerX][y + centerY];
    }

    public boolean isEmptyAt(int x, int y) {
        return acGrid[x + centerX][y + centerY] == null;
    }
}
