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
public class AggressiveAlien implements Alien {

    Context ctx;

    final boolean debug = true;

    public AggressiveAlien() {
    }

    @Override
    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + ctx.getPosition().toString()
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    // Martians move left, right, left, right
    @Override
    public Direction getMove() {

        ctx.debugOut("Move requested,"
                + ctx.getStateString());
        int move_energy;

        // don't move more than you have tech
        move_energy = (int) Math.min(ctx.getTech(), ctx.getEnergy());
        // don't move more than 5, leave energy for other stuff
        move_energy = Math.min(move_energy, 5);

        int powerX;
        int powerY;

        if (move_energy <= 2) {
            powerX = ctx.getRandomInt(3) - 1;
            powerY = ctx.getRandomInt(3) - 1;
        } else {
            // spend a random amount of that moving into x direction
            powerX = ctx.getRandomInt(move_energy / 2);
            powerY = ctx.getRandomInt(move_energy / 2);
        }

        int x = powerX * (ctx.getRandomInt(2) == 0 ? -1 : 1);
        int y = powerY * (ctx.getRandomInt(2) == 0 ? -1 : 1);

        Direction dir = new Direction(x, y);

        // don't park, a planet might hit you
        if (dir.x == 0 & dir.y == 0) {
            dir = new Direction (ctx.getRandomInt(2) == 0 ? -1 : 1,ctx.getRandomInt(2) == 0 ? -1 : 1);
        }

        try {
            if (ctx.getView(move_energy).getSpaceObjectAtPos(ctx.getPosition().add(dir)) != null) {
                // don't be a dumbass, don't move into a star
                ctx.debugOut("Avoiding Star at " + ctx.getPosition().add(dir).toString());
                dir = new Direction(-dir.x, -dir.y);
            }
        } catch (Exception e) {
            // do something here to deal with errors
            ctx.debugOut("EXPLAIN?????? " + e.toString());
            dir = new Direction(1, 1);
        }

        ctx.debugOut("Moving to " + ctx.getPosition().add(dir).toString() + ctx.getStateString());
        return dir;
    }

    @Override
    public Action getAction() {

        ctx.debugOut("Action requested," + ctx.getStateString());

        // catch any shenanigans
        if (ctx.getEnergy() > 100) {
            try {
                View view = ctx.getView((int) ctx.getTech());
                if (view.getAliensAtPos(ctx.getPosition()).size() > 1) {
                    ctx.debugOut("Uh-oh.There is someone else here."
                            + ctx.getStateString());
                }

                // do we have enough energy?
                if (ctx.getEnergy() < 10) {
                    // no, charge
                    ctx.debugOut("Choosing to gain energy,"
                            + ctx.getStateString());
                    return new Action(Action.ActionCode.Gain);
                }

                // do we have enough tech?
                if (ctx.getTech() < 30 && ctx.getEnergy() > ctx.getTech()) {
                    // no, research
                    ctx.debugOut("Choosing to research"
                            + ctx.getStateString());
                    return new Action(Action.ActionCode.Research);
                }

                // is there another alien on our position?
                if (view.getAliensAtPos(ctx.getPosition()).size() > 1
                        && ctx.getEnergy() > ctx.getFightingCost() + 2) {
                    ctx.debugOut("EXTERMINATE!!!!!"
                            + ctx.getStateString());

                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 2 - ctx.getFightingCost());
                }

                if (ctx.getEnergy() > (ctx.getSpawningCost() + 10)) {
                    // no other aliens here, have enough stuff, spawn!
                    ctx.debugOut("AAs RULE SUPREME! SPAWNING!"
                            + ctx.getStateString());

                    return new Action(Action.ActionCode.Spawn, 5);
                }
            } catch (Exception e) {
                // do something here to deal with errors
                ctx.debugOut("EXPLAIN?????? " + e.toString());
            }
        }
        ctx.debugOut("Gaining energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {
        if (ctx.getEnergy() > 100) {
            try {
                if (ctx.getGameTurn() % 20 == 0) {
                    ctx.broadcastAndListen("I say: AAs rule supreme!!!!!", 1, true);
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
    public void processResults() {
    }

}
