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
public interface ContextComplex extends Context {

    WorldVector getVelocity();

    double getMeanAnomaly();

    SpaceObject getFocus();

    double getMass();

    Orbit getOrbit();
}
