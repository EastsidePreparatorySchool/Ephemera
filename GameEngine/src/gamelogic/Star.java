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
public class Star extends SpaceObject{
    
    public Star(GameVisualizer vis, int x, int y, String packageName, String className, Constructor<?> cns, List<Resident> residents) {
        super(vis, x, y, packageName, className, cns, residents);
    }
    
    // Add code here to detail the working of stars
}
