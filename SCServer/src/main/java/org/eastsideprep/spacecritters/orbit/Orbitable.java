
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
    public Position getPositionAtTime(double t);
    public WorldVector getWorldPositionAtTime(double t);
    public WorldVector getVelocityAtTime(double t);
    public double hillRadius();
}
