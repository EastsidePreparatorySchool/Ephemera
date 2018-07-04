/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.IntegerPosition;
import alieninterfaces.Position;
import gameengineinterfaces.PlanetBehavior;
import java.util.LinkedList;
import orbit.Trajectory;

/**
 *
 * @author guberti
 */
public class Planet extends InternalSpaceObject {

    public final String parentString;
    private final InternalSpaceObject parent;
    public Position parentPosition;
    
    private double radius;

    public Planet(SpaceGrid grid, InternalSpaceObject parent, double radius, int index, String domainName, String packageName, String className,
            double energy, double tech, String parentString, PlanetBehavior pb, double mass) {
        super(grid, parent.position, index, domainName, packageName, className, energy, tech, mass);
        this.parentString = parentString;
        this.parentPosition = parent.position;
        this.radius = radius * Constants.deltaX;
        this.isPlanet = true;
        this.pb = pb;
        
        this.parent = parent;
        this.radius = radius;
    }

    public void init() {
        // slight random eccentricity
        this.trajectory = new Trajectory(parent,radius,grid.rand.nextDouble()* 0.5,grid.rand.nextDouble()*Math.PI,grid.rand.nextDouble()*Math.PI,grid);
        position = trajectory.positionAtTime(grid.getTime());
        
        hillRadius = trajectory.partialHillRadius() * Math.pow(mass, 1f/3);
        
        // initialize planet behavior
        if (pb != null) pb.init(this);
    }

    public void move() {
        this.position = trajectory.positionAtTime(grid.getTime());
    }

    public void reviewInhabitants() {
        if (pb == null) return;

        try {
            // todo: need to make a new list with only the landed aliens
            pb.reviewInhabitants(grid.aliens.getAliensAt(position.round()));
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void reviewInhabitantActions() {
        if (pb == null) return;

        try {
            // todo: make a new list with only the landed aliens
            pb.reviewInhabitantActions(grid.aliens.getAliensAt(position.round()));
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }
}
