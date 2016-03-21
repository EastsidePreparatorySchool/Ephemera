package gamelogic;

import gameengineinterfaces.GameVisualizer;

/**
 *
 * @author guberti
 */
public class Resident extends SpaceObject {
    // Residents are stored in an array held by the SpaceObject they inhabit
    // Nothing else about residents is known, so no constructors/variables
    // are defined here yet.
    String parent;
    
    public Resident (GameVisualizer vis, int x, int y, String packageName, String className, int energy, int tech, String parent) {
        super(vis, x, y, packageName, className, energy, tech);
        this.parent = parent;
    }
}
