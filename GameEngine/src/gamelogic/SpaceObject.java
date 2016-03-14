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
    public final Constructor<?> constructor;
    public List<Resident> residents;
    
    public SpaceObject (GameVisualizer vis, int x, int y, String packageName, String className, Constructor<?> cns, List<Resident> residents) {
        this.x = x;
        this.y = y;
        this.packageName = packageName;
        this.className = className;
        this.constructor = cns;
    }
}
