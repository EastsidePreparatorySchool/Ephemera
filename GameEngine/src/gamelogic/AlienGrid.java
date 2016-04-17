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

    LinkedList<AlienContainer>[][] acGrid;
    int centerX;
    int centerY;

    public AlienGrid(int width, int height) {
        acGrid = new LinkedList[width][height];
        centerX = width / 2;
        centerY = height / 2;
    }

    public boolean addAlienAndPlug(AlienContainer ac) {
        // add alien to grid as well as to master list
        LinkedList<AlienContainer> acs = acGrid[ac.x + centerX][ac.y + centerY];
        if (acs == null) {
            acs = new LinkedList();
            acGrid[ac.x + centerX][ac.y + centerY] = acs;
        }
        acs.add(ac);
        //System.out.println("added "+ ac.hashCode() + " to list " + acs.hashCode());
        return super.add(ac);
    }

    public AlienContainer removeAlienAndUnplug(AlienContainer ac) {
        // remove alien from grid as well as from master list
        super.remove(ac);
        unplug(ac);
        return ac;
    }
    
    

    public void move(AlienContainer ac, int oldX, int oldY, int newX, int newY) {
        LinkedList<AlienContainer> acs = acGrid[oldX + centerX][oldY + centerY];
        acs.remove(ac);
        //System.out.println("removing "+ ac.hashCode() + " from list " + acs.hashCode());
        if (acs.isEmpty()) {
            acGrid[oldX + centerX][oldY + centerY] = null;
        }

        acs = acGrid[newX + centerX][newY + centerY];
        if (acs == null) {
            acs = new LinkedList();
            acGrid[newX + centerX][newY + centerY] = acs;
        }
        acs.add(ac);
        //System.out.println("added "+ ac.hashCode() + " to list " + acs.hashCode());
    }

    public void unplug(AlienContainer ac) {
        // remove alien from grid as well as from master list
        LinkedList<AlienContainer> acs = acGrid[ac.x + centerX][ac.y + centerY];
        //System.out.println("removing "+ ac.hashCode() + " from list " + acs.hashCode());
        acs.remove(ac);
        if (acs.isEmpty()) {
            acGrid[ac.x + centerX][ac.y + centerY] = null;
        }
    }

    public LinkedList<AlienContainer> getAliensAt(int x, int y) {
        return acGrid[x + centerX][y + centerY];
    }

    public boolean isEmptyAt(int x, int y) {
        return acGrid[x + centerX][y + centerY] == null;
    }
}
