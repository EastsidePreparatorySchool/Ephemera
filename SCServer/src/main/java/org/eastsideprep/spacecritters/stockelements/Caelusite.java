/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.stockelements;

import org.eastsideprep.spacecritters.alieninterfaces.*;

/**
 *
 * @author gmein
 */
public class Caelusite implements Alien, AlienComplex {

    Context ctx;
    int id,
        parent;
    String pastWisdom;

    public Caelusite() {}

    @Override
    public void initComplex(ContextComplex game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        this.id = id;
        this.parent = parent;
        pastWisdom = message;
    }

    
    @Override
    public Action getAction() {
        
        
        View view = null;
        try {
            view = ctx.getView(ctx.getTech());
        } catch (Exception e){}
        
        if(ctx.getEnergy() > 80) {
            if (ctx.getRandomInt(2) == 1) return new Action(Action.ActionCode.Spawn, 5);
            return new Action(Action.ActionCode.Research);
        }
        
        ctx.debugOut("Gaining energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
    }

    

    @Override
    public WorldVector getAccelerate() {
        if (ctx.getGameTurn() % 15 == 0) {
            return new WorldVector(1,0);
        }
        return null; 
    }
    
    @Override
    public void communicate() {
    }

    @Override
    public void receive(String[] messages) {
    }

    @Override
    public void processResults() {
    }
    

    
    // Obsolete but needs to exist
    @Override
    public void init(Context ctx, int id, int parent, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
