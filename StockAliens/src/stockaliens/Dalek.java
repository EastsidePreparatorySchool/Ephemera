/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.*;

/**
 *
 * @author gmein
 */
public class Dalek implements Alien {

    Context ctx;

    final boolean debug = true;

    public Dalek() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    // Martians move left, right, left, right
    public MoveDir getMove() {

        ctx.debugOut("Move requested,"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));
        int move_energy;

        // don't move more than you have tech
        move_energy = (int) Math.min(ctx.getTech(), ctx.getEnergy());
        // don't move more than 5, leave energy for other stuff
        move_energy = Math.min(move_energy, 5);

        if (move_energy <= 2) {
            return new MoveDir(0, 0);
        }

        // spend a random amount of that moving into x direction
        int powerX = ctx.getRandomInt(move_energy / 2);
        int powerY = ctx.getRandomInt(move_energy / 2);
        int x = powerX * (ctx.getRandomInt(2) == 0 ? -1 : 1);
        int y = powerY * (ctx.getRandomInt(2) == 0 ? -1 : 1);

        ctx.debugOut("Moving (" + Integer.toString(x) + "," + Integer.toString(y) + ")");
        return new MoveDir(x, y);
    }

    public Action getAction() {

        ctx.debugOut("Action requested,"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));

        // catch any shenanigans
        if (ctx.getEnergy() > 100) {
            try {
                View view = ctx.getView((int) ctx.getTech());
                if (view.getAliensAtPos(ctx.getX(), ctx.getY()).size() > 1) {
                    ctx.debugOut("Uh-oh.There is someone else here."
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech())
                            + " X:" + Double.toString(ctx.getX())
                            + " Y:" + Double.toString(ctx.getY()));
                }

                // do we have enough energy?
                if (ctx.getEnergy() < 10) {
                    // no, charge
                    ctx.debugOut("Choosing to gain energy,"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));
                    return new Action(Action.ActionCode.Gain);
                }

                // do we have enough tech?
                if (ctx.getTech() < 30 && ctx.getEnergy() > ctx.getTech()) {
                    // no, research
                    ctx.debugOut("Choosing to research"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));
                    return new Action(Action.ActionCode.Research);
                }

                // is there another alien on our position?
                if (view.getAliensAtPos(ctx.getX(), ctx.getY()).size() > 1
                        && ctx.getEnergy() > ctx.getFightingCost() + 2) {
                    ctx.debugOut("EXTERMINATE!!!!!"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech())
                            + " X:" + Double.toString(ctx.getX())
                            + " Y:" + Double.toString(ctx.getY()));

                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 2 - ctx.getFightingCost());
                }

                if (ctx.getEnergy() > (ctx.getSpawningCost() + 10)) {
                    // no other aliens here, have enough stuff, spawn!
                    ctx.debugOut("DALEKS RULE SUPREME! SPAWNING!"
                            + " E:" + Double.toString(ctx.getEnergy())
                            + " T:" + Double.toString(ctx.getTech()));

                    return new Action(Action.ActionCode.Spawn, 5);
                }
            } catch (Exception e) {
                // do something here to deal with errors
                ctx.debugOut("EXPLAIN?????? " + e.toString());
            }
        }
        ctx.debugOut("Gaining energy"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {
        if (ctx.getEnergy() > 100) {
            try {
                if (ctx.getGameTurn() % 20 == 0) {
                    ctx.broadcastAndListen("I say: Daleks rule supreme!!!!!", 1, true);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void receive(String[] messages) {
        for (String s : messages) {
            ctx.debugOut(s);
        }
    }

    @Override
    public void beThoughtful() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
