/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
import org.eastsideprep.spacecritters.gameengineinterfaces.PlanetBehavior;
import org.eastsideprep.spacecritters.orbit.Orbitable;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author guberti
 */
public abstract class InternalSpaceObject implements Orbitable {

    public Position position;
    public WorldVector worldPosition;
    public final String domainName;
    public final String packageName;
    public final String className;
    private String fullName;
    public SpaceGrid grid;
    public boolean isPlanet = false;
    public int index;
    public PlanetBehavior pb;

    public double hillRadius = 0;

    double mass;
    Trajectory trajectory;

    public double energy; // Energy that aliens gain every turn from the planet
    public double tech; // Tech boost for the planet

    public InternalSpaceObject(SpaceGrid grid, Position p, int index, String domainName, String packageName, String className, double energy, double tech, double mass) { //[Q]
        this.position = new Position(p);
        this.worldPosition = new WorldVector(p);
        this.energy = energy;
        this.tech = tech;
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.grid = grid;
        this.index = index;
        this.pb = null;
        this.mass = mass;
    }

    public String getFullName() {
        if (fullName == null) {
            fullName = domainName + ":" + packageName + ":" + className;
        }

        return fullName;

    }

    @Override
    public double mass() {
        return mass;
    }

    @Override
    public Position position(double t) {
        if (trajectory != null) {
            position = new Position (trajectory.getWorldPositionAtTime(t));
        }
        return position;
    }

    @Override
    public WorldVector worldPosition(double t) {
        if (trajectory != null) {
            worldPosition =  trajectory.getWorldPositionAtTime(t);
        }
        return worldPosition;
    }
    @Override
    public WorldVector worldPositionAtAngle(double theta) {
        if (trajectory != null) {
            worldPosition =  trajectory.conic.calculateWorldPositionAtAngle(theta);
        }
        return worldPosition;
    }

    @Override
    public WorldVector velocity(double t) {
        if (trajectory == null) {
            return new WorldVector(0, 0);
        }
        return trajectory.getVelocityAtTime(t);
    }

    public double hillRadius() {
        return hillRadius;
    }
}
