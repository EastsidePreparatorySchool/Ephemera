/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.AlienSpec;
import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

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

    public ActionCode currentActionCode;
    public int currentActionPower;
    public String currentActionMessage;
    public boolean listening;

    int tech;
    int energy;
    public static boolean chatter = false;

    boolean fought;
    public int x;
    public int y;
    public int nextX;
    public int nextY;
    public String outgoingMessage;
    public int outgoingPower;

    // Declare stats here
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(SpaceGrid sg, GameVisualizer vis, int x, int y,
            String alienDomainName, String alienPackageName, String alienClassName, Constructor<?> cns, AlienSpecies as,
            int energy, int tech, int parent, String message) {

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

        Alien a;

        // if position = (0,0) assign random position
        if (x == 0 && y == 0) {

            this.x = ctx.getRandomInt(20) - 10; // TODO: get these hardcoded constants from spacegrid instead
            this.y = ctx.getRandomInt(20) - 10;
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
            a.init(this.ctx, alienHashCode, parent, message);
        } catch (Throwable t) {
            this.alien = null;
            debugOut("ac: Error constructing Alien");
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
            species = new AlienSpecies(this.domainName, this.packageName, this.className, species.speciesID);
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

    // checked moves
    public void move() throws NotEnoughTechException {
        // Whether the move goes off the board will be determined by the grid

        MoveDir direction = alien.getMove();
        this.checkMove(direction); // Throws an exception if illegal

        // we want to contain aliens in the 250 sphere, so apply the "cosmic drift"
        direction = this.applyDrift(x, y, direction);

        int oldx = x;
        int oldy = y;
        nextX = x + direction.x();
        nextY = y + direction.y();

        int width = 500;
        int height = 500;
        if (nextX >= (width / 2) || nextX < (0 - width / 2) || nextY >= (height / 2) || nextY < (0 - height / 2)) {
            debugErr("ac.move: Out of bounds: (" + x + ":" + y + ")");
        }
    }

    // this does the actual checking
    private void checkMove(MoveDir direction) throws NotEnoughTechException {
        // for immediate vicinity, just let this go
        if (Math.abs((long) direction.x()) + Math.abs((long) direction.y()) <= 2) {
            return;
        }
        // If the move is farther than the alien has the tech to move
        // distance |x|+|y|, easier to program.
        if (Math.abs((long) direction.x()) + Math.abs((long) direction.y())
                > (long) tech) {
            debugErr("Illegal move: " + (direction.x()) + "," + (direction.y()) + " tech " + tech);
            throw new NotEnoughTechException();
        }
    }

    // this calculates inward drift based on location
    // keeps aliens within 250 spaces of Earth
    public MoveDir applyDrift(int x, int y, MoveDir dir) {
        int dxi, dyi;

        dxi = dir.x();
        dyi = dir.y();

        if (x + dxi > 249) {
            dxi = 249 - x;
        }
        if (x + dxi < -250) {
            dxi = -250 - x;
        }
        if (y + dyi > 249) {
            dyi = 249 - y;
        }
        if (y + dyi < -250) {
            dyi = -250 - y;
        }
        return new MoveDir(dxi, dyi);
    }
    
    
    // easy way to kill an alien
    public void kill() {
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
                    debugOut("AC: Research fail with T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }
                break;

            case Spawn:
                if (a.power + ctx.getSpawningCost() > energy) {
                    debugOut("AC: Spawn fail with P:" + a.power + " T:" + (tech) + " and E:" + (energy));
                   throw new NotEnoughEnergyException();
                }
                break;
            case Fight:
                if (energy < (a.power + ctx.getFightingCost())) {
                    debugOut("AC: Fight fail with P:" + a.power + " T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }
                
                // limit power by tech
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

    public ViewImplementation getFullView() {
        // Create the alien's view
        int size = tech;
        int lowX = Math.max(x - size, (grid.width / -2));
        int lowY = Math.max(y - size, (grid.height / -2));
        int highX = Math.min(x + size, ((grid.width / 2) - 1));
        int highY = Math.min(y + size, ((grid.height / 2) - 1));

        List<AlienContainer> visibleAliens = new LinkedList<>();
        List<SpaceObject> visibleObjects = new LinkedList<>();

        for (int x = lowX; x <= highX; x++) {
            for (int y = lowY; y <= highY; y++) {
                if (!grid.aliens.isEmptyAt(x, y)) {
                    visibleAliens.addAll(grid.aliens.getAliensAt(x, y));
                }
                //visibleObjects.add()..
            }
        }

        // excluse thyself
        visibleAliens.remove(this);

        return new ViewImplementation(this, visibleAliens, visibleObjects, lowX, lowY, size);
    }

}
