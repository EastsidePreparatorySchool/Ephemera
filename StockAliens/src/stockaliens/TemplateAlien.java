/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.Action;
import alieninterfaces.ActionCode;
import alieninterfaces.Alien;
import alieninterfaces.Context;
import alieninterfaces.MoveDir;
import alieninterfaces.View;

/**
 *
 * @author jbriggs
 */
public class TemplateAlien implements Alien {

    Context ctx;
    int id;

    public TemplateAlien() {
    }

    public void init(Context game_ctx, int id, int parent, String message) {
        this.id = id;

        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    @Override
    public void communicate() {
        if (ctx.getEnergy() > 10) {
            try {
                if (ctx.getGameTurn() % 20 == 0) { // do this only every 20 turns
                    ctx.broadcastAndListen("Test message", 1, true); // send message, and listen for others
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

    public MoveDir getMove() {

        //ctx.debugOut("Move requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));
        int techLevel = (int)ctx.getTech();
        int energy = (int)ctx.getEnergy();
        View view = ctx.getView();
        int[] nearestAlienPos = view.getClosestAlienPos(ctx.getX(), ctx.getY());

        return new MoveDir(1, 1);
    }

    public Action getAction() {
        View view = ctx.getView();

        try {
            // is there another alien on our position?
            if (view.getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("Others are here");
            }
        } catch (Exception e) {
            // do something here to deal with errors
        }

        //
        // if we have spare energy, research tech
        if (ctx.getEnergy() > (ctx.getTech() + 5)) {
            if (ctx.getTech() < 30) {
                ctx.debugOut("Researching");
                return new Action(ActionCode.Research);
            }
        }
        ctx.debugOut("Gaining");

        return new Action(ActionCode.Gain);
    }

}
