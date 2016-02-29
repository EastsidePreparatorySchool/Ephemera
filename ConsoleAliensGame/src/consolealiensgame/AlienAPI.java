/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consolealiensgame;

import alieninterfaces.*;

/**
 *
 * @author guberti
 */
public class AlienAPI implements Context {
    private AlienContainer aC;
    ViewImplementation view;
    final boolean debug = true;
    
    AlienAPI(AlienContainer aC) {
        this.aC = aC;
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
            System.err.println("DEBUG: " + s);
        }
    }
}
