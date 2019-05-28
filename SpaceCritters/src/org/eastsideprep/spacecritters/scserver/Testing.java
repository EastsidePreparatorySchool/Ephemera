/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.scserver;

import org.eastsideprep.spacecritters.gamelogic.SpaceGrid;
import org.eastsideprep.spacecritters.orbit.*;

/**
 *
 * @author qbowers
 */
public class Testing {
    
    
    
    static Orbitable dummy = new DummyMass();
    public static void run() {
        System.out.println("\n\n\n\n\n\n");
        
        
        /*double prev = speed(Math.PI/2f + 0.2);
        for (double i = Math.PI / 2f; i >= 0; i-= 0.1) {
            double v = speed(i);
            System.out.println("velocity: " + v);
            if (prev > v) {
                System.out.println("AAAAAHHHHHHHH: " + (prev - v));
            }
            prev = v;
        }*/
        
        for (double theta = Math.PI / 2f; theta >= 0; theta-= 0.1) {
            Conic clockwise = Conic.newConic(dummy,
                    1, 1.2, theta, 1, 0,
                    new SpaceGrid());
            clockwise.updateStateVectors(0);
            Conic counter = Conic.newConic(dummy,
                    1, 1.2, theta, -1, 0,
                    new SpaceGrid());
            counter.updateStateVectors(0);
            
            System.out.println("Clockwise: " + clockwise.vCurrent);
            System.out.println("Counter Clockwise: " + counter.vCurrent);
        }
        
        
        System.out.println("Test complete\n\n\n\n\n\n");
    }
    
    public static double speed(double theta) {
        Conic t = Conic.newConic(dummy,
                1, 1.2, theta, 1, 0,
                new SpaceGrid()
        );
        t.updateStateVectors(0);
        return t.vCurrent
                .magnitude();
    }
}
