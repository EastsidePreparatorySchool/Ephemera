/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orbit;

import alieninterfaces.Vector2;
import gamelogic.SpaceGrid;

/**
 *
 * @author qbowers
 */
public class Parabola extends Conic {

    public Parabola(Orbitable focus, double p, double e, double theta, double tNaught, double rotation, SpaceGrid sg) {
        super(focus, p, e, theta, tNaught, rotation, sg);

        M0 = theta;
        n = mu * mu / (h * h * h);
    }

    @Override
    public double angleAtTime(double t) {
        //M = mu^2 t / h^3
        //tan (theta/2) = ( 3M + root( (3M)^2 + 1 ) )^1/3 - ( 3M + root( (3M)^2 + 1 ) )^-1/3

        double M = n * t + M0;

        double c = Math.pow(3 * M + Math.sqrt(9 * M * M + 1), 1f / 3f);
        
        
        double theta = 2 * Math.atan(c + 1f / c);
        if (t == sg.getTime()) {
            theta0 = theta1;
            theta1 = theta;
        }
        return theta;
        //always returns the same number. This is probably an issue
    }
    
    @Override
    public double MAtAngle(double theta) {
        return Math.tan(theta/2)/2 + Math.pow(Math.tan(theta/2), 2)/6;
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
