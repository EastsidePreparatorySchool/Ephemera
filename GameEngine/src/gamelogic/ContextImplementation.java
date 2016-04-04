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
    public GameVisualizer vis;
    public ViewImplementation view;
    public boolean chatter;
    
    ContextImplementation(AlienContainer aC, GameVisualizer vis) {
        this.aC = aC;
        this.vis = vis;
        chatter = false;
        view = new ViewImplementation ();
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
        return this.view;
    }
    
    public void debugOut(String s) {
        if (chatter) {
            vis.debugOut("Alien "+aC.alienPackageName + ":" + aC.alienClassName + "(" 
                    +Integer.toHexString(aC.alien.hashCode()).toUpperCase()+"): " + s);
        }
    }
}
