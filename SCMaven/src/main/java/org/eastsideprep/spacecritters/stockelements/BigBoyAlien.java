/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eastsideprep.spacecritters.stockelements;

import org.eastsideprep.spacecritters.alieninterfaces.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author flucco
 */
public class BigBoyAlien implements Alien {

    Context cntxt;
    int id;
    int numSpawned = 0;
    Boolean boy = false;

    @Override
    public void init(Context cntxt, int i, int i1, String string) {
        this.cntxt = cntxt;
        this.id = i;
    }

    @Override
    public void communicate() {
        try {
            cntxt.broadcastAndListen("TO ALL MY FELLOW ALIENS, from " + id, 0, true);
        } catch (NotEnoughTechException | NotEnoughEnergyException ex) {
        }
    }

    @Override
    public void receive(String[] messages) {
        for (String s : messages) {
            cntxt.debugOut(s);
        }
    }

    @Override
    public Direction getMove() {
        int x = 0;
        int y = 0;
       
        try {
            if (cntxt.getView(2).getSpaceObjectAtPos(cntxt.getPosition().add(new Direction((int) x, (int) y))) != null) {
                x = 0;
                y = 0;
            }
        } catch (NotEnoughEnergyException | NotEnoughTechException | View.CantSeeSquareException ex) {
            cntxt.debugOut("i caught an exception, whee!");
        }

        IntegerPosition ClosestAlien = new IntegerPosition(5, 5);
        IntegerPosition NextClosestAlien = new IntegerPosition(5, 5);
        try {
            List<AlienSpecies> l = cntxt.getView((int) cntxt.getTech()).getClosestSpecificAliens(null); // changed to "null", I know I told you otherwise, but this is more efficient
            if (l != null && l.size() > 1) {
                ClosestAlien = l.get(1).position;
                cntxt.debugOut("seeing a son at " + ClosestAlien);
                boy = true;
            }
            if (l != null && l.size() > 2) {
                NextClosestAlien = l.get(2).position;
                cntxt.debugOut("there are multiple boys!");
            }
        } catch (NotEnoughEnergyException | NotEnoughTechException e) { 
            //ClosestAlien = new Position (5, 5);
            //change to wherever spawn point is
        }

        if (ClosestAlien == null) {
            ClosestAlien = new IntegerPosition (5, 5);

        }
        cntxt.debugOut("the nearest boy is " + ClosestAlien + " and the second nearest boy is" + NextClosestAlien);

            if (cntxt.getEnergy() > (ClosestAlien.y - cntxt.getPosition().y + ClosestAlien.x - cntxt.getPosition().x) + 5
                    && cntxt.getTech() > (Math.abs(ClosestAlien.y - cntxt.getPosition().y) + Math.abs(ClosestAlien.x - cntxt.getPosition().x)) + 7 
                    && boy == true) {
                Double Energy = cntxt.getEnergy();
                int Tech = cntxt.getTech();
                cntxt.debugOut("necessary tech is:" + ((Math.abs(ClosestAlien.y - cntxt.getPosition().y) + Math.abs(ClosestAlien.x - cntxt.getPosition().x)) + 7));
                
                if (cntxt.getDistance(cntxt.getIntegerPosition(), ClosestAlien) < 3) {
                    cntxt.debugOut("moving away");
                    return new Direction(ClosestAlien.y - cntxt.getPosition().y - 3, ClosestAlien.x - cntxt.getPosition().x - 3);
                }
                if (cntxt.getDistance(cntxt.getIntegerPosition(), NextClosestAlien) > 3) {
                    cntxt.debugOut("moving closer!!");
                    return new Direction(NextClosestAlien.y - cntxt.getPosition().y - 3, NextClosestAlien.x - cntxt.getPosition().x - 3);
                }
            }

       cntxt.debugOut("not going anywhere, frances, no matter how much you beg");
        return new Direction(x, y);
    }

    @Override
    public Action getAction() {
        

        if (cntxt.getEnergy() < 100) {
            cntxt.debugOut("Gaining" + cntxt.getStateString());
            return new Action(Action.ActionCode.Gain);
        }

        if (cntxt.getTech() < 30) {
            cntxt.debugOut("Researching" + cntxt.getStateString());
            return new Action(Action.ActionCode.Research);
        }
        if (boy == false) {
        if (cntxt.getEnergy() > cntxt.getSpawningCost() + 20) {
            cntxt.debugOut("Spawning");
            numSpawned++;
            return new Action(Action.ActionCode.Spawn, 5);
        }
        }
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
