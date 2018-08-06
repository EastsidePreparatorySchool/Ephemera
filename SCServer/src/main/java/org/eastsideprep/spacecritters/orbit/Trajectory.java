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

    public Trajectory(Orbitable focus, Vector2 r, Vector2 v, SpaceGrid sg) {
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
        Vector2 v = velocityAtTime(t);
        Vector2 v2 = v.add(deltaV);
        Vector2 r = worldPositionAtTime(t);
        Orbitable focus = conic.focus;

        conic = Conic.newConic(focus, r, v2, t, sg);
        Vector2 v3 = velocityAtTime(t);
        if (v.unit().dot(v3.unit()) < 0) {
            System.out.println("Unexplained velocity reversal in accelerate");
        }
    }

    public WorldVector worldPositionAtTime(double t) {
        return conic.worldPositionAtTime(t);
    }

    public WorldVector velocityAtTime(double t) {
        return new WorldVector(conic.getVelocityAtTime(t).add(conic.focus.velocity(t)));
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
            points.add(conic.worldPositionAtAngle(theta));
        }
        if (conic.e < 1) {
            points.add(conic.worldPositionAtAngle(dTheta - Math.PI));
        }

        return points.iterator();
    }

    public WorldVector positionOfFocus() {
        return new WorldVector(currentFocus.position(sg.getTime()));
    }
}
