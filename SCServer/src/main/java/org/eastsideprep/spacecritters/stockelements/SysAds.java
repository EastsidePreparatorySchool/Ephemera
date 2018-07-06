/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.stockelements;

import org.eastsideprep.spacecritters.alieninterfaces.*;
import org.eastsideprep.spacecritters.gameengineinterfaces.*;
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
        ctx.debugOut("SysAds initialized");
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
        ctx.debugOut("SysAds resident alien interface initialized");
    }
}
