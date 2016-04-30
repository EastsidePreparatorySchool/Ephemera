package gamelogic;

/**
 *
 * @author guberti
 */
public class Resident extends InternalSpaceObject {
    // Residents are stored in an array held by the InternalSpaceObject they inhabit
    // Nothing else about residents is known, so no constructors/variables
    // are defined here yet.
    String parent;
    
    public Resident (SpaceGrid grid, int x, int y, String domainName, String packageName, String className, double energy, double tech, String parent) {
        super(grid, x, y, domainName, packageName, className, energy, tech);
        this.parent = parent;
    }
}
