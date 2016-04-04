/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.Random;

/**
 *
 * @author guberti
 */
public class AlienContainer {

    public final String alienPackageName;
    public final String alienClassName;
    public final Constructor<?> alienConstructor;
    public Alien alien;
    public final ContextImplementation api;

    int tech;
    int energy;

    boolean fought;
    public int x;
    public int y;
    public boolean action; // Whether the alien has performed an action this turn
    
    static Random rand = new Random(System.currentTimeMillis());

    // Declare stats here
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(GameVisualizer vis, int x, int y, String alienPackageName, String alienClassName, Constructor<?> cns, int energy, int tech) {
        Alien a;

        this.alienPackageName = alienPackageName;
        this.alienClassName = alienClassName;
        this.alienConstructor = cns;
        this.energy = energy;
        this.tech = tech;
        this.api = new ContextImplementation(this, vis);

        // if position = (0,0) assign random position
        if (x == 0 && y == 0) {
            
            this.x = rand.nextInt(20) - 10; // TODO: get these hardcoded constants from spacegrid instead
            this.y = rand.nextInt(20) - 10;
        } else {
            this.x = x;
            this.y = y;
        }
        
        
        
        // construct and initialize alien

        try {
            a = (Alien) cns.newInstance();
            this.alien = a;
            a.init(this.api);
        } catch (Throwable t) {
            this.alien = null;
            t.printStackTrace();
        }

    }

    public void move(ViewImplementation view) throws NotEnoughTechException {
        // Whether the move goes off the board will be determined by the grid
        api.view = view;
        MoveDir direction = alien.getMove();
        checkMove(direction); // Throws an exception if illegal
        int oldx = x;
        int oldy = y;
        x += direction.x();
        y += direction.y();
        
        // debug code following
        if (x > 1000 || y > 1000 || x < -1000 || y < -1000) {
            debugOut("Moving out of control");
        }
        
        api.vis.showMove(alienPackageName, alienClassName, this.hashCode(), oldx, oldy, x, y, energy, tech);
    }

    public void kill() {
        //energy = Integer.MIN_VALUE; // GM: Gavin, this strikes me as a bug ...
        energy = 0;

    }

    public Action getAction(ViewImplementation view) throws NotEnoughEnergyException, UnknownActionException {
        api.view = view;
        Action action = alien.getAction();
        switch (action.code) {
            case None:
            case Gain:
                return action;
            case Research:
                if (tech >= energy) { // If the tech can't be researched due to lack of energy
                    debugOut("AC: Research with T:" + Integer.toString(tech) + " and E:" + Integer.toString(energy));
                    throw new NotEnoughEnergyException();
                }
                // return new Action(ActionCode.Research, tech); // GM: This seems like a mistake
                return action;

            case Spawn:
                if (action.power + 3 > energy) {
                    throw new NotEnoughEnergyException();
                }
                return action;
            case Fight:
                //TODO: Perform necessary checks
                return action;

            default:
                debugOut("AC: Checking action, unknown: " + action.code.toString());
                throw new UnknownActionException();
        }
    }

    public int fight(int fightPower) throws NotEnoughEnergyException, NotEnoughTechException {
        //int fightPower = 0; //alien.getFightPower(api); GM need to fix this up after reconciling fighting into Action

        // If the alien cannot fight with the amount of energy it wants
        // Throw the appropriate exception
        if (fightPower > energy) {
            throw new NotEnoughEnergyException();
        }
        if (fightPower > tech) {
            throw new NotEnoughTechException();
        }

        // If the move is valid, subtract the energy expended
        energy -= fightPower;
        energy = Integer.max(energy, 0); // let's not drop below zero.

        // Return how much power the alien will fight with
        return fightPower;
    }

    private void checkMove(MoveDir direction) throws NotEnoughTechException {
        // for immediate vicinity, just let this go
        if (Math.abs((long)direction.x()) + Math.abs((long)direction.y())
                <= 2) {
            return;
        }
        // If the move is farther than the alien has the tech to move
        // distance |x|+|y|, easier to program.
        if (Math.abs((long)direction.x()) + Math.abs((long)
                direction.y())
                > (long) tech) {
            debugErr("Illegal move: " + Integer.toString(direction.x()) + "," + Integer.toString(direction.y()));
            throw new NotEnoughTechException();
        }
    }
    
    // this debugOut is not sensitive to chatter control
    public void debugOut(String s) {
            api.vis.debugOut("Alien "+alienPackageName + ":" + alienClassName + "(" 
                +Integer.toHexString(alien.hashCode()).toUpperCase()+"): " + s);
    }

    public void debugErr(String s) {
            api.vis.debugErr("Alien "+alienPackageName + ":" + alienClassName + "(" 
                +Integer.toHexString(alien.hashCode()).toUpperCase()+"): " + s);
    }
}

class NotEnoughEnergyException extends Exception {
}

class NotEnoughTechException extends Exception {
}

class UnknownActionException extends Exception {
}


