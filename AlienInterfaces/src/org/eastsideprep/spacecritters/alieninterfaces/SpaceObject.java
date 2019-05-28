/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.alieninterfaces;

/**
 *
 * @author gmein
 */
public class SpaceObject {

    final public String kind;
    final public String name;
    final public Position position;
    final public double mass;
    final public double hillRadius;
    final public WorldVector worldPosition;

    public SpaceObject(String kind, String name, Position position, double mass, double hillRadius) {
        this.name = name;
        this.kind = kind;
        this.position = position;
        this.mass = mass;
        this.hillRadius = hillRadius;
        this.worldPosition = new WorldVector(position);
    }
}
