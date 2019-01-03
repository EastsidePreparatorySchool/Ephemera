/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
import org.eastsideprep.spacecritters.gamelogic.Constants;

/**
 *
 * @author gunnar
 */
public class GridVector extends Vector2 {

    public GridVector(double x, double y) {
        super(x, y);
    }

    public GridVector(WorldVector wv) {
        super (wv.x/Constants.deltaX, wv.y/Constants.deltaX);
    }
    
      
  
    public GridVector(Vector2 v) {
        super(v.x,v.y);
    }
    
    public GridVector(Position p) {
        super (p);
    }
}
