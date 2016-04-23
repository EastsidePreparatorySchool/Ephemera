/*
 * 
 */
package stockaliens;

import java.util.Random;
import alieninterfaces.*;

/**
 *
 * @author Sam Freisem-Kirov
 */
public class Martian implements Alien {

    //declaring everything that will need to be used later
    Context ctx;
    long HorizontalMove;
    long VerticalMove;
    int Remainder;
    int fightStrength;
    //gets a random boolean to determine positive or negative move function.

    public boolean getRandomBoolean() {
        return (ctx.getRandomInt(2) == 0);
    }

    //empty constructor
    public Martian() {
    }

    @Override
    public void init(Context ctx,int id, int parent, String message) {
        this.ctx = ctx;
        ctx.debugOut("Initialized at "
                + "(" + Double.toString(ctx.getX())
                + "," + Double.toString(ctx.getY()) + ")"
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));
        ctx.debugOut("Pancakes taste like styrofoam");

    }

    public MoveDir getMove() {
    //    ctx.debugOut("Move requested,"
        //            + " E:" + Integer.toString(ctx.getEnergy())
        //            + " T:" + Integer.toString(ctx.getTech()));

        //splits in two whole numbers
        Remainder = (int)ctx.getTech() % 2;
        HorizontalMove = (int)(ctx.getTech() - Remainder) / 2;
        VerticalMove = (int)ctx.getTech() - HorizontalMove;
        if (getRandomBoolean() == true) {
            HorizontalMove *= -1;
        }
        if (getRandomBoolean() == true) {
            VerticalMove *= -1;
        }

        //gets the coordinates of the closest alien.
        int ClosestAlienXCoordinate;
        int ClosestAlienYCoordinate;
        ClosestAlienXCoordinate = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY())[0];
        ClosestAlienYCoordinate = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY())[1];

        //Checks to see if the closest alien is withen moving capability.
        if ((long) Math.abs((long)ClosestAlienXCoordinate - ctx.getX()) + (long) Math.abs((long)ClosestAlienYCoordinate - ctx.getY()) <= (long) ctx.getTech()) {

            HorizontalMove = ClosestAlienXCoordinate - ctx.getX();
            VerticalMove = ClosestAlienYCoordinate - ctx.getY();
        }

        //sets the amount of energy to fight with by how much energy, and how muc technology
        fightStrength = 1;
        if (ctx.getEnergy() > 3) {
            fightStrength = (int)ctx.getEnergy() - 2;
            if (ctx.getEnergy() > ctx.getTech()) {
                fightStrength = (int)ctx.getTech();
            }

        }

        //ctx.debugOut("Moving ("+ Integer.toString(HorizontalMove) + "," + Integer.toString(VerticalMove) + ")");
        return new MoveDir((int)HorizontalMove, (int)VerticalMove);
    }

    public Action getAction() {
        //ctx.debugOut("Action requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));

        //checks if alien is on the same position, if so, then fights with the priorly designated amount of energy
        try {
            if (ctx.getView().getAlienCountAtPos(ctx.getX(), ctx.getY()) > 1) {
                ctx.debugOut("Fighting");
                return new Action(ActionCode.Fight, (fightStrength));
            }
        } catch (Exception e) {
            ctx.debugOut("Fighting");
            return new Action(ActionCode.Fight, (int)ctx.getEnergy()-1);
        }
        //if it doesnt fight, it chooses a item to do depending on how much energy it has.
        if (ctx.getEnergy() < 2) {
            ctx.debugOut("Gaining");
            return new Action(ActionCode.Gain);
        } else if (ctx.getEnergy() < 3 && ctx.getEnergy() > ctx.getTech() && ctx.getTech() < 30) {
            ctx.debugOut("Researching");
            return new Action(ActionCode.Research);
        } else if (ctx.getEnergy() > ctx.getSpawningCost() + 2) {
            ctx.debugOut("Spawning");
            return new Action(ActionCode.Spawn, 2);
        } 
        
        return new Action (ActionCode.Gain);
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
