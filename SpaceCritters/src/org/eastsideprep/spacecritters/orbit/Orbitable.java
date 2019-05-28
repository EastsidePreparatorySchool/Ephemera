
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;

/**
 *
 * @author qbowers
 */


public interface Orbitable {
    public double mass();
    public Position position(double t);
    public WorldVector worldPosition(double t);
    public WorldVector worldPositionAtAngle(double theta);
    public WorldVector velocity(double t);
    public double hillRadius();
}
