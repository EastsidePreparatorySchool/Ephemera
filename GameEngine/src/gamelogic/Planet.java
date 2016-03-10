/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;

/**
 *
 * @author guberti
 */
public class Planet extends SpaceObject {
    
    public Planet(GameVisualizer vis, int x, int y, String packageName, String className, Constructor<?> cns) {
        super(vis, x, y, packageName, className, cns);
    }
    
    // Add code here to detail the working of planets
}
