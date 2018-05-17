
package orbit;

import alieninterfaces.Position;
import alieninterfaces.Vector2;

/**
 *
 * @author qbowers
 */


public interface Orbitable {
    public double mass();
    public Position position(double t);
    public Vector2 velocity(double t);
}
