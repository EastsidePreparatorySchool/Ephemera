/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;

import org.eastsideprep.spacecritters.gamelogic.Constants;

/**
 *
 * @author gunnar
 */
public class WorldVector extends Vector2 {

    public WorldVector(double x, double y) {
        super(x, y);
    }

    public WorldVector(GridVector v) {
        super(v.x * Constants.deltaX, v.y * Constants.deltaX);
    }

    public WorldVector(Vector2 v) {
        super(v.x, v.y);
    }

    public WorldVector(Vector3 v) {
        super(v.x, v.y);
    }

    public WorldVector(Position p) {
        super(p.x * Constants.deltaX, p.y * Constants.deltaX);
    }

    public WorldVector(IntegerPosition p) {
        super(p.x * Constants.deltaX, p.y * Constants.deltaX);
    }

    public WorldVector add(WorldVector v) {
        return new WorldVector(this.x + v.x, this.y += v.y);
    }
}
