/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.AlienSpec;
import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;
import static gamelogic.GridCircle.distance;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;
import static gamelogic.GridCircle.distance;

/**
 *
 * @author guberti
 */
public class AlienContainer {

    public static int currentID = 0;

    public final String domainName;
    public final String packageName;
    public final String className;
    public String fullName;
    public String speciesName;
    public AlienSpecies species;
    public final Constructor<?> constructor;
    public Alien alien;
    public final ContextImplementation ctx;
    public final SpaceGrid grid;
    public int alienHashCode;

    public Action.ActionCode currentActionCode;
    public double currentActionPower;
    public String currentActionMessage;
    public Action currentAction;
    public boolean listening;

    double tech;
    double energy;
    public static boolean chatter = false;

    boolean participatedInAction;
    public int x;
    public int y;
    public int nextX;
    public int nextY;
    public String outgoingMessage;
    public double outgoingPower;

    // Declare stats here
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(SpaceGrid sg, GameVisualizer vis, int x, int y,
            String alienDomainName, String alienPackageName, String alienClassName, Constructor<?> cns, AlienSpecies as,
            double energy, double tech, int parent, String message) throws InstantiationException {

        this.domainName = alienDomainName;
        this.packageName = alienPackageName;
        this.className = alienClassName;
        this.constructor = cns;
        this.species = as;
        this.energy = energy;
        this.tech = tech;
        this.ctx = new ContextImplementation(this, vis);
        this.grid = sg;
        this.listening = false;

        Alien a = null;

        // if position = (0,0) assign random position in safe zone
        if (x == 0 && y == 0) {
            this.x = ctx.getRandomInt(Constants.safeZoneRadius + 1);
            this.x *= (ctx.getRandomInt(2) == 0 ? 1 : -1);
            this.y = ctx.getRandomInt(Constants.safeZoneRadius + 1);
            this.y *= (ctx.getRandomInt(2) == 0 ? 1 : -1);
        } else {
            this.x = x;
            this.y = y;
        }

        this.alienHashCode = 0;

        // construct and initialize alien
        try {
            a = (Alien) cns.newInstance();
            this.alien = a;
            this.alienHashCode = ++currentID;
        } catch (Throwable t) {
            this.alien = null;
            debugOut("ac: Error constructing Alien");
            throw new InstantiationException();
        }

        try {
            a.init(this.ctx, alienHashCode, parent, message);
        } catch (UnsupportedOperationException e) {
            // let this go
        }

        fullName = this.getFullName();
        speciesName = this.getFullSpeciesName();

    }
    // class-related helpers

    public String getFullSpeciesName() {
        if (speciesName == null) {
            speciesName = domainName + ":" + packageName + ":" + className;
        }
        return speciesName;
    }

    public String getFullName() {
        if (fullName == null) {
            fullName = getFullSpeciesName() + "(" + Integer.toHexString(alien.hashCode()).toUpperCase() + ")";
        }
        return fullName;
    }

    public AlienSpec getFullAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className, this.species.speciesID, this.alienHashCode, this.x, this.y,
                this.tech, this.energy, this.fullName, this.speciesName, this.currentActionPower);
    }

    public AlienSpec getSimpleAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className, this.species.speciesID, this.fullName, this.speciesName);
    }

    public AlienSpecies getAlienSpecies() {
        if (this.species == null) {
            species = new AlienSpecies(this.domainName, this.packageName, this.className, species.speciesID, this.x, this.y);
        }
        return species;

    }

    public String toStringExpensive() {
        return getFullName() + ": "
                + "X:" + (x)
                + " Y:" + (y)
                + " E:" + (energy)
                + " T:" + (tech)
                + " r:" + ((int) Math.floor(Math.hypot((double) x, (double) y)));
    }

    public void processResults() {
        try {
            this.alien.processResults();
        } catch (UnsupportedOperationException e) {
        }

    }

    public void move() throws NotEnoughTechException {
 
        // Whether the move goes off the board will be determined by the grid

        Direction direction = null;
        try {
            direction = alien.getMove();
        } catch (UnsupportedOperationException e) {
            // we'll let that go
            direction = new Direction(0, 0);
        }

        this.checkMove(direction); // Throws an exception if illegal

        // we want to contain aliens in the 250 sphere, so apply the "cosmic drift"
        direction = this.containMove(x, y, direction);

        int oldx = x;
        int oldy = y;
        nextX = x + direction.x;
        nextY = y + direction.y;

        int width = Constants.width;
        int height = Constants.height;
        if (nextX > (width / 2) || nextX < (0 - width / 2) || nextY > (height / 2) || nextY < (0 - height / 2)) {
            debugErr("ac.move: Out of bounds: (" + x + ":" + y + ")");
        }
    }

    // this does the actual checking
    private void checkMove(Direction direction) throws NotEnoughTechException {
        int moveLength = distance(0, 0, direction.x, direction.y);

        // let one x one moves go
        if (Math.abs(direction.x) <= 1 && Math.abs(direction.y) <= 1) {
            return;
        }

        // If the move is farther than the alien has the power to move
        if (moveLength > tech) {
            debugErr("Illegal move(" + moveLength + "): " + (direction.x) + "," + (direction.y) + " tech " + tech);
            throw new NotEnoughTechException();
        }

        // If the move is farther than the alien has the tech to move
        if (moveLength > tech) {
            debugErr("Illegal move: " + (direction.x) + "," + (direction.y) + " tech " + tech);
            throw new NotEnoughTechException();
        }
    }

    public Direction containMove(int x, int y, Direction dir) {
        int dxi, dyi;

        dxi = dir.x;
        dyi = dir.y;

        if (x + dxi > Constants.width / 2) {
            dxi = Constants.width / 2 - x;
        }
        if (x + dxi < -Constants.width / 2) {
            dxi = -Constants.width / 2 - x;
        }
        if (y + dyi > Constants.width / 2) {
            dyi = Constants.width / 2 - y;
        }
        if (y + dyi < -Constants.width / 2) {
            dyi = -Constants.width / 2 - y;
        }
        return new Direction(dxi, dyi);
    }

    // easy way to kill an alien
    public void kill(String s) {
        debugOut(s + " with T:" + (Math.round(tech * 10) / 10)
                + " and E:" + (Math.round(energy * 10) / 10)
                + " during turn:" + grid.currentTurn
        );
        energy = 0;
    }

    // checked action
    public void getAction() throws NotEnoughEnergyException, UnknownActionException {
        Action a = alien.getAction();

        this.currentActionCode = a.code;
        this.currentActionPower = a.power;
        this.currentActionMessage = a.message;

        switch (a.code) {
            case None:
            case Gain:
                break;

            case Research:
                if (tech >= energy) {
                    // If the tech can't be researched due to lack of energy
                    debugOut("AC: Research violation with T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }
                break;

            case Spawn:
                if (a.power + ctx.getSpawningCost() > energy) {
                    debugOut("AC: Spawn violation with P:" + a.power + " T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }
                break;
            case Fight:
                if (energy < (a.power + ctx.getFightingCost())) {
                    debugOut("AC: Fight violation with P:" + a.power + " T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }

                // limit fight power by tech
                if (a.power > tech) {
                    this.currentActionPower = tech;
                }
                //TODO: Perform necessary checks
                break;

            default:
                debugOut("AC: Checking action, unknown: " + a.code.toString());
                throw new UnknownActionException();
        }
    }

    // this debugOut is not sensitive to chatter control
    public void debugOut(String s) {
        ctx.vis.debugOut("Alien " + getFullName() + ": " + s);
    }

    public void debugErr(String s) {
        ctx.vis.debugErr("Alien " + getFullName() + ": " + s);
    }
}
