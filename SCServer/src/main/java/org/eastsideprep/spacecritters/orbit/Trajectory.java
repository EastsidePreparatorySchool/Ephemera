/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author qbowers
 */
public class Trajectory {

    public Conic conic;
    public Orbitable currentFocus;
    public SpaceGrid sg;

    public Trajectory() {
    }

    public Trajectory(Orbitable focus, Vector2 r, Vector2 v, SpaceGrid sg) {
        this.sg = sg;
        conic = Conic.newConic(focus, r, v, sg.getTime(), sg);
        currentFocus = focus;
    }
    public Trajectory(Orbitable focus, double p, double e, double mNaught, double rotation, SpaceGrid sg) {
        this.sg = sg;
        conic = Conic.newConic(focus, p, e, mNaught, sg.getTime(), rotation, sg);

        currentFocus = focus;

    }

    public double partialHillRadius() {
        return conic.partialHillRadius();
    }

    public boolean isBound() {
        return conic.e < 0;
    }

    public void accelerate(Vector2 deltaV, double t) {
        Vector2 v = velocityAtTime(t).add(deltaV);
        Vector2 r = positionAtTime(t);
        Orbitable focus = conic.focus;

        conic = Conic.newConic(focus, r, v, t, sg);
    }

    public Position positionAtTime(double t) {
        return conic.positionAtTime(t);
    }
    public Vector2 velocityAtTime(double t) {
        return conic.getVelocityAtTime(t).add(conic.focus.velocity(t));
    }
    @Override
    public Trajectory clone() {
        Trajectory trajectory = new Trajectory();
        trajectory.conic = conic;

        trajectory.currentFocus = currentFocus;
        return trajectory;
    }

    public Iterator<Vector2> toDraw(int segments) {
        double dTheta = 2 * Math.PI / segments;
        double topBound;
        double bottomBound;

        Conic conic = this.conic;
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
        if (conic.e < 1) {
            points.add(conic.positionAtAngle(dTheta - Math.PI));
        }

        return points.iterator();
    }

    public Vector2 positionOfFocus() {
        return currentFocus.position(sg.getTime());
    }
    
    public boolean wasWithinRadius(double r) {
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
