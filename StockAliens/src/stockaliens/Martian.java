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
    int HorizontalMove;
    int VerticalMove;
    int Remainder;
    int fightStrength;
    //gets a random boolean to determine positive or negative move function.
    private static Random rnd = new Random();

    public static boolean getRandomBoolean() {
        return rnd.nextBoolean();
    }

    //empty constructor
    public Martian() {
    }

    @Override
    public void init(Context ctx) {
        this.ctx = ctx;
        ctx.debugOut("Initialized at "
                + "(" + Integer.toString(ctx.getX())
                + "," + Integer.toString(ctx.getY()) + ")"
                + " E: " + Integer.toString(ctx.getEnergy())
                + " T: " + Integer.toString(ctx.getTech()));
        ctx.debugOut("Pancakes taste like styrofoam");
        
    }

    public MoveDir getMove() {
    //    ctx.debugOut("Move requested,"
    //            + " E:" + Integer.toString(ctx.getEnergy())
    //            + " T:" + Integer.toString(ctx.getTech()));


        //splits in two whole numbers
        Remainder = ctx.getTech() % 2;
        HorizontalMove = (ctx.getTech() - Remainder) / 2;
        VerticalMove = ctx.getTech() - HorizontalMove;
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
        if ((long)Math.abs(ClosestAlienXCoordinate - ctx.getX()) + (long)Math.abs(ClosestAlienYCoordinate - ctx.getY()) <= (long)ctx.getEnergy()) {

            HorizontalMove = ClosestAlienXCoordinate - ctx.getX();
            VerticalMove = ClosestAlienYCoordinate - ctx.getY();
        }

        //sets the amount of energy to fight with by how much energy, and how muc technology
        fightStrength = 1;
        if (ctx.getEnergy() > 3) {
            fightStrength = ctx.getEnergy() - 2;
            if (ctx.getEnergy() > ctx.getTech()) {
                fightStrength = ctx.getTech();
            }

        }

        ctx.debugOut("Moving ("+ Integer.toString(HorizontalMove) + "," + Integer.toString(VerticalMove) + ")");

        return new MoveDir(HorizontalMove, VerticalMove);
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
            return new Action(ActionCode.Fight, ctx.getEnergy());
        }
        //if it doesnt fight, it chooses a item to do depending on how much energy it has.
        if (ctx.getEnergy() < 2) {
            ctx.debugOut("Gaining");
            return new Action(ActionCode.Gain);
        } else if (ctx.getEnergy() < 3) {
            ctx.debugOut("Researching");
            return new Action(ActionCode.Research);
        } else {
            ctx.debugOut("Spawning");
            return new Action(ActionCode.Spawn);
        }

    }

    @Override
    public void processResults() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
