/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import org.eastsideprep.spacecritters.gamelogic.Constants;

/**
 *
 * @author qbowers
 */
public class Ellipse extends Conic {

    double prevE;

    public Ellipse(Orbitable focus, double p, double e, double theta,  double rotation, SpaceGrid sg) {
        super(focus, p, e, theta, rotation, sg);

        //M0 = theta; // to do: This overrrides the more meaningful code in super()
        prevE = Math.PI; // seeding point for finding E. Pi always converges.
        n = mu * mu * Math.pow(1 - e * e, 2f / 3f) / h * h * h; // angular velocity

        orbits = 0;
    }

    public double EAtTrue(double theta) {
        return Math.acos((Math.cos(theta) + e) / (1 + e * Math.cos(theta))) * ((theta < 0 || theta > Math.PI) ? -1 : 1);
    }

    public double trueAtE(double E) {
        return Math.acos((Math.cos(E) - e) / (1 - e * Math.cos(E))) * ((E < 0 || E > Math.PI) ? -1 : 1);
    }

    public double EAtTime(double t) {
        //T = (2 pi / mu^2) * (h / root(1 - ||e||^2))^3
        //M = 2 pi t / T = n*t = E - ||e|| sin E
        //n = 2 pi / T
        //I have math from this point on

        //find mean anomaly
        double M = (n * (t-t0)) - (orbits * 2 * Math.PI) + M0;
        //System.out.println("M: " + M);
        while (M > Math.PI) {
            M -= 2 * Math.PI;
            orbits++;
            //System.out.println("loop: " + orbits);
        }
        
        //find eccentric anomaly using the Newton-Raphson method on Kepler's equation
        double E = prevE;
        double dE;
        int count = 0;
        do { 
            dE = E - e * Math.sin(E) - M;
            E -= dE / (1 - e * Math.cos(E));
            if (++count > 1000) {
                System.err.println("Stuck in Newton-Raphson loop in Ellipse:EAtTime");
                break;
            }
        } while (Math.abs(dE) >= Constants.accuracy);

        // give us a good starting point for next time
        if (t == sg.getTime()) {
            prevE = E;
        } else {
            prevE = Math.PI;
        }
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
}
