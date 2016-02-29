/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.*;
import java.util.Random;

/**
 *
 * @author gmein
 */
public class Dalek implements Alien {

    Random rand;
    Context ctx;
    
    final boolean debug = true;
    
    
    

    public Dalek() {
    }
    
    public void init(Context game_ctx) {
        rand = new Random();
        ctx = game_ctx;
        ctx.debugOut("Dalek initialized");

        
    }

    // Martians move left, right, left, right
    public MoveDir getMove() {
        
        ctx.debugOut("Dalek Move requested");
        
        
        int move_energy;

        // don't move more than you have tech
        move_energy = Math.min(ctx.getEnergy(), ctx.getTech());
        // don't move more than 5, leave energy for other stuff
        move_energy = Math.min(move_energy, 5);

        // spend a random amount of that moving into x direction
        int x = rand.nextInt() % move_energy;
        move_energy -= x;

        // and y takes the rest
        int y = move_energy;

        // don't move off the board
        x = Math.min(ctx.getX(), x);
        y = Math.min(ctx.getY(), y);

        return new MoveDir(x, y);
    }

    public Action getAction() {
        
        ctx.debugOut("Dalek Action requested");
        
        View view = ctx.getView();

        
        // catch and shenanigans
        try {
            // is there another alien on our position?
            if (view.isAlienAtPos(ctx.getX(), ctx.getY())) {
                // if so, do we have any energy?
                if (ctx.getEnergy() < 3) {
                    // no, lie still and start praying
                    return new Action(ActionCode.Gain);
                }

                // or, spend our energy on fighting
                return new Action(ActionCode.Fight, ctx.getEnergy() - 2);
            }
        } catch (Exception e) {
            // do something here to deal with errors
        }

        //
        // if we have spare energy, research tech
        //
        if (ctx.getEnergy() > (ctx.getTech() + 2)) {
            return new Action(ActionCode.Research);
        }
        return new Action(ActionCode.Gain);
    }

    public void processResults() {
        ctx.debugOut("Dalek processing results");
        return;
    }

}

