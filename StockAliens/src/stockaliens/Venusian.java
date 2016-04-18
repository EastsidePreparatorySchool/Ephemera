/*
 * 
 */
package stockaliens;

import alieninterfaces.*;
import java.util.Random;

/**
 *
 * @author Marnie Manning
 */
public class Venusian implements Alien {

    Context ctx;

    final boolean debug = true;

    public Venusian() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Integer.toString(ctx.getEnergy())
                + " T: " + Integer.toString(ctx.getTech()));

    }

    public MoveDir getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));

        // Venusians run away from the nearest alien
        int[] nearestAlienPos = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY());

        int x = 0;
        int y = 0;

        //always moves away from other aliens
        if (nearestAlienPos[0] > ctx.getX()) {
            x = -1;
        } else if (nearestAlienPos[0] < ctx.getX()) {
            x = 1;
        }

        if (nearestAlienPos[1] > ctx.getY()) {
            y = -1;
        } else if (nearestAlienPos[1] < ctx.getY()) {
            y = 1;
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
                // if so, do we have any energy?
                if (ctx.getEnergy() < 10) {
                    // no, keep moving.
                    return new Action(ActionCode.Gain);
                }

                // or, hit really hard then run again
                ctx.debugOut("Fighting");
                return new Action(ActionCode.Fight, ctx.getEnergy() - 10);
            }
        } catch (Exception e) {
            // do something here to deal with errors
        }

        //
        // if we have spare energy, research tech
        //tech is not a priority
        if (ctx.getEnergy() > (ctx.getTech() + 5)) {

            // TODO: Assumption: spawning cost will never be greater than 20
            if (ctx.getEnergy() > ctx.getSpawningCost() + 10) {
                //should spawn fast at the beggining,
                ctx.debugOut("Spawning");
                return new Action(ActionCode.Spawn, 5);
            }
            if (ctx.getTech() < 30) {
                ctx.debugOut("Researching");
                return new Action(ActionCode.Research);
            }
        }
        ctx.debugOut("Gaining");

        return new Action(ActionCode.Gain);
    }


    @Override
    public void communicate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
