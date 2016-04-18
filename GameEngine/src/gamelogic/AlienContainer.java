/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

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
            String alienDomainName, String alienPackageName, String alienClassName, Constructor<?> cns,
            int energy, int tech, int parent, String message) {

        this.domainName = alienDomainName;
        this.packageName = alienPackageName;
        this.className = alienClassName;
        this.constructor = cns;
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
        return new AlienSpec(this.domainName, this.packageName, this.className, this.alienHashCode, this.x, this.y,
                this.tech, this.energy, this.fullName, this.speciesName, this.currentActionPower);
    }

    public AlienSpec getSimpleAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className, this.fullName, this.speciesName);
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
        double r;
        double alpha;
        double deltaAlpha;
        double deltaR;
        double dx;
        double dy;
        //double reduction;
        int oldx, oldy;

        oldx = dir.x();
        oldy = dir.y();

        // distance from Earth, and vector angle
        //r = Math.hypot((double) x, (double) y);
        r = (double) ((long) x * (long) x + (long) y * (long) y); // reasoning in r^2 instead
        alpha = Math.atan2(x, y);

        // no action below r = 20
        if (r < 400) {
            return dir;
        }

        // wormhole for escapees (r > 250)
        if (r > 62500) {
            // return these people to Earth
            this.debugOut("Tunneled back to Earth");
            return new MoveDir(0 - x, 0 - y);
        }

        // get angle of proposed move
        deltaAlpha = Math.atan2(dir.x(), dir.y());
        // keep track of it as difference from current angle
        deltaAlpha = getAngleDiff(alpha, deltaAlpha);
        //debugOut("Drift: deltaAlpha" + deltaAlpha);

        deltaR = (double) (dir.x() + dir.y()); // cheating: not hypothenuse, but sum of sides.

        // no action if inward
        if (deltaAlpha <= Math.PI / 2 && deltaAlpha >= -Math.PI / 2) {
            // outward: push the alien counterclockwise
            deltaAlpha += getAngleDiff(deltaAlpha, Math.PI) * (r / 55000);
            deltaAlpha = getAngleDiff(deltaAlpha, 0); // normalize
        }

        // this is final drift correction: we keep changing angle and radius of the move until it fits
        long hypotSq;
        int count = 0;
        do {
            //put the x,y back together
            dx = deltaR * Math.cos(deltaAlpha + alpha);
            dy = deltaR * Math.sin(deltaAlpha + alpha);
            deltaR *= 1.1; // expecting a correction in next loop
            deltaAlpha += 2 * Math.PI / 10;

            hypotSq = hypotSquare((x + (long) dx), (y + (long) dy));
            //debugOut("Drift hypotSq of new r: " + hypotSq);
            if (count++ > 10) {
                if (count > 11) {
                    break;
                }
                // this isn't working, turn it all the way back
                deltaAlpha = Math.PI;
                deltaR = 32;
            }
        } while (hypotSq >= 55000);

        int dxi = (int) Math.round(dx);
        int dyi = (int) Math.round(dy);

        // stop-gap-measure to keep things in grid
        if (x + dxi > 249) {
            dxi = 249 - x;
            debugOut("Drift stop: x: " + (x + dxi));
        }
        if (x + dxi < -250) {
            dxi = -250 - x;
            debugOut("Drift stop: x: " + (x + dxi));
        }
        if (y + dyi > 249) {
            dyi = 249 - y;
            debugOut("Drift stop: y: " + (y + dyi));
        }
        if (y + dyi < -250) {
            dyi = -250 - y;
            debugOut("Drift stop: y: " + (y + dyi));
        }
        /*
         ctx.vis.debugOut("Drift final: ("
         + (x + oldx) + ","
         + (y + oldy) + ") -> ("
         + (x + dxi) + ","
         + (y + dyi) + ")");
         */

        //if (Math.hypot(dxi + x, dyi + y) > 250) {
        //    debugOut("Drift not contained: " + (dxi + x) + "," + (dyi + y));
        //}
        return new MoveDir(dxi, dyi);
    }

    public long hypotSquare(long x, long y) {
        return x * x + y * y;
    }

    // normalizes angle difference to fit in [-pi:pi]
    public double getAngleDiff(double alpha, double beta) {
        double gamma = alpha - beta;
        if (gamma > Math.PI) {
            gamma = gamma - (Math.PI * 2);
        }
        if (gamma < -Math.PI) {
            gamma = gamma + (Math.PI * 2);
        }
        return gamma;
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
                if (tech >= energy) { // If the tech can't be researched due to lack of energy
                    //debugOut("AC: Research with T:" + (tech) + " and E:" + (energy));
                    throw new NotEnoughEnergyException();
                }
                break;

            case Spawn:
                if (a.power + ctx.getSpawningCost() > energy) {
                    throw new NotEnoughEnergyException();
                }
                break;
            case Fight:
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

