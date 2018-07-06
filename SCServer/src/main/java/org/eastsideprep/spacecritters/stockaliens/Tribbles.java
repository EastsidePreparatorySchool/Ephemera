/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.stockaliens;

import org.eastsideprep.spacecritters.alieninterfaces.*;

/**
 *
 * @author thaeger
 */
public class Tribbles implements Alien {

    int myID = 0;
    Context ctx;
    int alienCount = 0;
    int distance = 500;
    int x = 0;
    int y = 0;
    int r = 25;

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.myID = id;
        this.ctx = ctx;
    }

    @Override
    public void communicate() {

    }

    @Override
    public void receive(String[] messages) {

    }

    @Override
    public Direction getMove() {
        //find distance between here and corner
        //find nearest star/planet for avoidance

        if (alienCount < 5000) {
            distance = (int) ctx.getDistance(ctx.getPosition(), ctx.getMinPosition().v2());
            if (distance > r) {
                distance = (int) ctx.getDistance(ctx.getPosition(), ctx.getMinPosition().v2());
                ctx.debugOut(Integer.toString(distance));
                return new Direction(-1, -1);
            }
            if (distance <= r) {
                distance = (int) ctx.getDistance(ctx.getPosition(), ctx.getMinPosition().v2());
                x = ctx.getRandomInt(3) - 1;
                y = ctx.getRandomInt(3) - 1;
                return new Direction(x, y);
            }
            return new Direction(0, 0);
        } else {
            x = ctx.getRandomInt(3) - 1;
            y = ctx.getRandomInt(3) - 1;
            return new Direction(x, y);
        }
    }

    @Override
    public Action getAction() {
        if (distance < r + 5 && ctx.getEnergy() > ctx.getSpawningCost() + 11) {
            alienCount++;
            ctx.debugOut(Double.toString(ctx.getEnergy()));
            return new Action(Action.ActionCode.Spawn, 10);
        } else {
            if (ctx.getEnergy() < ctx.getTech() + 5) {
                return new Action(Action.ActionCode.Gain);
            } else {
                return new Action(Action.ActionCode.Research);
            }
        }
    }

    @Override
    public void processResults() {
    }
}
