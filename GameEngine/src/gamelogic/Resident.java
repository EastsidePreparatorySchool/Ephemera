package gamelogic;

import gameengineinterfaces.GameVisualizer;

/**
 *
 * @author guberti
 */
public class Resident extends InternalSpaceObject {
    // Residents are stored in an array held by the InternalSpaceObject they inhabit
    // Nothing else about residents is known, so no constructors/variables
    // are defined here yet.
    String parent;
    
    public Resident (GameVisualizer vis, int x, int y, String packageName, String className, double energy, double tech, String parent) {
        super(vis, x, y, packageName, className, energy, tech);
        this.parent = parent;
    }
}
