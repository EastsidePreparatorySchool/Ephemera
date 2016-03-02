/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;

/**
 *
 * @author guberti
 */
public class ContextImplementation implements Context {
    private AlienContainer aC;
    ViewImplementation view;
    final boolean debug = true;
    GameVisualizer vis;
    
    ContextImplementation(AlienContainer aC, GameVisualizer vis) {
        this.aC = aC;
        this.vis = vis;
    }
    
    public int getEnergy() {
        return aC.energy;
    }
    
    public int getTech() {
        return aC.tech;
    }
    
    public int getX() {
        return aC.x;
    }
    
    public int getY() {
        return aC.y;
    }
    
    public View getView() {
        return view;
    }
    
    public void debugOut(String s) {
        if (debug) {
            vis.debugOut("Alien:Context:DEBUG: " + s);
        }
    }
}
