/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import java.util.ArrayList;
import java.util.Iterator;
import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;

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

    public Trajectory(Orbitable focus, WorldVector r, WorldVector v, SpaceGrid sg) {
        this.sg = sg;
        conic = Conic.newConic(focus, r, v, sg.getTime(), sg);
        currentFocus = focus;
    }

    public Trajectory(Orbitable focus, double p, double e, double signum, double rotation, SpaceGrid sg) {
        this.sg = sg;
        conic = Conic.newConic(focus, p, e, sg.getTime(), signum, rotation, sg);

        currentFocus = focus;

    }

    public double partialHillRadius() {
        return conic.partialHillRadius();
    }

    public boolean isBound() {
        return conic.e < 0;
    }

    public void accelerate(WorldVector deltaV, double t) {
        WorldVector v = getVelocityAtTime(t);
        WorldVector v2 = v.add(deltaV);
        WorldVector r = getWorldPositionAtTime(t);
        Orbitable focus = conic.focus;
        Conic c = Conic.newConic(focus, r, v2, t, sg);
        if (c != null) {
            conic = c;
        }
    }

    public void setFocus(Orbitable focus) {
        WorldVector v = getVelocityAtTime(sg.getTime());
        WorldVector r = getWorldPositionAtTime(sg.getTime());

        Conic c = Conic.newConic(focus, r, v, sg.getTime(), sg);
        if (c != null) {
            conic = c;
            currentFocus = focus;
        }
    }

    public WorldVector getWorldPositionAtTime(double t) {
        if (t != conic.tCurrent) {
            conic.updateStateVectors(t);
        }
        return conic.rCurrent;
    }

    public WorldVector getVelocityAtTime(double t) {
       if (t != conic.tCurrent) {
            conic.updateStateVectors(t);
        }
        return conic.vCurrent;
    }

    @Override
    public Trajectory clone() {
        Trajectory trajectory = new Trajectory();
        trajectory.conic = conic;
        trajectory.sg = sg;
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
            points.add(conic.calculateWorldPositionAtAngle(theta));
        }
        if (conic.e < 1) {
            points.add(conic.calculateWorldPositionAtAngle(dTheta - Math.PI));
        }

        return points.iterator();
    }

    public Position positionOfFocus() {
        return currentFocus.position(sg.getTime());
    }
}
