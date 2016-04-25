/*
 * 
 */
package stockaliens;

import alieninterfaces.*;
import java.util.List;

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
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    public MoveDir getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        int x = 0;
        int y = 0;

        try {
            List<AlienSpecies> nearestAliens = ctx.getView().getClosestXenosToPos(
                    new AlienSpecies("eastsideprep.org", "stockaliens", "Alf", 0),
                    ctx.getX(), ctx.getY());
            if (!nearestAliens.isEmpty()) {
                int nearestX = nearestAliens.get(0).x;
                int nearestY = nearestAliens.get(0).y;

                if (nearestX > ctx.getX()) {
                    x = -1;
                } else if (nearestX < ctx.getX()) {
                    x = 1;
                }

                if (nearestY > ctx.getY()) {
                    y = -1;
                } else if (nearestY < ctx.getY()) {
                    y = 1;
                }
                
                // guard against tech fail
                //if (ctx.getTech() < 2.0) {
                //    y = 0;
                //}
            }
        } catch (Exception e) {
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
            if (view.getAliensAtPos(ctx.getX(), ctx.getY()).size() > 1) {
                // if so, do we have any energy?
                if (ctx.getEnergy() < 10) {
                    // no, keep moving.
                    return new Action(Action.ActionCode.Gain);
                }

                // or, hit really hard then run again
                ctx.debugOut("Fighting");
                return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 10);
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
