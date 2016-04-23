/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.*;


/**
 *
 * @author guberti
 */
public abstract class SpaceObject {
    
    public int x;
    public int y;
    public final String packageName;
    public final String className;
    
    public double energy; // Energy that aliens gain every turn from the planet
    public double tech; // Tech boost for the planet
    
    public SpaceObject (GameVisualizer vis, int x, int y, String packageName, String className, double energy, double tech) {
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.tech = tech;
        this.packageName = packageName;
        this.className = className;
    }
}
