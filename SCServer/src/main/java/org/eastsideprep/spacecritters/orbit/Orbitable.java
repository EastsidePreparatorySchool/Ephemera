
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;

/**
 *
 * @author qbowers
 */


public interface Orbitable {
    public double mass();
    public Position position(double t);
    public Vector2 velocity(double t);
    public double hillRadius();
}
