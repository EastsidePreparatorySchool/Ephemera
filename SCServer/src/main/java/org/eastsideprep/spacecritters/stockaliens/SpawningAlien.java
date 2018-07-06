/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.stockaliens;

import org.eastsideprep.spacecritters.alieninterfaces.*;

/**
 *
 * @author abedi
 */
public class SpawningAlien implements Alien {

    //declaring everything that will need to be used later
    Context ctx;
    int HorizontalMove = 1;
    int VerticalMove = 1;
    int Remainder;
    int fightStrength;
    int turn = 0;
    int count = 0;
    //gets a random boolean to determine positive or negative move function.

    public boolean getRandomBoolean() {
        return (ctx.getRandomInt(2) == 0);
    }

    //empty constructor
    public SpawningAlien() {
    }

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.ctx = ctx;
        ctx.debugOut("Initialized at "
                + ctx.getStateString());

    }

    public Direction getMove() {
        if (count % ctx.getTech() == 0) {
            return new Direction(HorizontalMove, VerticalMove);
        }

        if (ctx.getPosition().x == ctx.getMaxPosition().x && ctx.getPosition().y == ctx.getMaxPosition().y) {
            HorizontalMove = -1;
            VerticalMove = 1;
        }
        // doesn't move ever except to 1,1 where it shall stay
        return new Direction(0, 0);
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

        // if there is someone at our position fight
        try {

            if (view.getAliensAtPos(ctx.getPosition()).size() > 1) {
                ctx.debugOut("Don't Blink!!!!!"
                        + ctx.getStateString());

                if (ctx.getEnergy() > ctx.getFightingCost() + 2) {
                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - ctx.getFightingCost() - 2);
                }

            }
            // always get energy
            if (ctx.getEnergy() < 25) {
                // no, charge
                ctx.debugOut("Choosing to gain Energy," + ctx.getStateString());
                return new Action(Action.ActionCode.Gain);
            }

            // do we have enough tech?
            if (ctx.getTech() < 20) {
                // no, research
                ctx.debugOut("Choosing to research" + ctx.getStateString());
                return new Action(Action.ActionCode.Research);
            }
            // every 
            turn = turn + 1;
            if (ctx.getEnergy() >= ctx.getSpawningCost() + 40) {
                ctx.debugOut("Spwaning" + ctx.getStateString());

                return new Action(Action.ActionCode.Spawn, (int) (ctx.getEnergy() - ctx.getSpawningCost()) / 2);
            }

        } catch (Exception e) {
        }

        // always gains energy
        ctx.debugOut("Weeping Angels: Must gain energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] messages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
