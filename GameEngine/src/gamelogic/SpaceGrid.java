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
    public List<AlienContainer> aliens;    // Aliens are born and die, so
    // our list needs to be able to grow and shrink

    List<SpaceObject> objects; // Stars, planets, space stations, etc.

    static Random rand = new Random(System.currentTimeMillis());

    public boolean executeGameTurn() {
        moveAliens();
        removeDeadAliens();
        performAlienActions();
        removeDeadAliens();
        resetMovesAndFights();

        AlienContainer aUniqueAlien = null;
        for (AlienContainer a : aliens) {
            if (a != null) {
                if (aUniqueAlien != null) {
                    if (aUniqueAlien.alienConstructor != a.alienConstructor) {
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
        
        for (AlienContainer a : aliens) {
            if (a != null) {
                vis.debugErr("Alien " + a.alienPackageName + ":" + a.alienClassName + "("
                        + Integer.toHexString(a.alien.hashCode()).toUpperCase() + "): "
                        + "X:" + (a.x)
                        + " Y:" + (a.y)
                        + " E:" + (a.energy)
                        + " T:" + (a.tech)
                        + " r:" + ((int) Math.floor(Math.hypot((double) a.x, (double) a.y))));
            }
        }
        vis.debugErr("");
    }

    public SpaceGrid(GameVisualizer vis) {
        this.vis = vis;
        aliens = new ArrayList<>(); // AlienContainer type is inferred
        objects = new ArrayList<>();
    }

    public void moveAliens() {
        vis.debugOut("Moving " + (aliens.size()) + " aliens");
        for (int i = 0; i < aliens.size(); i++) {
            ViewImplementation view = getAlienView(i);
            try {
                aliens.get(i).move(view);
            } catch (Exception ex) {
                aliens.get(i).debugErr("Unhandled exception in move: " + ex.toString());
                aliens.get(i).kill();
            }
        }
    }

    public void removeDeadAliens() {
        int number = aliens.size();
        for (int i = number - 1; i >= 0; i--) {
            if (aliens.get(i).energy <= 0) {
                vis.showDeath(aliens.get(i).alienPackageName, aliens.get(i).alienClassName, aliens.get(i).alien.hashCode(),
                        aliens.get(i).x, aliens.get(i).y);
                aliens.remove(i);
            }
        }
    }

    public void resetMovesAndFights() {
        aliens.stream().forEach((alien) -> {
            alien.action = false;
            alien.fought = false;
        });
    }

    public void performAlienActions() {
        int number = aliens.size();

        vis.debugOut("Processing actions for " + (number) + " aliens");

        // Obtain all actions before enacting any of them
        Action[] actions = new Action[number];

        for (int i = 0; i < number; i++) {
            AlienContainer thisAlien = aliens.get(i);

            ViewImplementation view = getAlienView(i);

            try {
                //thisAlien.api.debugOut("Query action ...");
                // Note: getAction() checks validity
                actions[i] = thisAlien.getAction(view);
                //thisAlien.api.debugOut(actions[i].code.name());

            } catch (Exception ex) {
                // if an alien blows up here, we'll kill it. 
                actions[i] = new Action(ActionCode.None);
                thisAlien.debugErr("Blew up on getAction(): " + ex.toString());
                thisAlien.kill();
            }
        }

        // now process all actions
        for (int i = 0; i < actions.length; i++) {
            Action thisAction = actions[i];
            AlienContainer thisAlien = aliens.get(i);
            thisAlien.debugOut(thisAction.code.toString());

            switch (thisAction.code) {
                case Fight:
                    //vis.debugOut("SpaceGrid: Processing Fight");
                    // Note: You can fight with aliens of your own species
                    if (aliens.get(i).fought) {
                        break;
                    }

                    List<Integer> fightingAliens = new ArrayList<>(); // Stores indexes
                    List<String> fightingRaces = new ArrayList<String>();
                    List<Integer> fightingPowers = new ArrayList<Integer>();

                    fightingAliens.add(i);
                    fightingRaces.add(thisAlien.alienPackageName + ":"
                            + thisAlien.alienClassName
                            + "(" + Integer.toHexString(thisAlien.alien.hashCode()).toUpperCase() + ")");
                    fightingPowers.add(thisAction.power);

                    int energyForWinner = 0;
                    thisAlien.fought = true;

                    for (int k = i + 1; k < actions.length; k++) {
                        AlienContainer theOtherAlien = aliens.get(k);
                        Action theOtherAction = actions[k];

                        if (theOtherAlien.x == thisAlien.x
                                && theOtherAlien.y == thisAlien.y) {

                            // If an alien didn't choose to fight at all
                            // (even if they fought with zero power)
                            // they will get blown off the board and the alien
                            // that won the fight will take their energy
                            // TODO: consider whether they should be killed or just lose
                            theOtherAlien.fought = true;
                            fightingAliens.add(k);
                            fightingRaces.add(theOtherAlien.alienPackageName + ":"
                                    + theOtherAlien.alienClassName
                                    + "(" + Integer.toHexString(theOtherAlien.alien.hashCode()).toUpperCase() + ")");
                            fightingPowers.add(theOtherAction.power);

                            if (theOtherAction.code != ActionCode.Fight) {
                                //theOtherAlien.api.debugOut("Was a pacifist at the wrong time and place.");

                                energyForWinner += theOtherAlien.energy;
                                theOtherAlien.kill();
                            } else {
                                fightingAliens.add(k);
                            }
                        }
                    }

                    String[] fRs = new String[fightingRaces.size()];
                    Integer[] fPs = new Integer[fightingPowers.size()];

                    vis.showFightBefore(aliens.get(i).x, aliens.get(i).y, fightingRaces.toArray(fRs), fightingPowers.toArray(fPs));

                    // Determine the winner and maximum tech in the fight
                    int winner = 0;
                    int maxTech = 0;
                    for (int k = 0; k < fightingAliens.size(); k++) {
                        // Winner
                        if (actions[fightingAliens.get(k)].power
                                > actions[fightingAliens.get(winner)].power) {
                            winner = k;
                        }

                        // Max tech
                        if (aliens.get(fightingAliens.get(k)).tech > maxTech) {
                            maxTech = aliens.get(fightingAliens.get(k)).tech;
                        }
                    }

                    // The winner's tech is brought up to the max in the group
                    aliens.get(fightingAliens.get(winner)).tech = maxTech;

                    for (int k = 0; k < fightingAliens.size(); k++) {
                        // If the alien is a loser and of a different species than the winner
                        if (k != winner
                                && aliens.get(fightingAliens.get(k)).alienConstructor
                                == aliens.get(fightingAliens.get(winner)).alienConstructor) {

                            // If the alien was beaten by more than five energy points
                            if (actions[fightingAliens.get(winner)].power
                                    < actions[fightingAliens.get(k)].power + 5) {

                                // GM : I took this out because it makes it too good for the winner
                                // The winning alien will get their energy
                                // energyForWinner += aliens.get(fightingAliens.get(k)).energy;
                                // GM: Also, you never gave it to the winner anywhere

                                // The alien will then be killed
                                // aliens.get(k).api.debugOut("Lost and died of its wounds.");
                                aliens.get(fightingAliens.get(k)).kill();

                            } else { // If the alien was beaten by less than 5 points
                                // They lose 5 points of technology
                                // However, their tech cannot go below 1
                                aliens.get(fightingAliens.get(k)).tech
                                        = Math.max(
                                                aliens.get(fightingAliens.get(k)).tech - 5,
                                                1);
                            }
                        }
                        // TODO: Write showFightAfter here
                    }
                    break;

                case Gain:
                    thisAlien.energy += Math.floor(thisAlien.tech / 10) + 1;
                    break;

                case Research:
                    thisAlien.energy -= thisAlien.tech;
                    thisAlien.tech++; // you only gain 1 tech regardless of energy invested
                    break;

                case Spawn:
                    thisAlien.energy -= thisAlien.api.getSpawningCost() + thisAction.power;

                    // construct a random move for the new alien depending on power and send that move through drift correction
                    // spend thisAction.power randomly on x move, y move and initital power
                    int power = rand.nextInt(thisAction.power);
                    thisAction.power -= power;
                    int x = rand.nextInt(thisAction.power) - thisAction.power;
                    thisAction.power -= x;
                    int y = thisAction.power * (rand.nextInt(2) == 0 ? 1 : -1);

                    MoveDir dir = new MoveDir(x, y);
                    dir = thisAlien.applyDrift(thisAlien.x, thisAlien.y, dir);

                    // make a container, it will create the alien
                    AlienContainer aC = new AlienContainer(
                            this.vis,
                            thisAlien.x + dir.x(),
                            thisAlien.y + dir.y(),
                            thisAlien.alienPackageName,
                            thisAlien.alienClassName,
                            thisAlien.alienConstructor,
                            power,
                            thisAlien.tech);   // tech inherited undiminished from parent
                    
                    // Add in the alien to the end of the list so actions
                    // are not executed on it this turn
                    aliens.add(aC);

                    // visualize
                    vis.showSpawn(aC.alienPackageName, aC.alienClassName, aC.alien.hashCode(),
                            aC.x, aC.y, aC.energy, aC.tech);
                    break;
            }
            // this ends our big switch statement
        }
    }

    private ViewImplementation getAlienView(int index) {
        // Create the alien's view
        int size = aliens.get(index).tech;
        int cornerX = aliens.get(index).x - size;
        int cornerY = aliens.get(index).y - size;
        // Bottom left corner
        List<AlienContainer> visibleAliens = new ArrayList<>();
        List<SpaceObject> visibleObjects = new ArrayList<>();

        for (int k = 0; k < aliens.size(); k++) {
            if (k != index) { // The alien does not show itself on the map
                AlienContainer alien = aliens.get(k);
                if ( // If the alien can be seen
                        alien.x >= cornerX
                        && alien.x <= cornerX + size * 2
                        && alien.y >= cornerY
                        && alien.y <= cornerY + size * 2) {
                    visibleAliens.add(alien);
                }
            }
        }
        for (int k = 0; k < objects.size(); k++) {
            SpaceObject obj = objects.get(k);
            if ( // If the alien can be seen
                    obj.x >= cornerX
                    && obj.x <= cornerX + size * 2
                    && obj.y >= cornerY
                    && obj.y <= cornerY + size * 2) {
                visibleObjects.add(obj);
            }
        }

        return new ViewImplementation(visibleAliens, visibleObjects, cornerX, cornerY, size);
    }

    private int maxValueIndex(List<Integer> array) {
        int index = 0;
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) > array.get(index)) {
                index = i;
            }
        }
        return index;
    }

    private int maxValue(int[] array) {
        int max = Integer.MIN_VALUE;
        for (int item : array) {
            if (item > max) {
                max = item;
            }
        }
        return max;
    }

    void addAlien(int x, int y, String alienPackageName, String alienClassName, Constructor<?> cns) {
        AlienContainer aC = new AlienContainer(this.vis, x, y, alienPackageName, alienClassName, cns, 1, 1);
        aliens.add(aC);
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
            addAlien(0, 0, element.packageName, element.className, element.cns);

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
