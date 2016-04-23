/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.*;


/**
 *
 * @author jbriggs
 */
public class Alf implements Alien {

    Context ctx;
    static int totalCount = 0;
    int myAlienNumber;

    final boolean debug = true;

    public Alf() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        totalCount += 1;
        myAlienNumber = totalCount;

        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    public MoveDir getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        // Venusians run away from the nearest alien
        double techLevel = ctx.getTech();

        int[] nearestAlienPos = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY());

        int x = 0;
        int y = 0;

        //always moves away from other aliens
        if (nearestAlienPos[0] > ctx.getX()) {
            x = (int)(-techLevel / 2);
        } else if (nearestAlienPos[0] < ctx.getX()) {
            x = (int)(techLevel / 2);
        }

        if (nearestAlienPos[1] > ctx.getY()) {
            y = (int)(-techLevel / 2);
        } else if (nearestAlienPos[1] < ctx.getY()) {
            y = (int)(techLevel / 2);
        }
        //ctx.debugOut("Moving (" + Integer.toString(x) + "," + Integer.toString(y) + ")");

        return new MoveDir(x, y);
    }

    public Action getAction() {

        //ctx.debugOut("Action requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        View view = ctx.getView();

        //goal is to make a ton of Venusians fast and be good at hiding
        // catch and shenanigans
        try {
            // is there another alien on our position?
            if (view.getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("Others are here");
                // if so, do we have any energy?
                if (ctx.getEnergy() < 10) {
                    // no, keep moving.
                    return new Action(Action.ActionCode.Gain);
                }

                // or, hit really hard then run again
                ctx.debugOut("Fighting");
                return new Action(Action.ActionCode.Fight, (int)ctx.getEnergy() - 10);
            }
        } catch (Exception e) {
            // do something here to deal with errors
        }

        //
        // if we have spare energy, research tech
        //tech is not a priority
        if (ctx.getEnergy() > (ctx.getTech() + 5)) {

            if (ctx.getEnergy() > ctx.getSpawningCost() + 10) {
                //should spawn fast at the beggining,
                ctx.debugOut("Spawning");
                return new Action(Action.ActionCode.Spawn, 5);
            }
            if (ctx.getTech() < 30) {
                ctx.debugOut("Researching");
                return new Action(Action.ActionCode.Research);
            }
        }
        ctx.debugOut("Gaining");

        return new Action(Action.ActionCode.Gain);
    }

    public void processResults() {
        ctx.debugOut("Processing results");
        return;
    }

    @Override
    public void communicate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] messages) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void beThoughtful() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
