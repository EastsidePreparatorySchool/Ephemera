/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;

/**
 *
 * @author qbowers
 */
public class Circle extends Conic {

    public Circle(Orbitable focus, double p, double e, double theta,  double rotation, SpaceGrid sg) {
        super(focus, p, e, theta,  rotation, sg);

        M0 = theta; //TO DO
        n = Math.sqrt(mu / p);
    }

    @Override
    public double angleAtTime(double t) {
        return n * t + M0;
    }
    @Override
    public double MAtAngle(double theta){
        return theta;
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
