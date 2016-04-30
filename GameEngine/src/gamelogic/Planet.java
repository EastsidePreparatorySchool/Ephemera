/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.Position;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author guberti
 */
public class Planet extends InternalSpaceObject {

    final String parent;
    public int radius;
    private GridCircle gc;
    Iterator<Position> gcIterator;
    int orbitalVelocityCounter;
    Position parentPosition;

    public Planet(SpaceGrid grid, int parentx, int parenty, int radius, String domainName, String packageName, String className,
            double energy, double tech, String parent) {
        super(grid, parentx, parenty, domainName, packageName, className, energy, tech);
        this.parent = parent;
        this.parentPosition = new Position(parentx, parenty);
        this.radius = radius;
        this.isPlanet = true;
    }

    public void startOrbit() {
        gc = new GridCircle(position.x, position.y, radius);
        gcIterator = gc.iterator();
        this.orbitalVelocityCounter = radius;
        
        // randomize orbital position
        for (int randomShift = SpaceGrid.rand.nextInt(4 * radius); randomShift > 0; randomShift--) {
            this.position = gcIterator.next();
        }
        
        // if we somehow exhausted the circel (shouldn't happen, but you know)
        // create another iterator
        
        if (!gcIterator.hasNext()) {
            gcIterator = gc.iterator();
        }

    }

    public Position move() {
        --orbitalVelocityCounter;
        if (orbitalVelocityCounter == 0) {
            orbitalVelocityCounter = radius;

            if (!gcIterator.hasNext()) {
                gcIterator = gc.iterator();
            }

            Position pOld = this.position;
            Position pNew = gcIterator.next();

            // unplug planet from grid
            this.grid.aliens.unplugPlanet(this);

            // now worry about the aliens at the old and new positions
            AlienCell acsFrom = this.grid.aliens.getAliensAt(pOld);
            if (acsFrom == null) {
                boolean bullshit = true;
            }
            AlienCell acsTo = this.grid.aliens.getAliensAt(pNew);

            // if aliens are where the planet is moving to, they die.
            if (acsTo != null) {
                // we have a cell, let's look at aliens
                for (AlienContainer ac : acsTo) {
                    ac.kill("Death by parking in path of planet " + this.className);
                }
            }

            // aliens that were on the planet, move with the planet
            // do this on a cloned list to avoid comodification
            LinkedList<AlienContainer> acsClone = (LinkedList<AlienContainer>) acsFrom.clone();
            for (AlienContainer ac : acsClone) {
                grid.aliens.move(ac, this.position.x, this.position.y, pNew.x, pNew.y);
            }

            // update our position
            this.position = pNew;

            // plug planet back into grid
            this.grid.aliens.plugPlanet(this);

            // visualize
            this.grid.vis.showPlanetMove(pOld.x, pOld.y, pNew.x, pNew.y, className, energy, tech);
        }
        return this.position;
    }
}
