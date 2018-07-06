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

    public Caelusite() {}

    @Override
    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        
    }

    // Obsolete but needs to exist
    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action getAction() {
        
        
        View view = null;
        try {
            view = ctx.getView(ctx.getTech());
        } catch (Exception e){
        }
        
        
        if (view != null) try {
            int neighbors = view.getAliensAtPos(ctx.getPosition()).size();
            if (neighbors < ctx.getEnergy() * 3 && neighbors > 0 && ctx.getEnergy() > 20) return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy()/3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        

        if(ctx.getEnergy() > 80) {
            if (ctx.getRandomInt(2) == 1) return new Action(Action.ActionCode.Spawn, 5);
            return new Action(Action.ActionCode.Research);
        }
        
        ctx.debugOut("Gaining energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
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

    @Override
    public Vector2 getAccelerate() {
        
        
        if (ctx.getGameTurn() % 15 == 0) {
            return new Vector2(1,0);
        }
        return new Vector2(0,0); 
    }
}
