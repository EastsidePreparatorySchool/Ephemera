/*
 * 
 */
package stockaliens;

import alieninterfaces.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void init(Context ctx, int id, int parent, String message) {
        this.ctx = ctx;
        ctx.debugOut("Initialized at "
                + ctx.getStateString());
        ctx.debugOut("Pancakes taste like styrofoam");

    }

    public Direction getMove() {
        //    ctx.debugOut("Move requested,"
        //            + " E:" + Integer.toString(ctx.getEnergy())
        //            + " T:" + Integer.toString(ctx.getTech()));

        //splits in two whole numbers
        Remainder = (int) ctx.getTech() % 2;
        HorizontalMove = (int) (ctx.getTech() - Remainder) / 2;
        VerticalMove = (int) ctx.getTech() - HorizontalMove;
        if (getRandomBoolean() == true) {
            HorizontalMove *= -1;
        }
        if (getRandomBoolean() == true) {
            VerticalMove *= -1;
        }

        //gets the coordinates of the closest alien.
        Position ClosestAlien = new Position(5, 5);

        try {
            List<AlienSpecies> l = ctx.getView((int) ctx.getTech()).getClosestXenos(
                    new AlienSpecies("eastsideprep.org", "stockaliens", "Martian", 0));
            if (l.size() > 1) {
                ClosestAlien = l.get(0).position;
            }
        } catch (Exception e) {
            ClosestAlien = new Position(5, 5);
        }

        if (ClosestAlien == null) {
            ClosestAlien = new Position(5, 5);

        }

        //Checks to see if the closest alien is withen moving capability.
        if (ctx.getDistance(ClosestAlien, ctx.getPosition()) <= (long) ctx.getTech()) {

            HorizontalMove = ClosestAlien.x - ctx.getPosition().x;
            VerticalMove = ClosestAlien.y - ctx.getPosition().y;
        }

        //sets the amount of energy to fight with by how much energy, and how muc technology
        fightStrength = 1;
        if (ctx.getEnergy() > 3 + ctx.getFightingCost()) {
            fightStrength = (int) ctx.getEnergy() - ctx.getFightingCost() - 2;
            if (fightStrength > ctx.getTech()) {
                fightStrength = (int) ctx.getTech();
            }

        }

        // move at least 1 
        if (HorizontalMove == 0 && VerticalMove == 0) {
            VerticalMove = 1;
        }
        //but don't move into star
        try {
            if (ctx.getView(2).getSpaceObjectAtPos(ctx.getPosition().add(new Direction((int) HorizontalMove, (int) VerticalMove))) != null) {
                VerticalMove -= VerticalMove > 0 ? 1 : -1;
            }
        } catch (NotEnoughEnergyException | NotEnoughTechException | View.CantSeeSquareException ex) {
        }

        //ctx.debugOut("Moving ("+ Integer.toString(HorizontalMove) + "," + Integer.toString(VerticalMove) + ")");
        return new Direction((int) HorizontalMove, (int) VerticalMove);
    }

    public Action getAction() {
        //ctx.debugOut("Action requested,"
        //        + " E:" + Integer.toString(ctx.getEnergy())
        //        + " T:" + Integer.toString(ctx.getTech()));

        //checks if alien is on the same position, if so, then fights with the priorly designated amount of energy
        try {
            if (ctx.getView((int) ctx.getTech()).getAliensAtPos(ctx.getPosition()).size() > 1) {
                ctx.debugOut("Fighting");
                return new Action(Action.ActionCode.Fight, (fightStrength));
            }
        } catch (Exception e) {
            ctx.debugOut("Fighting");
            return new Action(Action.ActionCode.Fight, fightStrength);
        }
        //if it doesnt fight, it chooses a item to do depending on how much energy it has.
        if (ctx.getEnergy() < 2) {
            ctx.debugOut("Gaining");
            return new Action(Action.ActionCode.Gain);
        } else if (ctx.getEnergy() < 3 && ctx.getEnergy() > ctx.getTech() && ctx.getTech() < 30) {
            ctx.debugOut("Researching");
            return new Action(Action.ActionCode.Research);
        } else if (ctx.getEnergy() > ctx.getSpawningCost() + 2) {
            ctx.debugOut("Spawning");
            return new Action(Action.ActionCode.Spawn, 2);
        }

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
