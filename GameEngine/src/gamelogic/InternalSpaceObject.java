/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.IntegerPosition;
import alieninterfaces.Position;
import gameengineinterfaces.PlanetBehavior;

/**
 *
 * @author guberti
 */
public abstract class InternalSpaceObject {

    public Position position;
    public final String domainName;
    public final String packageName;
    public final String className;
    private String fullName;
    public SpaceGrid grid;
    public boolean isPlanet = false;
    public int index; 
    public PlanetBehavior pb;
    
    double mass;

    public double energy; // Energy that aliens gain every turn from the planet
    public double tech; // Tech boost for the planet

    public InternalSpaceObject(SpaceGrid grid, Position p, int index, String domainName, String packageName, String className, double energy, double tech, double mass) { //[Q]
        this.position = p;
        this.energy = energy;
        this.tech = tech;
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.grid = grid;
        this.index = index;
        this.pb = null;
        this.mass = mass;
    }

    public String getFullName() {
        if (fullName == null) {
            fullName = domainName + ":" + packageName + ":" + className;
        }

        return fullName;

    }
}
