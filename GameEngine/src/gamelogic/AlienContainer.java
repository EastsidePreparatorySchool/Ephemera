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
    boolean chatter;

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
        this.chatter = false;
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

        MoveDir direction = alien.getMove();
        this.checkMove(direction); // Throws an exception if illegal

        direction = this.applyDrift(x, y, direction);

        int oldx = x;
        int oldy = y;
        x += direction.x();
        y += direction.y();

        api.vis.showMove(alienPackageName, alienClassName, this.hashCode(), oldx, oldy, x, y, energy, tech);
    }

    // this calculates inward drift based on location
    // keeps aliens within 250 spaces of Earth
    public MoveDir applyDrift(int x, int y, MoveDir dir) {
        double r;
        double alpha;
        double deltaAlpha;
        double deltaR;
        double dx;
        double dy;
        double reduction;
        int oldx, oldy;

        oldx = dir.x();
        oldy = dir.y();

        //distance from Earth
        r = Math.sqrt(Math.pow((double) x, 2) + Math.pow((double) y, 2));
        alpha = Math.atan2(x, y);

        // no action under r = 10
        if (r < 20) {
            return dir;
        }

        // wormhole for escapees
        if (r > 250) {
            // return these people to Earth
            this.debugOut("Tunneled back to Earth");
            return new MoveDir(0 - x, 0 - y);
        }

        // get angle of proposed move
        deltaAlpha = Math.atan2(dir.x(), dir.y());
        // no action if inward
        if (getAngleDiff(deltaAlpha, alpha) >= Math.PI / 2 && getAngleDiff(deltaAlpha, alpha) <= 3 * Math.PI / 2) {
            return dir;
        }

        // and magnitude
        deltaR = Math.sqrt(Math.pow(dir.x(), 2) + Math.pow(dir.y(), 2)) + 1;

        /* 
         //Approach 1:
         // Apply gentle inward force
         // cubic function of how close to border, modulated by angle
         reduction = (1/Math.pow((250 - r), 3)) * (Math.abs(Math.abs(alpha - deltaAlpha) / Math.PI - 1)) + 1;

         if (reduction > 5) {
         //api.vis.debugOut("r: " + Double.toString(r));
         //api.vis.debugOut(Double.toString(Math.abs(Math.abs(alpha - deltaAlpha) / Math.PI - 1)));
         //api.vis.debugOut("Reduction: " + Double.toString(reduction));
         }
         deltaR /= reduction;
         */
        // Approach 2:
        // Make the new move ever more tangential as the alien gets closer to the rim
        api.vis.debugOut("Drift: r              " + Double.toString(r));
        api.vis.debugOut("Drift: alpha          " + Double.toString(getAngleDiff(alpha, 0)));
        api.vis.debugOut("Drift: deltaAlpha     " + Double.toString(getAngleDiff(deltaAlpha, 0)));
        api.vis.debugOut("Drift: diff           " + Double.toString(getAngleDiff(deltaAlpha, alpha)));

        if (getAngleDiff(deltaAlpha, alpha) < Math.PI / 2) {
            // alien bearing slightly left, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        } else if (getAngleDiff(deltaAlpha, alpha) > 3 * Math.PI / 2) {
            // alien bearing slightly right, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        }
        deltaAlpha = getAngleDiff(deltaAlpha, 0);
        api.vis.debugOut("Drift: new deltaAlpha " + Double.toString(deltaAlpha));

        //put the x,y back together
        dx = deltaR * Math.cos(deltaAlpha);
        dy = deltaR * Math.sin(deltaAlpha);

        api.vis.debugOut("Drift: ("
                + Integer.toString(oldy) + ","
                + Integer.toString(oldx) + ") -> ("
                + Integer.toString((int) Math.round(dx)) + ","
                + Integer.toString((int) Math.round(dy)) + ")");

        return new MoveDir((int) Math.round(dx), (int) Math.round(dy));
    }

    // normalizes angle difference to fit in [0:2pi[
    public double getAngleDiff(double alpha, double beta) {
        alpha = alpha < 0 ? alpha + (Math.PI * 2) : alpha;
        beta = beta < 0 ? beta + (Math.PI * 2) : beta;
        double gamma = alpha - beta;
        gamma = gamma >= Math.PI * 2 ? gamma - (Math.PI * 2) : gamma;
        gamma = gamma <= 0 ? gamma + (Math.PI * 2) : gamma;
        return gamma;
    }

    public void kill() {
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
                if (action.power + api.getSpawningCost() > energy) {
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
        if (Math.abs((long) direction.x()) + Math.abs((long) direction.y())
                <= 2) {
            return;
        }
        // If the move is farther than the alien has the tech to move
        // distance |x|+|y|, easier to program.
        if (Math.abs((long) direction.x()) + Math.abs((long) direction.y())
                > (long) tech) {
            debugErr("Illegal move: " + Integer.toString(direction.x()) + "," + Integer.toString(direction.y()));
            throw new NotEnoughTechException();
        }
    }

    // this debugOut is not sensitive to chatter control
    public void debugOut(String s) {
        if (chatter) {
            api.vis.debugOut("Alien " + alienPackageName + ":" + alienClassName + "("
                    + Integer.toHexString(alien.hashCode()).toUpperCase() + "): " + s);
        }
    }

    public void debugErr(String s) {
        if (chatter) {
            api.vis.debugErr("Alien " + alienPackageName + ":" + alienClassName + "("
                    + Integer.toHexString(alien.hashCode()).toUpperCase() + "): " + s);
        }
    }
}

class NotEnoughEnergyException extends Exception {
}

class NotEnoughTechException extends Exception {
}

class UnknownActionException extends Exception {
}
