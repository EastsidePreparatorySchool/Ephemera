/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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

    @Override
    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + ctx.getStateString());

    }

    @Override
    public Direction getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        int x = 0;
        int y = 0;

        try {
            List<AlienSpecies> nearestAliens = ctx.getView((int) ctx.getTech()).getClosestXenos(
                    new AlienSpecies("eastsideprep.org", "stockaliens", "Alf", 0));
            if (nearestAliens != null) {
                IntegerPosition nearest = nearestAliens.get(0).position;

                if (nearest.x > ctx.getPosition().x) {
                    x = -1;
                } else if (nearest.x < ctx.getPosition().x) {
                    x = 1;
                }

                if (nearest.y > ctx.getPosition().x) {
                    y = -1;
                } else if (nearest.y < ctx.getPosition().y) {
                    y = 1;
                }

                // guard against tech fail
                //if (ctx.getTech() < 2.0) {
                //    y = 0;
                //}
            }
        } catch (Exception e) {
            x = ctx.getRandomInt(3) - 1;
            y = ctx.getRandomInt(3) - 1;

        }
        //ctx.debugOut("Moving (" + Integer.toString(x) + "," + Integer.toString(y) + ")");

        // move at least 1 
        if (x == 0 && y == 0) {
            x = ctx.getRandomInt(3) - 1;
            y = ctx.getRandomInt(3) - 1;
        }
        //but don't move into star
        try {
            if (ctx.getView(2).getSpaceObjectAtPos(ctx.getPosition().add(new IntegerDirection((int) x, (int) y))) != null) {
                y -= y > 0 ? 1 : -1;
            }
        } catch (NotEnoughEnergyException | NotEnoughTechException | View.CantSeeSquareException ex) {
        }

        return new Direction(x, y);
    }

    @Override
    public Action getAction() {

        //ctx.debugOut("Action requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        View view = null;

        try {
            view = ctx.getView((int) ctx.getTech());
        } catch (Exception e) {
        }

        //goal is to make a ton of Venusians fast and be good at hiding
        // catch and shenanigans
        try {
            // is there another alien on our position?
            if (view.getAliensAtPos(ctx.getPosition()).size() > 1) {
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
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
