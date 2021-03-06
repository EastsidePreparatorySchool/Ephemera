/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */


import org.eastsideprep.spacecritters.alieninterfaces.*;
import java.util.List;

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
        /*ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));
         */
    }

    public Direction getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        // Venusians run away from the nearest alien
        double techLevel = ctx.getTech();

        int x = 0;
        int y = 0;

        try {
            List<AlienSpecies> nearestAliens = ctx.getView((int) ctx.getTech()).getClosestXenos(null);
            if (nearestAliens != null && nearestAliens.size() > 0) {
                //System.out.println("ALf: seeing "+nearestAliens.size()+", "+ctx.getStateString());
                IntegerPosition nearest = nearestAliens.get(0).position;

                //always moves away from other aliens
                if (nearest.x > ctx.getPosition().x) {
                    x = (int) (-techLevel / 2);
                } else if (nearest.x < ctx.getPosition().x) {
                    x = (int) (techLevel / 2);
                }

                if (nearest.y > ctx.getPosition().y) {
                    y = (int) (-techLevel / 2);
                } else if (nearest.y < ctx.getPosition().y) {
                    y = (int) (techLevel / 2);
                }
            } else {
                //System.out.println("Alf: not seeing anyone");
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
                ctx.debugOut("Others are here");
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
