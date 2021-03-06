/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.gameengineinterfaces.PlanetBehavior;
import org.eastsideprep.spacecritters.orbit.Trajectory;

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
        this.trajectory = new Trajectory(parent,
                radius * Constants.deltaX, // p
                grid.rand.nextDouble()* 0.5, // e
                1.0,    // signum
                grid.rand.nextDouble()*Math.PI, //rotation
                grid);
        worldPosition = trajectory.getWorldPositionAtTime(grid.getTime());
        position = new Position(worldPosition);
        
        hillRadius = trajectory.partialHillRadius() * Math.pow(mass, 1f/3);
        
        // initialize planet behavior
        if (pb != null) pb.init(this);
    }

    public void move() {
        this.worldPosition = trajectory.getWorldPositionAtTime(grid.getTime());
        this.position = new Position (worldPosition);
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
