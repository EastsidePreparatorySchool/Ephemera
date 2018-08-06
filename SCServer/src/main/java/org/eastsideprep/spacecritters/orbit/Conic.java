/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.Vector3;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
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
    public double signum;

    public double rotation;

    int orbits;

    Orbitable focus;
    double mu;
    double M0;  // mean anomaly (cicrle angle) at time t0
    double n;   // angular velocity
    double t0;  // time this orbit was entered

    double theta;

    SpaceGrid sg;
    Vector2 lastV;

    public static Conic newConic(Orbitable focus, Vector2 r, Vector2 v, double t, SpaceGrid sg) {
        System.out.println("----------- new conic");
        System.out.println("-- input v: "+v.magnitude());
        /*
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
        //r_p (radius at periapse) = (h^2 / mu) / (1 + ||e|| cos theta)    v = v.subtract(focus.velocity(sg.getTime()));
        
        
        
        r = r.subtract(focus.worldPosition(sg.getTime()));
        double rAngle = Vector2.normalizeAngle(r.angle());
        //System.out.println("Measured angle: " + r.angle());

        Vector3 h = r.cross(v);
        double hm = h.magnitude();
        System.out.println("New H: " + hm);
        double signum = Math.signum(h.z);

        //System.out.println(" new sign :"+signum);
        double mu = focus.mass() * Constants.G;

        double rm = r.magnitude();

        Vector3 e = v.cross(h).scale(1 / mu).subtract(r.unit());

        double em = e.magnitude();
        System.out.println("new con: new e:" + em);
        if (em > 0.9) {
            System.out.println("hah!");
        }

        double p = hm * hm / mu;
        double rotation = Math.atan2(e.y, e.x);//Math.asin( v.dot(r.unit()) * hm / (mu*em) ) - theta;
        rotation = Vector2.normalizeAngle(rotation);
        double theta = Vector2.normalizeAngle(rAngle - rotation);//Math.acos((p/rm - 1) / em);//Math.acos(e.dot(r) / (em*rm));
         */

        r = r.subtract(focus.worldPosition(sg.getTime()));
        double rAngle = Vector2.normalizeAngle(r.angle());
        //System.out.println("Measured angle: " + r.angle());     
        double rm = r.magnitude();
        double vm = v.magnitude();

        double mu = focus.mass() * Constants.G;

        Vector3 h = r.cross(v);
        double hm = h.magnitude();
        double signum = Math.signum(h.z);
        double energy = vm * vm / 2 - mu / rm;

        Vector3 e = (r.scale(vm * vm - mu / rm).subtract(v.scale(r.dot(v)))).scale(1 / mu);
        double em = e.magnitude();

        double p;

        if (Math.abs(em - 1.0) > 0.001) {
            double a = -mu / (2 * energy);
            p = a * (1 - em * em);
        } else {
            p = hm * hm / mu;
        }

        double rotation = Math.atan2(e.x,e.y);
        double theta = Vector2.normalizeAngle(rAngle - rotation);

       
        Conic c = newConic(focus, p, em, theta, signum, rotation, sg);
        
        return c;
    }
    
    public void dump (){
        System.out.println("--e:        " + e);
        System.out.println("--p:        " + p);
        System.out.println("--theta:    " + theta);
        System.out.println("--rotation: " + rotation);
    }

    public static Conic newConic(Orbitable focus, double p, double e, double theta, double signum, double rotation, SpaceGrid sg) {
        if (e == 0) {
            return new Circle(focus, p, e, theta, signum, rotation, sg);
        }
        if (e < 1) {
            return new Ellipse(focus, p, e, theta, signum, rotation, sg);
        }
        if (e == 1) {
            return new Parabola(focus, p, e, theta, signum, rotation, sg);
        }
        if (e > 1) {
            return new Hyperbola(focus, p, e, theta, signum, rotation, sg);
        }
        throw new OrbitException("Invalid Eccentricity: " + e);
    }

    Conic(Orbitable focus, double p, double e, double theta, double signum, double rotation, SpaceGrid sg) {
        this.p = p;
        this.e = e;
        this.rotation = rotation;
        this.signum = signum;

        this.focus = focus;
        this.mu = Constants.G * focus.mass();

        this.sg = sg;

        this.h = Math.sqrt(this.p * mu);

        this.theta = theta;
        this.M0 = MAtAngle(theta);
        this.t0 = sg.getTime();
        this.lastV = new Vector2(0, 0);

    }

    public abstract double angleAtTime(double t);

    public abstract double timeAtAngle(double theta);

    public abstract double nextTimeAtAngle(double theta, double t);

    public abstract double MAtAngle(double theta);

    public WorldVector worldPositionAtAngle(double theta) {
        double r = p / (1 + e * Math.cos(theta));
        if (r < 0 && Math.abs(theta) < Math.PI) {
            System.out.println("Radius was negative at angle " + theta);
        }
        return new WorldVector(r * Math.cos(theta + rotation), r * Math.sin(theta + rotation));
    }

    public WorldVector worldPositionAtTime(double t) {
        double theta = angleAtTime(t);
        return new WorldVector(worldPositionAtAngle(theta).add(focus.worldPosition(t)));
    }

    @Override
    public Conic clone() {
        return newConic(focus, p, e, theta, signum, rotation, sg);
    }

    public WorldVector getVelocityAtTime(double t) {
        double theta1 = angleAtTime(t);

        double vperp = mu * (1 + e * Math.cos(theta1)) / h * (signum);
        double vrad = mu * e * Math.sin(theta1) / h;

        WorldVector v = new WorldVector(new Vector2(vrad, vperp).rotate(rotation + theta1));
        if (v.dot(lastV) < 0) {
            System.out.println("  velocity sign reversal");
        }
        lastV = v;

        return v;
    }

    public double partialHillRadius() {
        return p * (1 - e) / ((1 - e * e) * Math.pow(focus.mass(), 1f / 3));
    }
}
