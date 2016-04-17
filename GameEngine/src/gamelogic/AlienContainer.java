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
import java.util.Random;

/**
 *
 * @author guberti
 */
public class AlienContainer {

    public final String domainName;
    public final String packageName;
    public final String className;
    public final Constructor<?> constructor;
    public Alien alien;
    public final ContextImplementation ctx;
    public final SpaceGrid grid;

    public ActionCode currentActionCode;
    public int currentActionPower;

    int tech;
    int energy;
    boolean chatter;

    boolean fought;
    public int x;
    public int y;
    public int nextX;
    public int nextY;

    static Random rand = new Random(System.nanoTime());

    // Declare stats here
    //
    // Heads up: This constructs an AlienContainer and contained Alien
    //
    public AlienContainer(SpaceGrid sg, GameVisualizer vis, int x, int y, String alienDomainName, String alienPackageName, String alienClassName, Constructor<?> cns, int energy, int tech) {
        Alien a;

        this.domainName = alienDomainName;
        this.packageName = alienPackageName;
        this.className = alienClassName;
        this.constructor = cns;
        this.energy = energy;
        this.tech = tech;
        this.chatter = false;
        this.ctx = new ContextImplementation(this, vis);
        this.grid = sg;

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
            a.init(this.ctx);
        } catch (Throwable t) {
            this.alien = null;
            debugErr("ac: Error constructing Alien");
        }

    }

    // class-related helpers
    public String getFullSpeciesName() {
        return domainName + ":" + packageName + ":" + className;
    }

    public String getFullName() {
        return getFullSpeciesName() + "(" + Integer.toHexString(alien.hashCode()).toUpperCase() + ")";
    }

    public AlienSpec getFullAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className, this.hashCode(), this.x, this.y, this.tech, this.energy, this.currentActionPower);
    }

    public AlienSpec getSimpleAlienSpec() {
        return new AlienSpec(this.domainName, this.packageName, this.className);
    }

    @Override
    public String toString() {
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
        x += direction.x();
        y += direction.y();

        int width = 500;
        int height = 500;
        if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
            debugErr("ac.move: Out of bounds: (" + x + ":" + y + ")");
        }

        // call shell visulizer
        // just make sure we don't blow up the alien beacuse of an exception in the shell
        try {
            ctx.vis.showMove(getFullAlienSpec(), oldx, oldy);
        } catch (Exception e) {
            grid.vis.debugErr("Unhandled exception in visualize: showMove");
            for (StackTraceElement s: e.getStackTrace()){
                grid.vis.debugErr(s.toString());
            }
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
        r = Math.hypot((double) x, (double) y);
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
        deltaR = Math.hypot((double) dir.x(), (double) dir.y());

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
        ctx.vis.debugOut("Drift: r              " + Double.toString(r));
        ctx.vis.debugOut("Drift: alpha          " + Double.toString(getAngleDiff(alpha, 0)));
        ctx.vis.debugOut("Drift: deltaAlpha     " + Double.toString(getAngleDiff(deltaAlpha, 0)));
        ctx.vis.debugOut("Drift: diff           " + Double.toString(getAngleDiff(deltaAlpha, alpha)));

        if (getAngleDiff(deltaAlpha, alpha) < Math.PI / 2) {
            // alien bearing slightly left, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        } else if (getAngleDiff(deltaAlpha, alpha) > 3 * Math.PI / 2) {
            // alien bearing slightly right, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        }
        deltaAlpha = getAngleDiff(deltaAlpha, 0);
        ctx.vis.debugOut("Drift: new deltaAlpha " + Double.toString(deltaAlpha));

        //put the x,y back together
        dx = deltaR * Math.cos(deltaAlpha);
        dy = deltaR * Math.sin(deltaAlpha);

        int dxi = (int) Math.round(dx);
        int dyi = (int) Math.round(dy);

        // stop-gap-measure to keep things in grid
        if (x + dxi > 249) {
            dxi = 249 - x;
            ctx.vis.debugOut("Drift stop: x: " + x + dxi);
        }
        if (x + dxi < -250) {
            dxi = -250 - x;
            ctx.vis.debugOut("Drift stop: x: " + x + dxi);
        }
        if (y + dyi > 249) {
            dyi = 249 - y;
            ctx.vis.debugOut("Drift stop: y: " + y + dyi);
        }
        if (y + dyi < -250) {
            dyi = -250 - y;
            ctx.vis.debugOut("Drift stop: y: " + y + dyi);
        }

        ctx.vis.debugOut("Drift final: ("
                + (x + oldx) + ","
                + (y + oldy) + ") -> ("
                + (x + dxi) + ","
                + (y + dyi) + ")");

        return new MoveDir(dxi, dyi);
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

    // easy way to kill an alien
    public void kill() {
        energy = 0;
    }

    // checked action
    public void getAction() throws NotEnoughEnergyException, UnknownActionException {
        Action a = alien.getAction();

        this.currentActionCode = a.code;
        this.currentActionPower = a.power;

        switch (a.code) {
            case None:
            case Gain:
                break;

            case Research:
                if (tech >= energy) { // If the tech can't be researched due to lack of energy
                    debugOut("AC: Research with T:" + (tech) + " and E:" + (energy));
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

class NotEnoughEnergyException extends Exception {
}

class NotEnoughTechException extends Exception {
}

class UnknownActionException extends Exception {
}
