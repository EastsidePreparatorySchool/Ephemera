/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 *
 * @author guberti
 */
public class Planet extends SpaceObject {
    
    final String parent;
    
    public Planet (GameVisualizer vis, int x, int y, String packageName, String className, int energy, int tech, String parent) {
        super(vis, x, y, packageName, className, energy, tech);
        this.parent = parent;
    }
    
    // Add code here to detail the working of planets
}
