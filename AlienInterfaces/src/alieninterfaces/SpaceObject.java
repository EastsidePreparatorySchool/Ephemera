/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class SpaceObject {
    final public String kind;
    final public String name;
    final public Position position;
    
    public SpaceObject(String kind, String name, Position position) {
        this.name = name;
        this.kind = kind;
        this.position = position;
    }
}
