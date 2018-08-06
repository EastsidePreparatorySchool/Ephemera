/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
import org.eastsideprep.spacecritters.alieninterfaces.*;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.eastsideprep.spacecritters.alieninterfaces.GridVector;
import org.eastsideprep.spacecritters.orbit.DummyTrajectory;
import org.eastsideprep.spacecritters.orbit.Orbitable;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author guberti
 */
public class AlienContainer {

    private static int currentID = 0;

    public final String domainName;
    public final String packageName;
    public final String className;
    public String fullName;
    public String speciesName;
    public AlienSpecies species;
    public final Constructor<?> constructor;
    public Alien alien;
    public AlienComplex calien;
    public final ContextImplementation ctx;
    public final SpaceGrid grid;
    public int alienHashCode;
    public int speciesID;

    public Action.ActionCode currentActionCode;
    public double currentActionPower;
    public String currentActionMessage;
    public Action currentAction;
    public boolean listening;
    Planet planet;

    double tech;
    double energy;

    public HashMap<String, Integer> secrets;

    boolean participatedInAction;
    public Position p = new Position(0, 0);
    public Position nextP;
    public WorldVector nextWP;
    public String outgoingMessage;
    public double outgoingPower;
    int turnsInSafeZone;

    boolean isComplex;
    Trajectory trajectory;

    public boolean updated = true;

    // Declare stats here
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(SpaceGrid sg, GameVisualizer vis, int x, int y,
            String alienDomainName, String alienPackageName, String alienClassName, Constructor<?> cns, AlienSpecies as,
            double energy, double tech, int parent, String message, Trajectory trajectory) throws InstantiationException { //[Q]

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
        this.secrets = new HashMap<>();
        this.speciesID = as.speciesID;
        this.planet = null;

        Alien a = null;

        // if position = (0,0) assign random position in safe zone
        if (x == 0 && y == 0) {
            this.p.x = ctx.getRandomInt(Constants.safeZoneRadius + 1);
            this.p.x *= (ctx.getRandomInt(2) == 0 ? 1 : -1);
            this.p.y = ctx.getRandomInt(Constants.safeZoneRadius + 1);
            this.p.y *= (ctx.getRandomInt(2) == 0 ? 1 : -1);
        } else {
            this.p.x = x;
            this.p.y = y;
        }

        this.alienHashCode = 0;

        // construct and initialize alien
        try {
            a = (Alien) cns.newInstance();
            this.alien = a;

            this.alienHashCode = ++currentID;
        } catch (Throwable t) {
            this.alien = null;
            grid.gridDebugErr("ac: Error constructing Alien");
            t.printStackTrace(System.out);
            throw new InstantiationException();
        }

        try {
            if (a instanceof AlienComplex) {
                initComplex(a, trajectory);
                ((AlienComplex) a).initComplex(this.ctx, this.alienHashCode, parent, message);
            } else {
                a.init(this.ctx, this.alienHashCode, parent, message);
            }
        } catch (UnsupportedOperationException e) {
            // let this go
        }

        fullName = this.getFullName();
        speciesName = this.getFullSpeciesName();

    }
    // class-related helpers

    public void initComplex(Alien a, Trajectory trajectory) throws InstantiationException {
        calien = (AlienComplex) a;
        isComplex = true;

        if (trajectory == null) {
            grid.gridDebugErr("ac: No trajectory or focus given");
            throw new InstantiationException();
        }

        if (trajectory instanceof DummyTrajectory) {
            this.trajectory = new Trajectory(
                    trajectory.currentFocus, //focus from the dummy trajectory
                    (grid.rand.nextDouble() * 5 + 5) * Constants.deltaX, //semi-latus rectum
                    Math.pow(grid.rand.nextDouble(), 3), //Eccentricity
                    1, //signum
                    grid.rand.nextDouble() * 2 * Math.PI, //rotation
                    grid);
        } else {
            this.trajectory = trajectory.clone();
        }

        p = new Position(this.trajectory.worldPositionAtTime(0));
    }

    public double getMass() {
        return Constants.alienMass;
    }

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
        return new AlienSpec(this.domainName, this.packageName, this.className, this.species.speciesID, this.alienHashCode, this.p.round().x, this.p.round().y,
                this.tech, this.energy, this.fullName, this.speciesName, this.currentActionPower);
    }

    /*public AlienSpec getSimpleAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className, this.species.speciesID, this.fullName, this.speciesName);
    }*/
    public AlienSpecies getAlienSpecies() {
        if (this.species == null) {
            assert false;
            species = new AlienSpecies(this.domainName, this.packageName, this.className, species.speciesID, this.p.round().x, this.p.round().y);
        }
        return species;

    }

    public String toStringExpensive() {
        return getFullName() + ": "
                + "X:" + (p.x)
                + " Y:" + (p.y)
                + " E:" + (energy)
                + " T:" + (tech)
                + " r:" + ((int) Math.floor(Math.hypot(p.x, p.y)));
    }

    public void processResults() {
        try {
            this.alien.processResults();
        } catch (UnsupportedOperationException e) {
        }

    }

    public void move() throws NotEnoughTechException {
        // if on planet, ignore move
        if (this.planet != null) {
            return;
        }

        if (isComplex) {
            movecomplex();
        } else {
            movestandard();
        }
    }

    public void movecomplex() throws NotEnoughTechException {
        /* FIND DELTAV */
        updated = true;
        double tolerance = 10; // percent

        WorldVector deltaV;
        try {
            deltaV = calien.getAccelerate();

        } catch (UnsupportedOperationException e) {
            deltaV = null;
        }

        // don't scale deltav by mass. we will fix accounting some other time.
//        if (deltaV != null) {
//            deltaV = new WorldVector(deltaV.scale(1f / getMass()));
//        }
// DEBUG
//        if (deltaV != null && deltaV.x == 0 && deltaV.y == 0) {
//            deltaV = null; //if there is no acceleration, don't do anything
//        }
// let's see if this thing can handle 0,0 accelerations. In theory, the new conic should look very similar to the old conic.
        if (deltaV == null) {
            deltaV = new WorldVector(0, 0);
        }
// END DEBUG

        WorldVector oldWP = nextWP;
        nextWP = trajectory.worldPositionAtTime(grid.getTime());
        nextP = new Position(nextWP);

        // debug:
        // calculate apparent velocity from move distance
        if (oldWP != null) {
            Vector2 d = nextWP.subtract(oldWP).scale(1 / Constants.deltaT);
            System.out.println("apparent alien velocity: " + d + ", mag:" + d.magnitude());
        }

        /* CHARGE ALIEN FOR DELTAV */
        //aliens cannot change their trajectory if they are enot in the game limits
        if (GridDisk.isValidPoint(nextP.round())) {
            if (deltaV != null) {
                double m = deltaV.magnitude();
                if (m < Constants.maxDeltaV(tech)) {
                    this.energy -= Constants.accelerationCost(m);
                } else {
                    deltaV = null;
                }
            }
        } else if (!trajectory.isBound()) {
            System.out.println("Floated into the abyss");
            kill("Floated into the abyss");
            return;
        }

        /* DETERMINE CURRENT FOCUS */
        Orbitable focus = findFocus();

        /* FINALLY, COMPUTE NEW TRAJECTORY */
        if (focus != trajectory.currentFocus) { //make a new trajectory if the focus has changed
            System.out.print("Changing focus ");
            if (focus instanceof Planet) {
                System.out.print("to planet ");
                System.out.println(((Planet) focus).className);
            } else if (focus instanceof Star) {
                System.out.print("to star ");
                System.out.println(((Star) focus).className);
            }
            WorldVector v = trajectory.velocityAtTime(grid.getTime());
            if (deltaV != null) {
                v = v.add(deltaV);
            }

            Trajectory trajectoryNew = new Trajectory(focus, nextWP, v, grid);
            Vector2 v2 = trajectoryNew.velocityAtTime(grid.getTime());

            if (Math.abs(v.x / v2.x) > (1.0 + tolerance / 100.0)
                    || Math.abs(v.x / v2.x) < (1.0 - tolerance / 100.0)
                    || Math.abs(v.y / v2.y) > (1.0 + tolerance / 100.0)
                    || Math.abs(v.y / v2.y) < (1.0 - tolerance / 100.0)) {
                System.out.println("Focus change: entry/exit velocities do not match within tolerance values");
                System.out.println("  exit velocity: " + v + " mag " + v.magnitude());
                System.out.println("  entry velocity: " + v2 + " mag " + v2.magnitude());
            }
            trajectory = trajectoryNew;
            return;
        } else if (deltaV != null) { //if not, alter the old one
            Vector2 v = trajectory.velocityAtTime(grid.getTime());
            trajectory.accelerate(deltaV, grid.getTime());
            Vector2 v2 = trajectory.velocityAtTime(grid.getTime()).subtract(deltaV);

            if ((v.x / v2.x) > (1.0 + tolerance / 100.0)
                    || (v.x / v2.x) < (1.0 - tolerance / 100.0)
                    || (v.y / v2.y) > (1.0 + tolerance / 100.0)
                    || (v.y / v2.y) < (1.0 - tolerance / 100.0)) {
                System.out.println("Accelerate: adjusted entry/exit velocities do not match within tolerance values");
                System.out.println("  exit velocity: " + v + " mag " + v.magnitude());
                System.out.println("  entry velocity: " + v2 + " mag " + v2.magnitude());
            }
            return;
        }
        updated = false;
    }

    public Orbitable findFocus() {
        Orbitable focus = trajectory.currentFocus;
        boolean altered = false;

        //if orbiting a planet and within that planet's hill sphere, you're staying there
        //if not, enter the parent star's orbit
        if (focus instanceof Planet) {
            System.out.println(focus.hillRadius());
            if (focus.position(grid.getTime()).subtract(nextP).magnitude() <= focus.hillRadius()) {
                return focus;
            } else {
                focus = ((Planet) focus).trajectory.currentFocus;
            }
        }

        //parent must be a star if code gets here
        double F = focus.mass() / focus.position(grid.getTime()).subtract(nextP).magnitude();

        //for each star
        //if it exerts more force on you than your parent star, it becomes your new parent
        for (InternalSpaceObject iso : grid.objects) {
            if (iso instanceof Star && iso != focus) {
                if (iso.mass() / iso.position(grid.getTime()).subtract(nextP).magnitude() > F) {
                    focus = iso;
                    altered = true;
                    F = focus.mass() / focus.position(grid.getTime()).subtract(nextP).magnitude();
                }
            }
        }

        //for each planet orbiting your parent star
        //if you're in their hill sphere, they are your new parent
        //TODO: does not account for overlapping hill spheres
        for (InternalSpaceObject iso : grid.objects) {
            if (iso instanceof Planet && iso.trajectory.currentFocus == focus) {
                if (iso.position(grid.getTime()).subtract(nextP).magnitude() <= iso.hillRadius()) {
                    focus = iso;
                    altered = true;
                }
            }
        }

        return focus;
    }

    public void movestandard() throws NotEnoughTechException { //[Q]

        // Whether the move goes off the board will be determined by the grid
        Vector2 direction = null;
        try {
            direction = alien.getMove();
        } catch (UnsupportedOperationException e) {
            // we'll let that go
            direction = new Direction(0, 0);
        }

        checkMove(direction); // Throws an exception if illegal

        // we want to contain aliens in the 250 sphere, so apply the "cosmic drift"
        direction = this.containMove(p.x, p.y, direction);

        nextP = new Position(p.add(direction));

        this.energy -= direction.magnitude() * Constants.standardMoveCost;
    }

    // this does the actual checking
    private void checkMove(Vector2 direction) throws NotEnoughTechException { //[Q]
        int moveLength = (int) direction.magnitude();

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

    public Direction containMove(double x, double y, Vector2 dir) { //[Q]
        double dxi, dyi;

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
                    debugOut("AC: Research violation with " + ctx.getStateString());
                    throw new NotEnoughEnergyException();
                }
                break;

            case Spawn:
                if (a.power + ctx.getSpawningCost() > energy) {
                    debugOut("AC: Spawn violation with P:" + a.power + " " + ctx.getStateString());
                    throw new NotEnoughEnergyException();
                }
                break;
            case Fight:
                if (energy < (a.power + ctx.getFightingCost())) {
                    debugOut("AC: Fight violation with P:" + a.power + " " + ctx.getStateString());
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
