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

    public void init(Context game_ctx) {
        ctx = game_ctx;
        ctx.debugOut("Venusian initialized");

    }

    public MoveDir getMove() {

        ctx.debugOut("Venusian: Move requested,"
                + " E:" + Integer.toString(ctx.getEnergy())
                + " T:" + Integer.toString(ctx.getTech()));

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
        ctx.debugOut("Venusian: Moving ("+ Integer.toString(x) + "," + Integer.toString(y));
       
        return new MoveDir(x, y);
    }

    public Action getAction() {

        ctx.debugOut("Venusian: Action requested,"
                + " E:" + Integer.toString(ctx.getEnergy())
                + " T:" + Integer.toString(ctx.getTech()));

        View view = ctx.getView();

        //goal is to make a ton of Venusians fast and be good at hiding
        // catch and shenanigans
        try {
            // is there another alien on our position?
            if (view.isAlienAtPos(ctx.getX(), ctx.getY())) {
                // if so, do we have any energy?
                if (ctx.getEnergy() < 10) {
                    // no, keep moving.
                    return new Action(ActionCode.None);
                }

                // or, hit really hard then run again
                return new Action(ActionCode.Fight, ctx.getEnergy() - 10);
            }
        } catch (Exception e) {
            // do something here to deal with errors
        }

        //
        // if we have spare energy, research tech
        //tech is not a priority
        if (ctx.getEnergy() > (ctx.getTech() + 5)) {

            if (ctx.getEnergy() > 20) {
                //should spawn fast at the beggining,
                return new Action(ActionCode.Spawn);
            }
            return new Action(ActionCode.Research);
        }
        return new Action(ActionCode.Gain);
    }

    public void processResults() {
        ctx.debugOut("Venusian processing results");
        return;
    }
}
