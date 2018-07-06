/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.gamelogic.Constants;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;

/**
 *
 * @author qbowers
 */
public class Hyperbola extends Conic {

    double prevF;

    public Hyperbola(Orbitable focus, double p, double e, double theta, double tNaught, double rotation, SpaceGrid sg) {
        super(focus, p, e, theta, tNaught, rotation, sg);

        M0 = theta;//TO DO
        prevF = M0;
        n = mu * mu * Math.pow(e * e - 1, 3f / 2f) / (h * h * h);

        orbits = 0;
    }
    
    public double trueAtF(double F) {
        return Math.acos((Math.cosh(F) - e) / (1 - e * Math.cosh(F))) * ((F < 0 || F > Math.PI) ? -1 : 1);
    }
    public double FAtTrue(double theta) {
        double coshF = (Math.cos(theta) + e) / (1 + e * Math.cos(theta));
        return Math.log(coshF + Math.sqrt(coshF*coshF - 1)) * ((theta < 0 || theta > Math.PI) ? -1 : 1);
    }
    

    @Override
    public double angleAtTime(double t) {
        //always returns the same number
        //draws the whole hyperbola line
        //this is probably an issue

        //M = mu^2 * (e^2 - 1)^3/2 * t / h^3
        //M = e sinh F - F
        //cos theta = (cosh F - e) / (1 - e cosh F)
        double M = n * t + M0;// - orbits*2*Math.PI;
        /*while (M > Math.PI) {
            M -= 2*Math.PI;
            orbits++;
        }*/
        double F = (t == sg.getTime()) ? prevF : M;
        double dF;
        int count = 0;
        do {
            dF = e * Math.sinh(F) - F - M;
            F -= dF / (e * Math.cosh(F) - 1);
             if (++ count > 1000) {
                System.err.println("Stuck in loop in Hyperbola angleAtTime");
                break;
            }
        } while (dF > Constants.accuracy);

        return trueAtF(F);
    }
    
    @Override
    public double MAtAngle(double theta) {
        double F = FAtTrue(theta);
        return e * Math.sinh(F) - F;
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
