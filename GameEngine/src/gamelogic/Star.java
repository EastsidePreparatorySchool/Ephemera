/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.Position;

/**
 *
 * @author guberti
 */
public class Star extends InternalSpaceObject{
    
    public Star (SpaceGrid grid, Position p, int index, String domainName, String packageName, String className, double energy, double tech) { //[Q]
        super(grid, p, index, domainName, packageName, className, energy, tech);
    }
    
    // Add code here to detail the working of stars
}
