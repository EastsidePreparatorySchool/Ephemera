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
import java.util.Iterator;

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
        
        trajectory.currentFocus = currentFocus;
        return trajectory;
    }   
    
    
    
    
    
    
    public Iterator toDraw(int segments) {
        double dTheta = 2 * Math.PI / segments;
        double topBound;
        double bottomBound;
        
        Conic conic = conics.get(0);
        if (conic instanceof Hyperbola) {
            topBound = Math.acos(-1f / conic.e) + conic.rotation;
            bottomBound = conic.rotation - Math.acos(-1f / conic.e);
        } else {
            topBound = Math.PI;
            bottomBound = dTheta - Math.PI;
        }
        
        ArrayList<Vector2> points = new ArrayList<>();
        for (double theta = bottomBound; theta < topBound; theta += dTheta) {
            points.add(conic.positionAtAngle(theta));
        }
        if (conic.e < 1) points.add(conics.get(0).positionAtAngle(dTheta - Math.PI));
        
        
        return points.iterator();
    }
    
    public Vector2 positionOfFocus() {
        return currentFocus.position(sg.getTime());
    }
    
    public boolean wasWithinRadius(double r) {
        Conic conic = conics.get(0);
        double theta0 = conic.theta0;
        double theta1 = conic.theta1;
        if ( conic.periapse() > r ) return false;
        if ( conic.apoapse() <= r ) return true;
        
        if ( conic.radiusAtAngle(theta1) <= r ) return true;
        
        double theta = conic.angleAtRadius(r);
        if (theta0 > theta) theta = 2*Math.PI - theta;
        
        return theta1 < theta0 || theta1 > theta;
    }
}
