/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.Position;
import java.util.LinkedList;

/**
 *
 * @author gmein
 */
public class AlienGrid extends LinkedList<AlienContainer> {

    AlienCell[][] acGrid;
    int centerX;
    int centerY;
    int width;
    int height;

    public AlienGrid(int width, int height) {
        acGrid = new AlienCell[width][height];
        this.width = width;
        this.height = height;
        centerX = width / 2;
        centerY = height / 2;
    }

    private String getXYString(int x, int y) { //[Q]
        return ("(" + x + "," + y + ")");
    }

    public boolean addAlienAndPlug(AlienContainer ac) { //[Q]
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

    public void move(AlienContainer ac, int oldX, int oldY, int newX, int newY) { //[Q]
        AlienCell acs = acGrid[oldX + centerX][oldY + centerY];
        //ac.debugOut("Grid: removing from list " + getXYString(oldX, oldY));
        acs.remove(ac);
        acs.energyPerAlien = 0;
        if (canBeRemoved(acs)) {
            acGrid[oldX + centerX][oldY + centerY] = null;
        }

        acs = acGrid[newX + centerX][newY + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[newX + centerX][newY + centerY] = acs;
        }
        acs.add(ac);
        acs.energyPerAlien = 0;

        //ac.debugOut("Grid: added to list " + getXYString(newX, newY));
    }

    public void unplug(AlienContainer ac) { //[Q]
        // remove alien from grid 
        AlienCell acs = acGrid[ac.x + centerX][ac.y + centerY];
        //ac.debugOut("Grid: removing from list " + getXYString(ac.x, ac.y));
        acs.remove(ac);
        if (canBeRemoved(acs)) {
            acGrid[ac.x + centerX][ac.y + centerY] = null;
        }
    }
    
    public AlienCell getAliensAt(Position p) {
        return getAliensAt (p.x, p.y);
    }

    public AlienCell getAliensAt(int x, int y) {
        return acGrid[x + centerX][y + centerY];
    }

    public boolean isEmptyAt(int x, int y) {
        return acGrid[x + centerX][y + centerY] == null;
    }

    public boolean canBeRemoved(AlienCell acs) {

        return (acs.isEmpty() && acs.star == null && acs.planet == null && acs.energy <= 1);
    }

    public void plugStar(Star st) {
        // add alien to grid 
        AlienCell acs = acGrid[st.position.x + centerX][st.position.y + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[st.position.x + centerX][st.position.y + centerY] = acs;
        }
        acs.star = st;
        acs.energy = st.energy;
    }

    public void plugPlanet(Planet p) { //[Q]
        // add alien to grid 
        AlienCell acs = acGrid[p.position.x + centerX][p.position.y + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[p.position.x + centerX][p.position.y + centerY] = acs;
        }
        acs.planet = p;
        acs.energy += p.energy;
        acs.tech = p.tech;
    }

    public void unplugPlanet(Planet p) { //[Q]
        // add alien to grid 
        AlienCell acs = acGrid[p.position.x + centerX][p.position.y + centerY];
        acs.planet = null;
        acs.energy -= p.energy;
        acs.tech = 0;
    }

    public void distributeStarEnergy(int x, int y, double energy) { //[Q]
        // probe around our current position,
        // tracing an imaginary square of increasing size,
        // starting from the midpoints of the sides
        for (int d = 1; d <= energy; d++) {
            // energy is multiplied by an arbitrary factor 16, but goes down by the square of the distance
            // todo: make this properly depend on our rect metric
            double pointEnergy = ((double) (energy * Constants.starEnergyPerLuminosity) / (double) ((long) d * (long) d));

            if (pointEnergy <= 1) {
                break; // at the level of empty space, get out
            }

            GridCircle g = new GridCircle(x, y, d);
            for (Position point : g) {
                if (point != null) {
                    putEnergyAt(point, pointEnergy);
                }
            }
        }
    }

    void putEnergyAt(Position p, double energy) { //[Q]
        int x = p.x;
        int y = p.y;

        if (((x + centerX) >= width)
                || ((x + centerX) < 0)
                || ((y + centerY) >= height)
                || ((y + centerY) < 0)) {
            return;
        }

        AlienCell acs = acGrid[x + centerX][y + centerY];
        if (acs == null) {
            acs = new AlienCell();
            acGrid[x + centerX][y + centerY] = acs;
        }

        if (energy > acs.energy) {
            acs.energy += energy;
        }
    }

    public double getEnergyAt(Position p) {
        return getEnergyAt(p.x, p.y);
    }

    public double getEnergyAt(int x, int y) { //[Q]
        // off grid?
        if (x + centerX >= width || x + centerX < 0
                || y + centerY >= height || y + centerY < 0) {
            return 0;
        }

        //
        AlienCell acs = acGrid[x + centerX][y + centerY];
        if (acs == null) {
            return 1;
        }
        return acs.energy;
    }

}
