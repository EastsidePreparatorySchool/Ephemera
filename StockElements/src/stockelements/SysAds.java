/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockelements;

import alieninterfaces.*;
import gameengineinterfaces.*;
import gamelogic.*;
import java.util.HashMap;


/**
 *
 * @author gunnar
 */
public class SysAds implements Alien, ResidentAlien {
    
    Context ctx;
    ResidentContext rc;
    int id;
    HashMap<String, Integer> secrets;


    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.ctx = ctx;
        this.id = id;
    }

    @Override
    public void communicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] messages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Direction getMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Action getAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    // resident interface implementation starts here

    @Override
    public void init(ResidentContext rc) {
        this.rc = rc;
        
        // SysAds are not to be trifled with ...
        rc.setEnergy(1000);
        rc.setTech(255);
    }
}
