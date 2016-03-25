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
    List<AlienContainer> aliens;    // Aliens are born and die, so
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
        vis.debugOut("");
        vis.debugOut("Current Aliens:");

        for (AlienContainer a : aliens) {
            if (a != null) {
                a.api.debugOut("X:" + Integer.toString(a.x)
                        + " Y:" + Integer.toString(a.y)
                        + " E:" + Integer.toString(a.energy)
                        + " T:" + Integer.toString(a.tech));
            }
        }
        vis.debugOut("");
    }

    public SpaceGrid(GameVisualizer vis) {
        this.vis = vis;
        aliens = new ArrayList<>(); // AlienContainer type is inferred
        objects = new ArrayList<>();
    }

    public void moveAliens() {
        for (int i = 0; i < aliens.size(); i++) {
            ViewImplementation view = getAlienView(i);
            try {
                aliens.get(i).move(view);

            } catch (Exception ex) {
                //Logger.getLogger(SpaceGrid.class.getName()).log(Level.SEVERE, null, ex);
                aliens.get(i).api.debugOut("Unhandled exception in move: ");
                aliens.get(i).api.debugOut(ex.getMessage());
                //ex.printStackTrace();
                aliens.remove(i);
            }
        }

        /* GM: TODO: I believe this is obsolete code. Gavin?
        
         // Once the aliens have moved, have them fight each other if they are in
         // the same game space

         // Note: We can't use an enhanced for loop here, because we need the
         // AC's index in case it crashes and needs to be destroyed
         for (int i = 0; i < aliens.size(); i++)
         {
         // If there has not already been a fight on the alien's space
         if (!aliens.get(i).action)
         {

         // We need alienIndices in order to destroy an alien if it 
         // crashes
         List<Integer> alienIndices = new ArrayList<>();

         // For each alien that it could fight with
         for (int k = i + 1; k < aliens.size(); k++)
         {
         if ( // If two aliens are on the same space 
         aliens.get(i).x == aliens.get(k).x
         && aliens.get(i).y == aliens.get(k).y)
         {

         // They need to fight, so add them to fightingAliens
         alienIndices.add(k);
         }
         }

         if (alienIndices.size() > 0)
         { // If a battle will happen
                    
         int fightX = aliens.get(i).x;
         int fightY = aliens.get(i).y;
                    
         // Add the initial alien to the fight
         alienIndices.add(i);

         // For each alien, ask it how much it wants to fight
         // Also, get its object information to log
         int[] fightPowers = new int[alienIndices.size()];
         String[] debugStrs = new String[alienIndices.size()];

         for (int k = 0; k < alienIndices.size(); k++)
         {
         try
         {
         fightPowers[k] = aliens.get(alienIndices.get(k)).fight();
         } catch (Exception ex)
         {
         fightPowers[k] = 0;
         aliens.get(alienIndices.get(k)).kill();
         Logger.getLogger(SpaceGrid.class.getName()).log(Level.SEVERE, null, ex);
         }
                        
         // Package and class
         debugStrs[k] = aliens.get(k).alienClassName + ":" + 
         aliens.get(k).alienPackageName;
                        
         // Unique object hash
         debugStrs[k] += "("+Integer.toHexString(aliens.get(k)
         .alien.hashCode()).toUpperCase()+")";
                        
         }

         // All fighting prerequisites have been taken care of

         vis.showFightBefore(fightX, fightY, debugStrs, fightPowers);
                    
         int winningEnergy = maxValue(fightPowers);

         for (int power : fightPowers)
         {
         if (power < winningEnergy)
         { // If the alien lost the fight

         } else if (power == winningEnergy)
         { // If the alien tied 

         } else
         { // If the alien won

         }
         }
         }
         }
         }
         */
    }

    public void removeDeadAliens() {
        for (int i = 0; i < aliens.size(); i++) {
            if (aliens.get(i).energy <= 0) {
                // running this through the API automatically logs alien info
                aliens.get(i).api.debugOut("Ran out of energy and died.");
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

        // Obtain all actions before enacting any of them
        Action[] actions = new Action[aliens.size()];

        for (int i = 0; i < aliens.size(); i++) {
            ViewImplementation view = getAlienView(i);

            try {
                // Note: getAction() checks validity
                actions[i] = aliens.get(i).getAction(view);

            } catch (Exception ex) {
                // if an alien blows up here, we'll kill it. 
                actions[i] = new Action(ActionCode.None);
                aliens.get(i).api.debugOut("Blew up on getAction()");
                aliens.get(i).kill();
            }
        }

        for (int i = 0; i < actions.length; i++) {
            switch (actions[i].code) {
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
                    fightingRaces.add(aliens.get(i).alienPackageName + ":"
                            + aliens.get(i).alienClassName
                            + "(" + Integer.toHexString(aliens.get(i).alien.hashCode()).toUpperCase() + ")");
                    fightingPowers.add(actions[i].power);

                    int energyForWinner = 0;
                    aliens.get(i).fought = true;

                    for (int k = i + 1; k < aliens.size(); k++) {
                        if (aliens.get(k).x == aliens.get(i).x
                                && aliens.get(k).y == aliens.get(i).y) {

                            // If an alien didn't choose to fight at all
                            // (even if they fought with zero power)
                            // they will get blown off the board and the alien
                            // that won the fight will take their energy
                            // TODO: consider whether they should be killed or just lose
                            aliens.get(k).fought = true;
                            fightingAliens.add(k);
                            fightingRaces.add(aliens.get(k).alienPackageName + ":"
                                    + aliens.get(k).alienClassName
                                    + "(" + Integer.toHexString(aliens.get(k).alien.hashCode()).toUpperCase() + ")");
                            fightingPowers.add(actions[k].power);

                            if (actions[k].code != ActionCode.Fight) {
                                //aliens.get(k).api.debugOut("Was a pacifist at the wrong time and place.");

                                energyForWinner += aliens.get(k).energy;
                                aliens.get(k).kill();
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

                                // The winning alien will get their energy
                                energyForWinner += aliens.get(fightingAliens.get(k)).energy;

                                // The alien will then be killed
                                //aliens.get(k).api.debugOut("Lost and died of its wounds.");
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

                case Gain:
                    aliens.get(i).energy += Math.floor(aliens.get(i).tech / 10) + 1;
                    continue;
                case Research:
                    aliens.get(i).energy -= aliens.get(i).tech; //TODO: This doesn't make much sense
                    aliens.get(i).tech++; // you only gain 1 tech regardless of energy invested
                    continue;
                case Spawn:
                    aliens.get(i).energy -= 3 + actions[i].power;

                    // Add in the alien to the end of the list so actions
                    // are not executed on it this turn
                    // TODO add code to spawn species relevant offspring here
                    // e.g. Alien alien = new Martian();
                    aliens.add(new AlienContainer(
                            this.vis,
                            aliens.get(i).x + rand.nextInt(6) - 3, // TODO: hard-coded constant  
                            aliens.get(i).y + rand.nextInt(6) - 3, // need to be justified or moved somewhere central
                            aliens.get(i).alienPackageName,
                            aliens.get(i).alienClassName,
                            aliens.get(i).alienConstructor,
                            actions[i].power, 1)); // initial tech = 1;
            }
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
