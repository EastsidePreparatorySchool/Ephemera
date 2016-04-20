/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.GameElementKind;
import java.util.*;
import java.lang.reflect.Constructor;
import alieninterfaces.*;
import gameengineinterfaces.*;
import java.io.IOException;

/**
 *
 * @author guberti
 */
public class SpaceGrid {

    GameVisualizer vis;
    public AlienGrid aliens;    // Aliens are born and die, so our list needs to be able to grow and shrink
    List<SpaceObject> objects;  // Stars, planets, space stations, etc.
    int width;
    int height;
    int currentTurn = 1;

    // this is the only approved random generator for the game. Leave it alone!
    public static Random rand;

    public SpaceGrid(GameVisualizer vis, int width, int height) {
        this.vis = vis;
        this.width = width;
        this.height = height;
        aliens = new AlienGrid(width, height); // AlienContainer type is inferred
        objects = new ArrayList<>();

        long randSeed = System.nanoTime();
        rand = new Random(randSeed);
        vis.debugOut("RandSeed: " + randSeed);
    }

    public boolean executeGameTurn() {
        performCommunications();
        removeDeadAliens();
        performReceives();
        removeDeadAliens();
        Thread.yield();

        requestAlienMoves();
        recordAlienMoves();
        removeDeadAliens();
        Thread.yield();

        performAlienActions();
        removeDeadAliens();
        resetAliens();
        Thread.yield();

        currentTurn++;

        AlienContainer aUniqueAlien = null;
        for (AlienContainer a : aliens) {
            if (a != null) {
                if (aUniqueAlien != null) {
                    if (aUniqueAlien.constructor != a.constructor) {
                        // more than one species active, game not over
                        return false;
                    }
                } else {
                    // record this alien as the first species we found
                    aUniqueAlien = a;
                }
            }
        }
        // if we get to here, there was at most one species. Game Over.
        return true;
    }

    public void listStatus() {
        vis.debugErr("");
        vis.debugErr("Current Aliens:");

        for (AlienContainer ac : aliens) {
            if (ac != null) {
                vis.debugErr("Alien " + ac.toString());
            }
        }
        vis.debugErr("");
    }

    public void requestAlienMoves() {
        //vis.debugOut("Requesting moves for " + (aliens.size()) + " aliens");
        for (AlienContainer ac : aliens) {
            // get rid of stale views from prior moves
            ac.ctx.view = null;

            int oldX = ac.x;
            int oldY = ac.y;

            // call the alien to move
            try {
                ac.move();
            } catch (Exception ex) {
                ac.debugErr("Unhandled exception in getMove(): " + ex.toString());
                for (StackTraceElement s : ex.getStackTrace()) {
                    ac.debugErr(s.toString());
                }
                ac.kill();
            }

        }
    }

    public void performCommunications() {
        // phase 1: take outgoing messages and store in ac
        for (AlienContainer ac : aliens) {
            // get rid of stale views from prior phases
            ac.ctx.view = null;

            // call the alien to communicate
            try {
                ac.alien.communicate();
            } catch (Exception ex) {
                ac.debugErr("Unhandled exception in communicate(): " + ex.toString());
                for (StackTraceElement s : ex.getStackTrace()) {
                    ac.debugErr(s.toString());
                }
                ac.kill();
            }
        }

        // phase 2: route messages to gridpoints
        for (AlienContainer ac : aliens) {
            if (ac.outgoingMessage != null & ac.outgoingPower != 0) {
                ac.ctx.routeMessages();
                ac.outgoingMessage = null;
            }
        }
    }

    public void performReceives() {
        // phase 3: receive messages
        for (AlienContainer ac : aliens) {
            if (ac.listening) {
                // get rid of stale views from prior phases
                ac.ctx.view = null;

                // call the alien to receive messages
                AlienCell acell = aliens.getAliensAt(ac.x, ac.y);
                if (acell.currentMessages != null) {
                    String[] messages = new String[acell.currentMessages.size()];
                    try {
                        ac.alien.receive(acell.currentMessages.toArray(messages));
                    } catch (Exception ex) {
                        ac.debugErr("Unhandled exception in receive(): " + ex.toString());
                        for (StackTraceElement s : ex.getStackTrace()) {
                            ac.debugErr(s.toString());
                        }
                        ac.kill();
                    }
                }
            }
        }
    }

    // now that moving is done and views don't matter anymore,
    // record the moves in AlienContainers and the grid
    public void recordAlienMoves() {
        //vis.debugOut("Recording moves for " + (aliens.size()) + " aliens");
        for (AlienContainer ac : aliens) {
            int oldX = ac.x;
            int oldY = ac.y;

            if (oldX != ac.nextX || oldY != ac.nextY) {
                ac.x = ac.nextX;
                ac.y = ac.nextY;
                aliens.move(ac, oldX, oldY, ac.x, ac.y);
            }

            // call shell visulizer
            // just make sure we don't blow up the alien beacuse of an exception in the shell
            try {
                vis.showMove(ac.getFullAlienSpec(), oldX, oldY);
            } catch (Exception e) {
                vis.debugErr("Unhandled exception in visualize: showMove");
                for (StackTraceElement s : e.getStackTrace()) {
                    vis.debugErr(s.toString());
                }
            }
        }
    }

    public void removeDeadAliens() {
        for (Iterator<AlienContainer> iterator = aliens.iterator(); iterator.hasNext();) {
            AlienContainer ac = iterator.next();
            if (ac.energy <= 0) {
                vis.showDeath(ac.getFullAlienSpec());
                aliens.unplug(ac);
                iterator.remove();
            }
        }
    }

    public void resetAliens() {
        aliens.stream().forEach((alien) -> {
            alien.fought = false;
            alien.ctx.view = null;
            alien.outgoingMessage = null;
            alien.listening = false;

            AlienCell acell = alien.grid.aliens.getAliensAt(alien.x, alien.y);
            acell.listening = false;
            acell.currentMessages = null;
        });
    }

    public void performAlienActions() {
        LinkedList<AlienContainer> newAliens = new LinkedList();

        //vis.debugOut("Processing actions for " + (aliens.size()) + " aliens");
        // first request all the actions from the aliens
        for (AlienContainer thisAlien : aliens) {
            // get rid of stale views from prior phases
            thisAlien.ctx.view = null;

            try {
                // Note: getAction() checks validity
                thisAlien.getAction();
                //thisAlien.api.debugOut(thisAlien.currentAction.name());
            } catch (Exception ex) {
                // if an alien blows up here, we'll kill it. 
                thisAlien.currentActionCode = ActionCode.None;
                thisAlien.currentActionPower = 0;
                thisAlien.debugOut("Unhandled exception in getAction(): " + ex.toString());
                for (StackTraceElement s : ex.getStackTrace()) {
                    thisAlien.debugOut(s.toString());
                }
                thisAlien.kill();
            }
        }

        // now process all actions
        for (AlienContainer thisAlien : aliens) {
            //thisAlien.debugOut(thisAlien.currentActionCode.toString());

            // if this guy was part of a fight, don't bother with more actions
            if (thisAlien.fought) {
                continue;
            }

            switch (thisAlien.currentActionCode) {
                case Fight:
                    //vis.debugOut("SpaceGrid: Processing Fight");

                    List<AlienSpec> fightSpecs = new ArrayList<>();
                    HashMap<String, Integer> fightSpecies = new HashMap<>();

                    LinkedList<AlienContainer> fightingAliens = aliens.getAliensAt(thisAlien.x, thisAlien.y);

                    thisAlien.fought = true;

                    for (AlienContainer fightingAlien : fightingAliens) {

                        fightingAlien.fought = true;

                        if (fightingAlien.currentActionCode != ActionCode.Fight) {
                            fightingAlien.currentActionPower = 0;
                        }

                        Integer speciesCombinedPower = fightSpecies.get(fightingAlien.speciesName);
                        if (speciesCombinedPower == null){
                            speciesCombinedPower = 0;
                        }
                            
                        speciesCombinedPower += fightingAlien.currentActionPower;
                        /*if (fightSpecies.containsKey(fightingAlien.speciesName)) {
                            fightSpecies.put(fightingAlien.speciesName,
                                    fightSpecies.get(fightingAlien.speciesName) +
                                    fightingAlien.currentActionPower);
                        } else {
                            fightSpecies.put(
                                    fightingAlien.speciesName,
                                    fightingAlien.currentActionPower);
                        }*/
                        fightSpecies.put(
                                fightingAlien.speciesName,
                                fightingAlien.currentActionPower);

                        fightSpecs.add(fightingAlien.getFullAlienSpec());
                    }

                    vis.showFightBefore(thisAlien.x, thisAlien.y, fightSpecs);

                    // Determine the winning power in the fight
                    int winningPower = 0; // The fight powers might tie

                    Set set = fightSpecies.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mentry = (Map.Entry) iterator.next();
                        winningPower = Math.max(winningPower, (int) mentry.getValue());
                    }

                    int maxTech = 0;

                    for (AlienContainer candidateWinner : fightingAliens) {
                        // Max tech
                        maxTech = Math.max(maxTech, candidateWinner.tech);
                    }

                    List<String> winningSpecies = new ArrayList<>();
                    AlienContainer winner = null; // Must be initialized to make Java happy

                    for (AlienContainer ac : fightingAliens) {
                        // If alien was winner / part of a tie
                        if (fightSpecies.get(ac.speciesName) == winningPower) {
                            // If the species was not already known to be a winning race
                            String name = ac.speciesName;
                            if (!winningSpecies.contains(name)) {
                                // Add it
                                winningSpecies.add(name);
                            }
                            winner = ac;
                        }
                    }

                    if (winningSpecies.size() == 1) { // If there is no tie
                        for (AlienContainer ac : fightingAliens) {
                            if (ac.speciesName == winningSpecies.get(0)) {
                                // The winning species's tech is brought up to the max in the group
                                winner.tech = maxTech;
                            }
                        }
                    }

                    for (AlienContainer ac : fightingAliens) {
                        // If the alien was not part of one of the winning species
                        if (fightSpecies.get(ac.speciesName) != winningPower) {

                            // If the alien was beaten by more than five energy points
                            if (winningPower > fightSpecies.get(ac.speciesName) + 5) {

                                // The alien will then be killed
                                ac.kill();

                            } else { // If the alien was beaten by less than 5 points
                                // They go down to two technology
                                ac.tech = 2;
                            }
                        }
                        vis.showFightAfter(thisAlien.x, thisAlien.y, fightSpecs);
                    }
                    break;

                case Gain:
                    thisAlien.energy += Math.floor(thisAlien.tech / 10) + 1;
                    break;

                case Research:
                    // there is an arbitrary bceiling at 32 to keep views under control
                    // aliens can research up to 30, must buy tech beyond that from residents
                    if (thisAlien.tech < 30) {
                        thisAlien.energy -= thisAlien.tech;
                        thisAlien.tech++; // you only gain 1 tech regardless of energy invested
                    }
                    break;

                case Spawn:
                    thisAlien.energy -= thisAlien.ctx.getSpawningCost() + thisAlien.currentActionPower;

                    // construct a random move for the new alien depending on power and send that move through drift correction
                    // spend thisAction.power randomly on x move, y move and initital power
                    int power = Math.min(rand.nextInt(thisAlien.currentActionPower), thisAlien.currentActionPower / 2);
                    int powerForX = rand.nextInt(thisAlien.currentActionPower - power);
                    int powerForY = rand.nextInt(thisAlien.currentActionPower - power - powerForX);

                    int x = powerForX * (rand.nextInt(2) == 0 ? 1 : -1);
                    int y = powerForY * (rand.nextInt(2) == 0 ? 1 : -1);

                    thisAlien.energy -= power + powerForX + powerForY;

                    MoveDir dir = new MoveDir(x, y);
                    dir = thisAlien.applyDrift(thisAlien.x, thisAlien.y, dir);

                    x = thisAlien.x + dir.x();
                    y = thisAlien.y + dir.y();

                    int width = 500;
                    int height = 500;
                    if (x >= (width / 2) || x < (0 - width / 2) || y >= (height / 2) || y < (0 - height / 2)) {
                        vis.debugErr("sg.spawn: Out of bounds: (" + x + ":" + y + ")");
                        vis.debugErr("sg.spawn: Out of bounds: old(" + thisAlien.x + ":" + thisAlien.y + ")");
                        vis.debugErr("sg.spawn: Out of bounds: dir(" + dir.x() + ":" + dir.y() + ")");
                        vis.debugErr("sg.spawn: Out of bounds: power " + thisAlien.currentActionPower);
                    }

                    // make a container, it will create the alien
                    AlienContainer aC = new AlienContainer(
                            this,
                            this.vis,
                            thisAlien.x + dir.x(),
                            thisAlien.y + dir.y(),
                            thisAlien.domainName,
                            thisAlien.packageName,
                            thisAlien.className,
                            thisAlien.constructor,
                            power,
                            thisAlien.tech, // tech inherited undiminished from parent
                            thisAlien.alienHashCode,
                            thisAlien.currentActionMessage);

                    // Add in the alien to a new arraylist
                    // so the list is not disrupted
                    newAliens.add(aC);

                    break;
            }
            // this ends our big switch statement
        }

        // add all new aliens
        for (AlienContainer ac : newAliens) {
            aliens.addAlienAndPlug(ac);
            // visualize
            vis.showSpawn(ac.getFullAlienSpec());
        }
    }

    // this calculates inward drift based on location
    // keeps aliens within 250 spaces of Earth
    public MoveDir applyDrift2(int x, int y, MoveDir dir) {
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
            vis.debugOut("Tunneled back to Earth");
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
        //ctx.vis.debugOut("Drift: r              " + Double.toString(r));
        //ctx.vis.debugOut("Drift: alpha          " + Double.toString(getAngleDiff(alpha, 0)));
        //ctx.vis.debugOut("Drift: deltaAlpha     " + Double.toString(getAngleDiff(deltaAlpha, 0)));
        //ctx.vis.debugOut("Drift: diff           " + Double.toString(getAngleDiff(deltaAlpha, alpha)));
        if (getAngleDiff(deltaAlpha, alpha) < Math.PI / 2) {
            // alien bearing slightly left, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        } else if (getAngleDiff(deltaAlpha, alpha) > 3 * Math.PI / 2) {
            // alien bearing slightly right, make it more left depending on r^3
            deltaAlpha = alpha + (Math.PI / 2) - (((Math.PI / 2) - getAngleDiff(deltaAlpha, alpha)) * (1 - Math.pow(r / 250, 3)));
        }
        deltaAlpha = getAngleDiff(deltaAlpha, 0);
        //ctx.vis.debugOut("Drift: new deltaAlpha " + Double.toString(deltaAlpha));

        //put the x,y back together
        dx = deltaR * Math.cos(deltaAlpha);
        dy = deltaR * Math.sin(deltaAlpha);

        int dxi = (int) Math.round(dx);
        int dyi = (int) Math.round(dy);

        //ctx.vis.debugOut("Drift final: ("
        //        + (x + oldx) + ","
        //        + (y + oldy) + ") -> ("
        //        + (x + dxi) + ","
        //        + (y + dyi) + ")");
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

    MoveDir inGrid(AlienContainer ac, MoveDir dir) {

        int x = ac.x;
        int y = ac.y;

        int dxi = dir.x();
        int dyi = dir.y();

        // stop-gap-measure to keep things in grid
        if (x + dxi > 249) {
            dxi = 249 - x;
            vis.debugOut("Drift stop: x: " + x + dxi);
        }
        if (x + dxi < -250) {
            dxi = -250 - x;
            vis.debugOut("Drift stop: x: " + x + dxi);
        }
        if (y + dyi > 249) {
            dyi = 249 - y;
            vis.debugOut("Drift stop: y: " + y + dyi);
        }
        if (y + dyi < -250) {
            dyi = -250 - y;
            vis.debugOut("Drift stop: y: " + y + dyi);
        }

        return new MoveDir(dxi, dyi);
    }

    void addAlien(int x, int y, String domainName, String alienPackageName, String alienClassName, Constructor<?> cns) {
        AlienContainer ac = new AlienContainer(this, this.vis, x, y, domainName, alienPackageName, alienClassName, cns, 1, 1,
                0, // no parent
                null); // no message
        aliens.addAlienAndPlug(ac);
        vis.showSpawn(ac.getFullAlienSpec());
    }

    // Note: Having a seperate method for each SpaceObject seems a little gross,
    // as there is already a large if statement in addElement and the code in
    // addPlanet and addStar is nearly identical
    void addPlanet(GameElementSpec element) {
        Planet p = new Planet(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech, element.parent);
        objects.add(p);
    }

    void addStar(GameElementSpec element) {
        Star s = new Star(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech);
        objects.add(s);
    }

    void addResident(GameElementSpec element) {
        Resident r = new Resident(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech, element.parent);
    }

    public void addElement(GameElementSpec element) throws IOException {
        // can't use switch here because switch on enum causes weird error
        // if you doubt that, uncomment this line:
        // switch (element.kind) {}

        String debugMessage = "SpaceGrid: Loading and constructing ";

        if (element.kind == GameElementKind.ALIEN) {
            debugMessage += "alien";

            // position (0,0) leads to random assignment
            addAlien(0, 0, "eastsideprep.org", element.packageName, element.className, element.cns);

        } else if (element.kind == GameElementKind.PLANET) {
            debugMessage += "planet";

            addPlanet(element);

        } else if (element.kind == GameElementKind.STAR) {
            debugMessage += "star";

            addStar(element);
        } else if (element.kind == GameElementKind.RESIDENT) {
            debugMessage += "resident";

            addResident(element);
        }

        debugMessage += " " + element.packageName + ":" + element.className;
        vis.debugOut(debugMessage);
    }
}
