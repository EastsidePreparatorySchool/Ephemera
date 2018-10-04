/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;

import org.eastsideprep.spacecritters.gamelogic.InternalSpaceObject;

/**
 *
 * @author gunnar
 */

public class Orbit {

    public SpaceObject focus;   // star or planet
    public double p;            // semi-latus rectum
    public double a;            // semi-major axis -- this will be undefined for parabolas
    public double e;            // eccentricity
    public double rotation;     // 
    public double signum;       // 1.0 = counter-clockwise
    public double mu;           // G*M
    public double h;            // angular momentum
}
