/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockaliens;

import alieninterfaces.*;
import java.util.Random;

/**
 *
 * @author abedi
 */
public class WeepingAngel implements Alien {

    //declaring everything that will need to be used later
    Context ctx;
    int HorizontalMove;
    int VerticalMove;
    int Remainder;
    int fightStrength;
    int turn = 0;
    //gets a random boolean to determine positive or negative move function.

    public  boolean getRandomBoolean() {
        return (ctx.getRandomInt(2) == 0);
    }

    //empty constructor
    public WeepingAngel() {
    }

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.ctx = ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));

    }

    public MoveDir getMove() {
        // doesn't move ever except to 1,1 where it shall stay
        return new MoveDir(0,0);
    }

    public Action getAction() {
        //ctx.debugOut("Action requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));

        View view = ctx.getView();

        // if there is someone at our position fight
        try {
           
            
            if (view.getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("Don't Blink!!!!!"
                        + " E:" + Double.toString(ctx.getEnergy())
                        + " T:" + Double.toString(ctx.getTech())
                        + " X:" + Double.toString(ctx.getX())
                        + " Y:" + Double.toString(ctx.getY()));

                return new Action(ActionCode.Fight, (int)ctx.getEnergy() - 2);
                
            }
            // always get energy
            if (ctx.getEnergy() < 25) {
                // no, charge
                ctx.debugOut("Choosing to gain Energy,"
                        + " E:" + Double.toString(ctx.getEnergy())
                        + " T:" + Double.toString(ctx.getTech()));
                return new Action(ActionCode.Gain);
            } 
           
             // do we have enough tech?
            if (ctx.getTech() < 20) {
                // no, research
                ctx.debugOut("Choosing to research"
                        + " E:" + Double.toString(ctx.getEnergy())
                        + " T:" + Double.toString(ctx.getTech()));
                return new Action(ActionCode.Research);
            }
            // every 
            turn = turn + 1;
            if (ctx.getEnergy()>=40 ) {
                ctx.debugOut("Spwaning"
                    + " E:" + Double.toString(ctx.getEnergy())
                    + " T:" + Double.toString(ctx.getTech()));

            return new Action(ActionCode.Spawn,(int)ctx.getEnergy()/2) ;
            }
            
        } catch (Exception e) {
        }
        
        // always gains energy
        ctx.debugOut("Weeping Angels: Must gain energy"
                + " E:" + Double.toString(ctx.getEnergy())
                + " T:" + Double.toString(ctx.getTech()));
        return new Action(ActionCode.Gain);
    }

    public void processResults() {
        ctx.debugOut("Processing results");
        return;
    }


    @Override
    public void communicate() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] messages) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
