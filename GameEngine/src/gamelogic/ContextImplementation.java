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
    GameVisualizer vis;
    public ViewImplementation view;
    
    ContextImplementation(AlienContainer aC, GameVisualizer vis) {
        this.aC = aC;
        this.vis = vis;
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
        vis.debugOut("Alien("+Integer.toHexString(aC.alien.hashCode()).toUpperCase()+"): " + s);
    }
}
