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
        n = mu * mu * Math.pow(1 - e * e, 2f / 3f);
        System.out.println("mu: " + mu);
        //System.out.println("Period in turns: " + (1f/n) / Constants.deltaT);

        orbits = 0;
    }

    @Override
    public double angleAtTime(double t) {
        System.out.println("ellipse.angle at time");
        //T = (2 pi / mu^2) * (h / root(1 - ||e||^2))^3
        //M = 2 pi t / T = n*t = E - ||e|| sin E
        //n = 2 pi / T
        //I have math from this point on

        //find mean anomaly
        double M = (n * t) - (orbits * 2 * Math.PI) + M0;
        System.out.println("n: " + n);
        System.out.println("M: " + M);
        while (M > Math.PI) {
            M -= 2 * Math.PI;
            orbits++;
            //System.out.println("loop: " + orbits);
        }
        System.out.println("end ellipse.angleattime");
        //find eccentric anomaly
        double E = (t == sg.getTime()) ? prevE : M;
        double dE;
        do { //some calculouse black magic to find E (to within a certain accuracy)
            dE = E - e * Math.sin(E) - M;
            E -= dE / (1 - e * Math.cos(E));
        } while (Math.abs(dE) >= Constants.accuracy);

        prevE = E;

        //find true anomaly
        
        return Math.acos((Math.cos(E) - e) / (1 - e * Math.cos(E))) * ((E < 0 || E > Math.PI) ? -1 : 1);
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
