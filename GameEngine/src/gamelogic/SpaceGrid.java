/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

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

    GameSpace[][] grid;
    GameVisualizer vis;
    public AlienGrid aliens;    // Aliens are born and die, so our list needs to be able to grow and shrink
    List<SpaceObject> objects;  // Stars, planets, space stations, etc.
    int width;
    int height;

    static Random rand = new Random(System.currentTimeMillis());

    public SpaceGrid(GameVisualizer vis, int width, int height) {
        this.vis = vis;
        this.width = width;
        this.height = height;
        aliens = new AlienGrid(width, height); // AlienContainer type is inferred
        objects = new ArrayList<>();
    }

    public boolean executeGameTurn() {
        requestAlienMoves();
        Thread.yield();
        recordAlienMoves();
        removeDeadAliens();

        performAlienActions();
        removeDeadAliens();

        resetAliens();
        Thread.yield();

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

            // any obtained view is potentially stale again
            ac.ctx.view = null;
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
        });
    }

    public void performAlienActions() {
        LinkedList<AlienContainer> newAliens = new LinkedList();

        vis.debugOut("Processing actions for " + (aliens.size()) + " aliens");

        // first request all the actions from the aliens
        for (AlienContainer thisAlien : aliens) {

            try {
                // Note: getAction() checks validity
                thisAlien.getAction();
                //thisAlien.api.debugOut(thisAlien.currentAction.name());
            } catch (Exception ex) {
                // if an alien blows up here, we'll kill it. 
                thisAlien.currentActionCode = ActionCode.None;
                thisAlien.currentActionPower = 0;
                thisAlien.debugErr("Unhandled exception in getAction(): " + ex.toString());
                for (StackTraceElement s : ex.getStackTrace()) {
                    thisAlien.debugErr(s.toString());
                }
                thisAlien.kill();
            }
        }

        // now process all actions
        for (AlienContainer thisAlien : aliens) {
            thisAlien.debugOut(thisAlien.currentActionCode.toString());

            // if this guy was part of a fight, don't bother with more actions
            if (thisAlien.fought) {
                continue;
            }

            switch (thisAlien.currentActionCode) {
                case Fight:
                    //vis.debugOut("SpaceGrid: Processing Fight");

                    List<AlienSpec> fightSpecs = new ArrayList<>();

                    thisAlien.fought = true;

                    LinkedList<AlienContainer> fightingAliens = aliens.getAliensAt(thisAlien.x, thisAlien.y);
                    for (AlienContainer fightingAlien : fightingAliens) {

                        // If an alien didn't choose to fight at all
                        // (or if they fought with zero power)
                        // they will get blown off the board and the alien
                        // that won the fight will take their energy
                        // TODO: consider whether they should be killed or just lose
                        fightingAlien.fought = true;

                        if (fightingAlien.currentActionCode != ActionCode.Fight) {
                            fightingAlien.currentActionPower = 0;
                        }

                        fightSpecs.add(fightingAlien.getFullAlienSpec());
                    }

                    vis.showFightBefore(thisAlien.x, thisAlien.y, fightSpecs);

                    // Determine the winner and maximum tech in the fight
                    int winningPower = 0; // The fight powers might tie
                    int maxTech = 0;

                    for (AlienContainer candidateWinner : fightingAliens) {
                        // Winning power
                        winningPower = Math.max(winningPower, candidateWinner.currentActionPower);

                        // Max tech
                        maxTech = Math.max(maxTech, candidateWinner.tech);
                    }

                    List<String> winningSpecies = new ArrayList<>();
                    AlienContainer winner = null; // Must be initialized to make Java happy

                    for (AlienContainer ac : fightingAliens) {
                        // If alien was winner / part of a tie
                        if (ac.currentActionPower == winningPower) {
                            // If the species was not already known to be a winning race
                            String name = ac.getFullSpeciesName();
                            if (!winningSpecies.contains(name)) {
                                // Add it
                                winningSpecies.add(name);
                            }
                            winner = ac;
                        }
                    }

                    // TODO/BUG: This bestowes maxtech on only one alien of the wining species
                    if (winningSpecies.size() == 1) { // If there is no tie
                        // The winner's tech is brought up to the max in the group
                        winner.tech = maxTech;
                    }

                    for (AlienContainer ac : fightingAliens) {
                        // If the alien was not part of one of the winning species
                        if (!winningSpecies.contains(ac.getFullName())) {

                            // If the alien was beaten by more than five energy points
                            if (winningPower > ac.currentActionPower + 5) {

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
                    // there is an arbitrary border at 32 to keep views under control
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
                            thisAlien.tech);   // tech inherited undiminished from parent

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

    void addAlien(int x, int y, String domainName, String alienPackageName, String alienClassName, Constructor<?> cns) {
        AlienContainer ac = new AlienContainer(this, this.vis, x, y, domainName, alienPackageName, alienClassName, cns, 1, 1);
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
