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

    static Random rand = new Random(System.currentTimeMillis() * Dalek.class.hashCode());
    Context ctx;

    final boolean debug = true;

    public Dalek() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Integer.toString(ctx.getEnergy())
                + " T: " + Integer.toString(ctx.getTech()));

    }

    // Martians move left, right, left, right
    public MoveDir getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        int move_energy;

        // don't move more than you have tech
        move_energy = ctx.getTech();
        // don't move more than 5, leave energy for other stuff
        move_energy = Math.min(move_energy, 10);

        // spend a random amount of that moving into x direction
        int powerX = rand.nextInt(move_energy) ;
        int x = powerX * (rand.nextInt(2) == 0?-1:1);
        move_energy -= powerX;
        // and y takes the rest
        int y = move_energy * (rand.nextInt(2) == 0?-1:1);

        //ctx.debugOut("Moving (" + Integer.toString(x) + "," + Integer.toString(y) + ")");
        return new MoveDir(x, y);
    }

    public Action getAction() {

        ctx.debugOut("Action requested,"
                + " E:" + Integer.toString(ctx.getEnergy())
                + " T:" + Integer.toString(ctx.getTech()));
        View view = ctx.getView();

        // catch and shenanigans
        try {
            if (view.getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("Uh-oh.There is someone else here."
                        + " E:" + Integer.toString(ctx.getEnergy())
                        + " T:" + Integer.toString(ctx.getTech())
                        + " X:" + Integer.toString(ctx.getX())
                        + " Y:" + Integer.toString(ctx.getY()));
            }

            // do we have enough energy?
            if (ctx.getEnergy() < 10) {
                // no, charge
                ctx.debugOut("Choosing to gain energy,"
                        + " E:" + Integer.toString(ctx.getEnergy())
                        + " T:" + Integer.toString(ctx.getTech()));
                return new Action(ActionCode.Gain);
            }

            // do we have enough tech?
            if (ctx.getTech() < 10 && ctx.getEnergy() > ctx.getTech()) {
                // no, research
                ctx.debugOut("Choosing to research"
                        + " E:" + Integer.toString(ctx.getEnergy())
                        + " T:" + Integer.toString(ctx.getTech()));
                return new Action(ActionCode.Research);
            }

            // is there another alien on our position?
            if (view.getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("EXTERMINATE!!!!!"
                        + " E:" + Integer.toString(ctx.getEnergy())
                        + " T:" + Integer.toString(ctx.getTech())
                        + " X:" + Integer.toString(ctx.getX())
                        + " Y:" + Integer.toString(ctx.getY()));

                return new Action(ActionCode.Fight, ctx.getEnergy() - 2);
            }

            if (ctx.getEnergy() > (ctx.getSpawningCost() + 10))  {
                // no other aliens here, have enough stuff, spawn!
                ctx.debugOut("DALEKS RULE SUPREME! SPAWNING!"
                        + " E:" + Integer.toString(ctx.getEnergy())
                        + " T:" + Integer.toString(ctx.getTech()));

                return new Action(ActionCode.Spawn, 5);
            }
        } catch (Exception e) {
            // do something here to deal with errors
            ctx.debugOut("EXPLAIN??????");
        }

        return new Action(ActionCode.Gain);
    }


    @Override
    public void communicate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
