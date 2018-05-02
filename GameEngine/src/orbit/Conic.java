/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orbit;

import alieninterfaces.Position;
import alieninterfaces.Vector2;
import gamelogic.Constants;
import gamelogic.SpaceGrid;

/**
 *
 * @author qbowers
 */
class OrbitException extends RuntimeException {

    OrbitException(String s) {
        super(s);
    }
}

public abstract class Conic {

    double p;
    double e;
    double h;

    double rotation;

    int orbits;

    Orbitable focus;
    double mu;
    double M0;
    double n;

    SpaceGrid sg;

    public static Conic newConic(Orbitable focus, double p, double e, double theta, double tNaught, double rotation, SpaceGrid sg) {
        p *= Constants.deltaX;
        if (e == 0) {
            return new Circle(focus, p, e, theta, tNaught, rotation, sg);
        }
        if (e < 1) {
            return new Ellipse(focus, p, e, theta, tNaught, rotation, sg);
        }
        if (e == 1) {
            return new Parabola(focus, p, e, theta, tNaught, rotation, sg);
        }
        if (e > 1) {
            return new Hyperbola(focus, p, e, theta, tNaught, rotation, sg);
        }

        throw new OrbitException("Invalid Eccentricity: " + e);
    }

    Conic(Orbitable focus, double p, double e, double theta, double tNaught, double rotation, SpaceGrid sg) {
        this.p = p;
        this.e = e;
        this.rotation = rotation;

        this.focus = focus;
        this.mu = Constants.G * focus.mass();

        this.sg = sg;

        this.h = Math.sqrt(p * mu);

        //p = h^2 / mu              semi-latus rectum
        //r = p / (1 + ||e|| cos theta)
        //h = specific angular momentum = r (position vector) cross rdot (velocity vector)
        //||h|| = ||r|| * ||(component of velocity perpendicular to the relative position vector)||
        //a = -(mu / ||r||^3)r
        //r / ||r|| + e = (velocity cross h) / mu
        //e = C/mu
        //||r|| + r dot e (a vector (somehow))) = h^2 / mu
        //e is a vector with the magnitude of the eccentricity, which points towards periapse
        //||r|| + ||r||||e||cos theta = h^2 / mu
        //r_p (radius at periapse) = (h^2 / mu) / (1 + ||e|| cos theta)
    }

    public abstract double angleAtTime(double t);

    public abstract double timeAtAngle(double theta);

    public abstract double nextTimeAtAngle(double theta, double t);

    public Position positionAtAngle(double theta) {
        double r = p / (1 + e * Math.cos(theta));
        if (r < 0 && Math.abs(theta) < Math.PI) {
            System.out.println(theta);
        }
        return new Position(r * Math.cos(theta + rotation), r * Math.sin(theta + rotation)).scale(1f / Constants.deltaX).add(focus.position());
    }

    public Position positionAtTime(double t) {
        double theta = angleAtTime(t);
        return positionAtAngle(theta);
    }
}
