package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;

/**
 *
 * @author qbowers
 */


public class DummyMass implements Orbitable {
    public DummyMass() {}

    @Override
    public double mass() {
        return 2000000;
    }

    @Override
    public Position getPositionAtTime(double t) {
        return new Position(0,0);
    }

    @Override
    public WorldVector getWorldPositionAtTime(double t) {
        return new WorldVector(0,0);
    }

    @Override
    public WorldVector getVelocityAtTime(double t) {
        return new WorldVector(0,0);
    }
    
    public double hillRadius() {
        return 0;
    }

    
}
