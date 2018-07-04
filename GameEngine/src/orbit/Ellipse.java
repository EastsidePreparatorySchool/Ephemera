/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package orbit;

import gamelogic.SpaceGrid;
import gamelogic.Constants;
import alieninterfaces.Vector2;

/**
 *
 * @author qbowers
 */
public class Ellipse extends Conic {

    double prevE;

    public Ellipse(Orbitable focus, double p, double e, double theta, double tNaught, double rotation, SpaceGrid sg) {
        super(focus, p, e, theta, tNaught, rotation, sg);

        M0 = theta; //TO DO
        prevE = M0;
        n = mu * mu * Math.pow(1 - e * e, 2f / 3f) / h*h*h;
        
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
        double M = (n * t) - (orbits * 2 * Math.PI) + M0;
        //System.out.println("M: " + M);
        while (M > Math.PI) {
            M -= 2 * Math.PI;
            orbits++;
            //System.out.println("loop: " + orbits);
        }
        //find eccentric anomaly
        double E = (t == sg.getTime()) ? prevE : M;
        double dE;
        int count = 0;
        do { //some calculouse black magic to find E (to within a certain accuracy)
            dE = E - e * Math.sin(E) - M;
            E -= dE / (1 - e * Math.cos(E));
            if (++ count > 1000) {
                System.err.println("Stuck in loop in Ellipse EAtTime");
                break;
            }
        } while (Math.abs(dE) >= Constants.accuracy);

        if (t == sg.getTime()) prevE = E;
        return E;
    }
    @Override
    public double angleAtTime(double t) {
        return trueAtE(EAtTime(t));
    }
    
    @Override
    public double MAtAngle(double theta) {
        double E = EAtTrue(theta);
        return E - e*Math.sin(E);
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
