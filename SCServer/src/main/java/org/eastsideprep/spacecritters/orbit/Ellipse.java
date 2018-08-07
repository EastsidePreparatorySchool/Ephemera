/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.alieninterfaces.WorldVector;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import org.eastsideprep.spacecritters.gamelogic.Constants;

/**
 *
 * @author qbowers
 */
public class Ellipse extends Conic {

    double prevE;

    public Ellipse(Orbitable focus, double p, double e, double theta, double signum, double rotation, SpaceGrid sg) {
        super(focus, p, e, theta, signum, rotation, sg);

        prevE = -10; // seeding point for finding E. -10 = invalid, start from M
        n = (mu * mu) * Math.pow(1 - e * e, 2f / 3f) / (h * h * h); // angular velocity
        n *= signum;
        //System.out.println("New ellipse: signum " + signum);
        orbits = 0;

//        WorldVector r = this.worldPositionAtTime(sg.getTime());
//        WorldVector v = this.getVelocityAtTime(sg.getTime());
//        System.out.println("-- output r: " + r + ", mag: " + r.magnitude());
//        System.out.println("-- output v: " + v + ", mag: " + v.magnitude());
    }

    public double EAtTrue(double theta) {
        return Math.acos((Math.cos(theta) + e) / (1 + e * Math.cos(theta))) * ((theta < 0 || theta > Math.PI) ? -1 : 1);
    }

    public double trueAtE(double E) {
        return Math.acos((Math.cos(E) - e) / (1 - e * Math.cos(E))) * ((E < 0 || E > Math.PI) ? -1 : 1);
    }

    public double EAtTime(double t) {
        //System.out.println("calculating E at time "+t+" for "+this);
        //T = (2 pi / mu^2) * (h / root(1 - ||e||^2))^3
        //M = 2 pi t / T = n*t = E - ||e|| sin E
        //n = 2 pi / T
        //I have math from this point on

        //find mean anomaly
        double M = (n * (t - t0)) - (orbits * 2 * Math.PI) + M0;
        //System.out.println("M: " + M);
        while (M > Math.PI) {
            M -= 2 * Math.PI;
            orbits++;
            //System.out.println("loop: " + orbits);
        }

        if (true) {
            return KeplerAlgorithms.kepler(e, M);
        }
        double E;

        //find eccentric anomaly using the Newton-Raphson method on Kepler's equation
        if (prevE != -10) {
            E = prevE;
        } else {
            if (e < 0.95) {
                E = M;
            } else {
                E = Math.PI;
            }
        }
        double dE;
        double E0 = E;
        int count = 100;
        do {
            dE = E - e * Math.sin(E) - M;
            E = (M - e * (E * Math.cos(E) - Math.sin(E))) / (1 - e * Math.cos(E));
            try {
                int i = 200 / --count;
            } catch (Exception ex) {
                System.out.println("ell " + this + " Stuck in Newton-Raphson loop in Ellipse:EAtTime - aborting");
                System.out.println("  e: " + e);
                System.out.println(" E0: " + E0);
                System.out.println(" pE: " + prevE);
                System.out.println("  M: " + M);
                System.out.println("  t: " + t);
                ex.printStackTrace();

                if (e > 0.999) {
                    E = M;
                }
                break;
            }
        } while (Math.abs(dE) >= Constants.accuracy);

        // give us a good starting point for next time
        if (t == sg.getTime()) {
            prevE = E;
        } else {
            prevE = -10.0;
        }
        //System.out.println("ell" + this + " E: " + E + " e " + e);
        return E;
    }

    @Override
    public double angleAtTime(double t) {
        return trueAtE(EAtTime(t));
    }

    @Override
    public double MAtAngle(double theta) {
        double E = EAtTrue(theta);
        return E - e * Math.sin(E);
    }

    @Override
    public double timeAtAngle(double theta) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double nextTimeAtAngle(double theta, double t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public WorldVector getVelocityAtTime(double t) {
//        // from https://space.stackexchange.com/questions/22172/calculating-velocity-state-vector-with-orbital-elements-in-2d
//
//        double a = p / (1 - e * e);             // semi-major axis
//        double b = a * Math.sqrt(1 - e * e);    // semi-minor axis
//
//        double E = EAtTime(t);                  // eccentric anomaly
//        double f = trueAtE(E);                  // true anomaly
//        double rm;                              // length of position vector from focus
//        double vm;                              // velocity magnitude
//
//        if (e < 0.999) {
//            rm = a * Math.sqrt(1 - e * e) / (1 + e * Math.cos(f));
//            vm = Math.sqrt(mu * (2 / rm - 1 / a));
//        } else {
//            rm = a;
//            vm = Math.sqrt(mu / a);
//        }
//        //System.out.println("ell" + this + " e.v: rlength " + rm + " velocity " + vm + " t " + t);
//
//      
//        Vector2 v = new Vector2(-a * Math.sin(E), b * Math.cos(E)).rotate(rotation).unit();
//        v = v.scale(vm * signum);
//        //System.out.println("ell" + this + " e.v: vx " + v.x + " vy " + v.y);
//
//        return new WorldVector(v);
//    }
}
