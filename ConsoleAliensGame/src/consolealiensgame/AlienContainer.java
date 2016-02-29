/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consolealiensgame;

import alieninterfaces.*;
import java.lang.reflect.Constructor;


/**
 *
 * @author guberti
 */
public class AlienContainer {
    public final String alienPackageName;
    public final String alienClassName;
    public final Constructor<?> alienConstructor;
    private final Alien alien;
    private final AlienAPI api;
    
    int tech;
    int energy;

    boolean fought;
    public int x;
    public int y;
    public boolean action; // Whether the alien has performed an action this turn
    
    // Declare stats here
    
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(int x, int y, String alienPackageName, String alienClassName, Constructor<?> cns, int energy, int tech) {
        Alien a;
        
        this.alienPackageName = alienPackageName;
        this.alienClassName = alienClassName;
        this.alienConstructor = cns;
        this.energy = energy;
        this.tech = tech;
        this.api = new AlienAPI(this);
        
        // construct and initialize alien
        
        try
        {
            a = (Alien) cns.newInstance();
            a.init(this.api);
        } catch (Throwable t)
        {
            a = null;
            t.printStackTrace();
        }
        
        this.alien = a;
    }
    
    public void move(ViewImplementation view) throws NotEnoughTechException {
        // Whether the move goes off the board will be determined by the grid
        api.view = view;
        MoveDir direction = alien.getMove();
        checkMove(direction); // Throws an exception if illegal
        x += direction.x();
        y += direction.y();
    }
    
    public void kill() {
        energy = Integer.MIN_VALUE;
    }
    
    public Action getAction(ViewImplementation view) throws NotEnoughEnergyException, UnknownActionException {
        api.view = view;
        Action action = alien.getAction();
        switch (action.code) {
            case None: 
            case Gain:
                return new Action (action.code);
            case Research: 
                if (tech > energy) { // If the tech can't be researched due to lack of energy
                    throw new NotEnoughEnergyException();
                }
                // Otherwise
                return new Action (ActionCode.Research, tech);
            case Spawn: 
                if (action.power + 3 > energy) {
                    throw new NotEnoughEnergyException();
                }
                return action;
            default:
                throw new UnknownActionException();
        }
    }
    
    public int fight() throws NotEnoughEnergyException, NotEnoughTechException {
        int fightPower = 0; //alien.getFightPower(api); GM need to fix this up after reconciling fighting into Action
        
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
        
        // Return how much power the alien will fight with
        return fightPower;
    }
    
    private void checkMove(MoveDir direction) throws NotEnoughTechException {
        // If the move is farther than the alien has the tech to move
        if (Math.pow(direction.x(), 2) + Math.pow(direction.y(), 2)
                > Math.pow(tech, 2)) {
            throw new NotEnoughTechException();
        }
    }
}

class NotEnoughEnergyException extends Exception {}
class NotEnoughTechException extends Exception {}
class UnknownActionException extends Exception {}