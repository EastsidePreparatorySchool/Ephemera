/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;


/**
 *
 * @author gunnar
 */
public class WorldVector extends Vector2 {
    public static double deltaX = 5e4;

    public WorldVector(double x, double y) {
        super(x, y);
    }

    public WorldVector(GridVector v) {
        super(v.x * deltaX, v.y * deltaX);
    }

    public WorldVector(Vector2 v) {
        super(v.x, v.y);
    }

    public WorldVector(Vector3 v) {
        super(v.x, v.y);
    }

    public WorldVector(Position p) {
        super(p.x * deltaX, p.y * deltaX);
    }

    public WorldVector(IntegerPosition p) {
        super(p.x * deltaX, p.y * deltaX);
    }

    public WorldVector add(WorldVector v) {
        return new WorldVector(this.x + v.x, this.y + v.y);
    }

    public WorldVector subtract(WorldVector v) {
        return new WorldVector(this.x - v.x, this.y - v.y);
    }

    @Override
    public WorldVector scale(double d) {
        return new WorldVector(this.x * d, this.y * d);
    }
 
    @Override
    public WorldVector unit() {
        return this.scale(1/this.magnitude());
    }
 
    public WorldVector scaleTo(double d) {
        return this.scale(d/this.magnitude());
    }
    
    
    @Override
    public WorldVector rotate(double theta) {
        double x = this.x * Math.cos(theta) - this.y * Math.sin(theta);
        double y = this.x * Math.sin(theta) + this.y * Math.cos(theta);

        return new WorldVector(x, y);
    }


}
