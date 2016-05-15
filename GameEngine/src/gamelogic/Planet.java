/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.Position;
import gameengineinterfaces.PlanetBehavior;
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

    public Planet(SpaceGrid grid, int parentx, int parenty, int radius, int index, String domainName, String packageName, String className,
            double energy, double tech, String parent, PlanetBehavior pb) {
        super(grid, parentx, parenty, index, domainName, packageName, className, energy, tech);
        this.parent = parent;
        this.parentPosition = new Position(parentx, parenty);
        this.radius = radius;
        this.isPlanet = true;
        this.pb = pb;
    }

    public void startOrbit() {
        // slight random eccentricity
        position.x += grid.rand.nextInt(3) - 1;
        position.y += grid.rand.nextInt(3) - 1;

        // make the orbit
        gc = new GridCircle(position.x, position.y, radius);
        gcIterator = gc.iterator();
        this.orbitalVelocityCounter = radius;

        this.position = gcIterator.next();

        // randomize position in orbit
        for (int randomShift = grid.rand.nextInt(4 * radius); randomShift > 0; randomShift--) {
            this.position = gcIterator.next();
        }

        // if we somehow exhausted the circle (shouldn't happen, but you know)
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
            AlienCell acsTo = this.grid.aliens.getAliensAt(pNew);

            // if aliens are where the planet is moving to, and not moving, they die.
            if (acsTo != null) {
                // we have a cell, let's look at aliens
                for (AlienContainer ac : acsTo) {
                    if (ac.nextX == ac.x && ac.nextY == ac.y) {
                        ac.kill("Death by parking in path of planet " + this.className);
                    }
                }
            }

            // aliens that were on the planet, move with the planet
            // do this on a cloned list to avoid comodification
            LinkedList<AlienContainer> acsClone = (LinkedList<AlienContainer>) acsFrom.clone();
            for (AlienContainer ac : acsClone) {
                if (ac.nextX == ac.x && ac.nextY == ac.y) {
                    // they didn't intend to move away, move with planet
                    ac.nextX = this.position.x;
                    ac.nextY = this.position.y;
                }
            }

            // update our position
            this.position = pNew;

            // plug planet back into grid
            this.grid.aliens.plugPlanet(this);

            // visualize
            this.grid.vis.showPlanetMove(pOld.x, pOld.y, pNew.x, pNew.y, className, this.index, energy, (int) tech);
        }
        return this.position;
    }

    public void communicateWithAliens() {

        if (pb == null) {
            return;
        }

        try {
            pb.communicateWithAliens();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }

    public void receive() {
        if (pb == null) {
            return;
        }

        try {
            pb.receive();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void reviewInhabitants() {
        if (pb == null) {
            return;
        }

        try {
            pb.reviewInhabitants();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void reviewInhabitantActions() {
        if (pb == null) {
            return;
        }

        try {
            pb.reviewInhabitantActions();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }
}
