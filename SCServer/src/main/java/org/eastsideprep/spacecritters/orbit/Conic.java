/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.Vector3;
import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;

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

    public double p;
    public double e;
    double h;

    public double rotation;

    int orbits;

    Orbitable focus;
    double mu;
    double M0;
    double n;
    double t0;
   

    double theta;
    
    double theta0;
    double theta1;

    SpaceGrid sg;
    public static Conic newConic(Orbitable focus, Vector2 r, Vector2 v, double t, SpaceGrid sg) {
        v = v.subtract(focus.velocity(sg.getTime()));
        r = r.subtract(focus.position(sg.getTime())).scale(Constants.deltaX);
        System.out.println("r angle to f: " + Vector2.normalizeAngle(r.angle()));
//        System.out.println("r: ("+r.x+","+r.y+","+r.z+")");

        Vector3 h = r.cross(v);
//        System.out.println("h: ("+h.x+","+h.y+","+h.z+")");
        double hm = h.magnitude();
//        System.out.println("New H: " + hm);

        double mu = focus.mass() * Constants.G;

        double rm = r.magnitude();

        Vector3 e = v.cross(h).scale(1 / mu).subtract(r.unit());
        //e = (r.scale(Math.pow(v.magnitude(),2)-mu/r.magnitude()).subtract(v.scale(r.dot(v)))).scale(1/mu);

        double em = e.magnitude();

        double p = hm * hm / mu;
        double rotation = Vector2.normalizeAngle(Math.atan2(e.y, e.x));//Math.asin( v.dot(r.unit()) * hm / (mu*em) ) - theta;
        System.out.println("rotation: " + rotation);
        double theta = Vector2.normalizeAngle(r.angle() - rotation);
        System.out.println("theta: " + theta);
        ////
//        System.out.println("some values:");
//        System.out.println("e: " + em);
//        System.out.println("p: " + p/Constants.deltaX);
//        System.out.println("theta: " + theta);
//        System.out.println("rotation of conic: " + rotation);

        return newConic(focus, p / Constants.deltaX, em, theta, rotation, sg);
    }

    public static Conic newConic(Orbitable focus, double p, double e, double theta, double rotation, SpaceGrid sg) {
        if (e == 0) {
            return new Circle(focus, p, e, theta, rotation, sg);
        }
        if (e < 1) {
            return new Ellipse(focus, p, e, theta, rotation, sg);
        }
        if (e == 1) {
            return new Parabola(focus, p, e, theta, rotation, sg);
        }
        if (e > 1) {
            return new Hyperbola(focus, p, e, theta, rotation, sg);
        }
        throw new OrbitException("Invalid Eccentricity: " + e);
    }

    Conic(Orbitable focus, double p, double e, double theta, double rotation, SpaceGrid sg) {
        this.p = p * Constants.deltaX;
        this.e = e;
        this.rotation = rotation;

        this.focus = focus;
        this.mu = Constants.G * focus.mass();

        this.sg = sg;

        this.h = Math.sqrt(this.p * mu);

        this.theta = theta;
        this.M0 = MAtAngle(theta);
        this.t0 = sg.getTime();
        

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

    public abstract double MAtAngle(double theta);
    
    public double radiusAtAngle(double theta) { return p / (1 + e * Math.cos(theta)); }
    public double angleAtRadius(double r) { return Math.acos((r - p) / (-r*e)); }
    
    public Position positionAtAngle(double theta) {
        double r = radiusAtAngle(theta);
        if (r < 0 && Math.abs(theta) < Math.PI) {
            System.out.println("Radius was negative at angle " + theta);
        }
        return new Position(r * Math.cos(theta + rotation), r * Math.sin(theta + rotation)).scale(1f / Constants.deltaX);
    }
    
    public Position positionAtTime(double t) {
        double theta = angleAtTime(t);
        return positionAtAngle(theta).add(focus.position(t));
    }

    @Override
    public Conic clone() {
        return newConic(focus, p / Constants.deltaX, e, theta, rotation, sg);
    }

    public Vector2 getVelocityAtTime(double t) {
        double theta = angleAtTime(t);

        double vperp = mu * (1 + e * Math.cos(theta)) / h;
        double vrad = mu * e * Math.sin(theta) / h;

        Vector2 v = new Vector2(vrad, vperp).rotate(rotation + theta);
        return v;
    }

    public double partialHillRadius() {
        return p * (1 - e) / ((1 - e * e) * Math.pow(3 * focus.mass(), 1f / 3)) / Constants.deltaX;
    }
    
    public double periapse() { return radiusAtAngle(0); }
    public double apoapse() { 
        if (this instanceof Parabola || this instanceof Hyperbola) return Double.POSITIVE_INFINITY;
        return radiusAtAngle(Math.PI);
    }
}
