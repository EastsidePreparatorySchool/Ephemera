package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;

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
    public Position position(double t) {
        return new Position(0,0);
    }

    @Override
    public Vector2 velocity(double t) {
        return new Vector2(0,0);
    }
    
    public double hillRadius() {
        return 0;
    }
    
}
