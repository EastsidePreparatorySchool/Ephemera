/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orbit;

import alieninterfaces.Position;
import alieninterfaces.Vector2;
import gamelogic.InternalSpaceObject;
import gamelogic.SpaceGrid;
import java.util.ArrayList;

/**
 *
 * @author qbowers
 */
public class Trajectory {

    ArrayList<Conic> conics = new ArrayList();

    public Trajectory(Orbitable focus, double p, double e, double mNaught, double rotation, SpaceGrid sg) {
        conics.add(Conic.newConic(focus, p, e, mNaught, sg.getTime(), rotation, sg));

        double dTheta = Math.PI / 200;
        
        double topBound;
        double bottomBound;

        if (conics.get(0) instanceof Hyperbola) {
            topBound = Math.acos(-1f / e) + conics.get(0).rotation;
            bottomBound = conics.get(0).rotation - Math.acos(-1f / e);
        } else {
            topBound = Math.PI;
            bottomBound = dTheta - Math.PI;
        }
    }
    
    public boolean isBound() {
        return conics.get(0).e < 0;
    }
    
    public void accelerate(Vector2 deltaV) {
        
    }

    public Position positionAtTime(double t) {
        Position temp = conics.get(0).positionAtTime(t);
        return temp;
    }
}
