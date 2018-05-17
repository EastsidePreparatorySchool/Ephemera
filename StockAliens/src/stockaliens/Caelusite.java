/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

import alieninterfaces.*;

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
        ctx.debugOut("Initialized at "
                + ctx.getPosition().toString()
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));
    }

    // Obsolete but needs to exist
    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action getAction() {

        if(ctx.getEnergy() > 100) return new Action(Action.ActionCode.Spawn, 5);
        
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
        if (ctx.getGameTurn() % 15 == 0) return new Vector2(1,0);
        return new Vector2(0,0); 
    }
}
