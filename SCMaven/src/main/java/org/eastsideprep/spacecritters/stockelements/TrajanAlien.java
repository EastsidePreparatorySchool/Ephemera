package org.eastsideprep.spacecritters.stockelements;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author tespelien
 */
import org.eastsideprep.spacecritters.alieninterfaces.*;

public class TrajanAlien implements Alien, AlienComplex {

    /* strategy:
    run from aliens unless they are at the same position as we are 
    if they share our spot we fight hard
       
    spawn a lot when we are far away from all aliens
    
     */
    ContextComplex ctx;

    public TrajanAlien() {}

    @Override
    public void initComplex(ContextComplex ctx, int id, int parentId, String message) {
        this.ctx = ctx;
    }

    @Override
    public Action getAction() {
        View view = null;
        //looks at far as possible
        try {
            view = ctx.getView((int) ctx.getTech());
        } catch (Exception e) {
            ctx.debugOut("Could not see that far");
        }

        try {
            //no energy means i cant do anything
            if (ctx.getEnergy() < 18) {
                return new Action(Action.ActionCode.Gain);
            }

            // is there another alien on our position?
            if (view.getAliensAtPos(ctx.getPosition()).size() > 0) {
                // if so, do we have any energy?
                if (ctx.getEnergy() < 7) {
                    //get more to fight
                    return new Action(Action.ActionCode.Gain);
                } else {
                    //otherwise use almost all energy fighting
                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 7);
                }

            } else {
                //if no one is there, research 1/3 of the time and get energy 2/3 of the time
                if (ctx.getRandomInt(3) > 1) {
                    return new Action(Action.ActionCode.Research);
                }
                return new Action(Action.ActionCode.Gain);
            }
        } catch (Exception e) {
        }

        /*try {
            List<AlienSpecies> inRangeAliens = ctx.getView((int) ctx.getTech()).getClosestXenos(
                    new AlienSpecies("ephemera.eastsideprep.org", "stockelements", "Alf", 0));

            Position nearest = inRangeAliens.get(0).position;
            int nearestDistX = Math.abs((nearest.x - ctx.getPosition().x));
            int nearestDistY = Math.abs((nearest.y - ctx.getPosition().y));

            //if everyone else is far away, gain energy
            if (nearestDistX > 60 && nearestDistY > 60) {
                return new Action(Action.ActionCode.Gain);
            }

        } catch (Exception e) {
        }*/
        //if nothing else is done get energy
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //unused methods
    @Override
    public void communicate() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] strings) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WorldVector getAccelerate() {

        //if i have no energy i cant move anywhere, so i
        //just stay in my orbit and collect energy
        if (ctx.getEnergy() < 18) {
            return new WorldVector(0, 0);
        }

        //goal is to get to a specific orbit that passes through this point
        //by using a hohmann transfer (http://www.instructables.com/id/Calculating-a-Hohmann-Transfer/)
        //to jump from one orbit to a larger one that contains a specific point
        double targetX = ctx.getRandomInt(20) + 10;
        double targetY = ctx.getRandomInt(20) + 10;
        Position target = new Position(targetX, targetY);

        //the sub 1's represent the origin's value
        //the sub 2's represent the target's value
        double r1 = Math.sqrt(ctx.getPosition().x * ctx.getPosition().x + ctx.getPosition().y * ctx.getPosition().y);
        double r2 = Math.sqrt(targetX * targetX + targetY * targetY);

        //period of the orbits, seems like 3 turns and it goes around once 
        double p1 = 3;
        double p2 = 3;
        double gm = 1.327 * Math.pow(10, 11);

        //semi major axis and period of the elliptical transfer orbit
        double a = (r1 + r2) / 2;
        double p = Math.sqrt(4 * Math.PI * Math.PI * a * a * a / gm);

        //velocities of my orbit and target
        double v1 = (Math.PI * r1) / p1;
        double v2 = (Math.PI * r2) / p2;

        //velocity at the perihelion, where i need to speed up to get out of my orbit
        //to enter the elliptical orbit that will take me to the target
        double vPer = (2 * Math.PI * a / p) * Math.sqrt(2 * a / r1 - 1);

        //however, i need the velocity i am adding to my current 
        //velocity to be able to get to my target orbit, so i subtract        
        double vec1 = vPer - v1;

        //i also need to exit this orbit to enter my target orbit at the aphelion
        double vAph = (2 * Math.PI * a / p) * Math.sqrt(2 * a / r2 - 1);
        //again, i need the change
        double vec2 = v2 - vAph;

        //but i only need to change my velocity twice, otherwise stay in orbit and spawn aliens
        int counter = 1;
        if (counter == 1) {
            counter++;
            return new WorldVector(vec1, 0);
        }
        if (counter == 2) {
            counter++;
            return new WorldVector(vec2, 0);
        }
        return new WorldVector(0, 0);
    }

    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
