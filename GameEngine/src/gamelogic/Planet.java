/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.IntegerPosition;
import alieninterfaces.Position;
import gameengineinterfaces.PlanetBehavior;
import java.util.Iterator;
import java.util.LinkedList;
import orbit.OrbitalElements;
import orbit.Trajectory;

/**
 *
 * @author guberti
 */
public class Planet extends InternalSpaceObject {

    public final String parent;
    public OrbitalElements elements;
    private Trajectory trajectory;
    Iterator<IntegerPosition> gcIterator;
    public Position parentPosition;

    public Planet(SpaceGrid grid, Position parentPosition, OrbitalElements elements, int index, String domainName, String packageName, String className,
            double energy, double tech, String parent, PlanetBehavior pb) {
        super(grid, parentPosition, index, domainName, packageName, className, energy, tech);
        this.parent = parent;
        this.parentPosition = parentPosition;
        this.elements = elements;
        this.isPlanet = true;
        this.pb = pb;
    }

    public void init() { //[Q]
        this.trajectory = new Trajectory(elements);
        this.position = trajectory.positionAtTime(0);
        
        /*// slight random eccentricity
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
        }*/

        // initialize planet behavior
        if (pb != null) {
            pb.init(this);
        }

    }

    public Position move() { //[Q]

        Position pOld = this.position;
        Position pNew = trajectory.positionAtTime(0); //!!!!!!!!!!!!!!!1

        // unplug planet from grid
        this.grid.aliens.unplugPlanet(this);

        // now worry about the aliens at the old and new positions
        AlienCell acsFrom = this.grid.aliens.getAliensAt(pOld.round());
        AlienCell acsTo = this.grid.aliens.getAliensAt(pNew.round());

        // if aliens are where the planet is moving to, and not landed, they die.
        if (acsTo != null) {
            // we have a cell, let's look at aliens
            for (AlienContainer ac : acsTo) {
                // if not landed, and not freshly moved here, you die.
                if (ac.planet != this
                        && ac.nextP.equals(ac.p)) {
                    ac.kill("Death by being in path of planet " + this.className);
                }
            }
        }

        // aliens that were on the planet, move with the planet
        // do this on a cloned list to avoid comodification
        LinkedList<AlienContainer> acsClone = (LinkedList<AlienContainer>) acsFrom.clone();
        for (AlienContainer ac : acsClone) {
            if (ac.planet == this) {
                // they didn't intend to move away, move with planet
                ac.nextP = this.position;
            }
        }

        // update our position
        this.position = pNew;

        // plug planet back into grid
        this.grid.aliens.plugPlanet(this);

        // visualize
        this.grid.vis.showPlanetMove(pOld, pNew, className, this.index, energy, (int) tech);

        return this.position;
    }

    public void reviewInhabitants() {
        if (pb == null) {
            return;
        }

        try {
            // todo: need to make a new list with only the landed aliens
            pb.reviewInhabitants(grid.aliens.getAliensAt(position.round()));
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void reviewInhabitantActions() {
        if (pb == null) {
            return;
        }

        try {
            // todo: make a new list with only the landed aliens
            pb.reviewInhabitantActions(grid.aliens.getAliensAt(position.round()));
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }
}
