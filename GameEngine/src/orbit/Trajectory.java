package orbit;

import alieninterfaces.Position;

/**
 *
 * @author qbowers
 */


public class Trajectory {
    public Trajectory(OrbitalElements elements) {}
    
    public Position positionAtTime(double t) {
        return new Position(0,0);
    }
}
