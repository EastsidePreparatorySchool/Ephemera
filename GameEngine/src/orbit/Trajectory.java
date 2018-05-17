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
    public Orbitable currentFocus;
    public SpaceGrid sg;
    
    public Trajectory () {}
    public Trajectory(Orbitable focus, Vector2 r, Vector2 v, SpaceGrid sg) {
        this.sg = sg;
        conics.add(Conic.newConic(focus, r, v, sg.getTime(), sg));
        currentFocus = focus;
    }
    public Trajectory(Orbitable focus, double p, double e, double mNaught, double rotation, SpaceGrid sg) {
        this.sg = sg;
        conics.add(Conic.newConic(focus, p, e, mNaught, sg.getTime(), rotation, sg));
        
        
        currentFocus = focus;
        
        
        
        //useless code for drawing conics
        //unneccessary as of yet
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
    
    public double partialHillRadius() {
        return conics.get(0).partialHillRadius();
    }
    
    public boolean isBound() {
        return conics.get(0).e < 0;
    }
    
    public void accelerate(Vector2 deltaV, double t) {
        Vector2 v = velocityAtTime(t).add(deltaV);
        Vector2 r = positionAtTime(t);
        Orbitable focus = conics.get(0).focus;
        
        
        conics.clear();
        conics.add(Conic.newConic(focus, r, v, t, sg));
    }
    
    

    public Position positionAtTime(double t) {
        return conics.get(0).positionAtTime(t);
    }
    public Vector2 velocityAtTime(double t) {
        return conics.get(0).getVelocityAtTime(t).add(conics.get(0).focus.velocity(t));
    }
    @Override
    public Trajectory clone() {
        Trajectory trajectory = new Trajectory();
        for (int i = 0; i < conics.size(); i++) trajectory.conics.add(conics.get(i).clone());
        
        return trajectory;
    }
}
