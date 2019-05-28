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
public class GridVector extends Vector2 {
    

    public GridVector(double x, double y) {
        super(x, y);
    }

    public GridVector(WorldVector wv) {
        super (wv.x/WorldVector.deltaX, wv.y/WorldVector.deltaX);
    }
    
      
  
    public GridVector(Vector2 v) {
        super(v.x,v.y);
    }
    
    public GridVector(Position p) {
        super (p);
    }
}
