/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;

/**
 *
 * @author gunnar
 */

public class Orbit {

    public SpaceObject focus;   // star or planet
    public double a;            // semi-major axis
    public double e;            // eccentricity
    public double rotation;     // 
    public double signum;       // 1.0 = counter-clockwise
    public double mu;           // G*M
    public double h;
}
