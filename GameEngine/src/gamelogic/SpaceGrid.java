/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.AlienSpec;
import gameengineinterfaces.GameElementKind;
import java.util.*;
import java.lang.reflect.Constructor;
import alieninterfaces.*;
import gameengineV1.GameEngineV1;
import gameengineinterfaces.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author guberti
 */
public class SpaceGrid {

    GameEngineV1 engine;
    GameVisualizer vis;
    public AlienGrid aliens;    // Aliens are born and die, so our list needs to be able to grow and shrink
    List<SpaceObject> objects;  // Stars, planets, space stations, etc.
    public HashMap<String, InternalAlienSpecies> speciesMap; // Maps alienSpeciesNames to indexes
    int width;
    int height;
    int safeZoneSize = 5; // Number of tiles out of 0,0 that should be protected
    // sZS = 0 gives a 1x1 sz, sZS = 1 gives a 3x3 sz, sZS = 5 gives a 11x11 sz

    int currentTurn = 1;
    int speciesCounter = 1; // starting with ID 1

    // this is the only approved random generator for the game. Leave it alone!
    public static Random rand;
    public static int randSeed = 0;
    public static boolean chatter = false;

    public SpaceGrid(GameEngineV1 eng, GameVisualizer vis, int width, int height) {
        this.vis = vis;
        this.width = width;
        this.height = height;
        this.engine = eng;
        aliens = new AlienGrid(width, height); // AlienContainer type is inferred
        objects = new ArrayList<>();
        speciesMap = new HashMap<>();

        rand = new Random(0);
    }

    public void ready() {
        // send the whole energyMap to the display
        for (int x = -aliens.centerX; x < aliens.centerX; x++) {
            for (int y = -(aliens.centerY); y < (aliens.centerY); y++) {
                vis.mapEnergy(x, y, aliens.getEnergyAt(x, y));
            }
        }
        vis.showReady();
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
            ac.energy -= Math.abs(ac.x - oldX) + Math.abs(ac.y - oldY);
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
            acell.energyPerAlien = 0;
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

                    // If the alien is in the safe zone
                    if (Math.abs(thisAlien.x) <= safeZoneSize
                            && Math.abs(thisAlien.y) <= safeZoneSize) {
                        // Make them pay the energy they spent
                        thisAlien.energy -= thisAlien.currentActionPower;
                        break;
                    }
                    List<AlienSpec> fightSpecs = new ArrayList<>();
                    HashMap<String, Double> fightSpecies = new HashMap<>();

                    LinkedList<AlienContainer> fightingAliens = aliens.getAliensAt(thisAlien.x, thisAlien.y);

                    for (AlienContainer fightingAlien : fightingAliens) {

                        if (fightingAlien.currentActionCode != ActionCode.Fight) {
                            fightingAlien.currentActionPower = 0;
                        }

                        Double speciesCombinedPower = fightSpecies.get(fightingAlien.speciesName);
                        if (speciesCombinedPower == null) {
                            speciesCombinedPower = 0.0;
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

                    if (fightSpecies.size() < 2) { // If only one species is fighting
                        break; // No fight will take place
                        // Aliens involved will lose their turns
                    }

                    // Deduct energy from all aliens and set their fought status
                    for (AlienContainer fightingAlien : fightingAliens) {
                        fightingAlien.fought = true;
                        fightingAlien.energy -= fightingAlien.currentActionPower;
                    }

                    vis.showFightBefore(thisAlien.x, thisAlien.y, fightSpecs);

                    // Determine the winning power in the fight
                    double winningPower = 0; // The fight powers might tie

                    Set set = fightSpecies.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry mentry = (Map.Entry) iterator.next();
                        winningPower = Math.max(winningPower, (double) mentry.getValue());
                    }

                    double maxTech = 0;

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
                            if (ac.speciesName.equals(winningSpecies.get(0))) {
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
                    AlienCell acs = aliens.getAliensAt(thisAlien.x, thisAlien.y);
                    if (acs.energyPerAlien == 0) {
                        acs.energyPerAlien = acs.energy / acs.size(); // aliens have to share harvest in one spot
                    }

                    thisAlien.energy += acs.energyPerAlien * thisAlien.tech / 10;
                    break;

                case Research:
                    // there is an arbitrary ceiling at 32 to keep views under control
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
                    
                    double power = Math.max(Math.min(rand.nextInt((int)thisAlien.currentActionPower), thisAlien.currentActionPower / 3), 1);
                    double powerForX = rand.nextInt((int)(thisAlien.currentActionPower - power));
                    double powerForY = rand.nextInt((int)(thisAlien.currentActionPower - power - powerForX));

                    int x = (int)powerForX * (rand.nextInt(2) == 0 ? 1 : -1);
                    int y = (int)powerForY * (rand.nextInt(2) == 0 ? 1 : -1);

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
                            thisAlien.species,
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

    void addSpecies(GameElementSpec element) {
        String speciesName = element.domainName + ":" + element.packageName + ":" + element.className;

        // add species if necessary
        InternalAlienSpecies as = speciesMap.get(speciesName);
        if (as == null) {

            as = new InternalAlienSpecies(element.domainName, element.packageName, element.className, speciesCounter);
            speciesMap.put(speciesName, as);
            speciesCounter++;
            vis.registerSpecies(new AlienSpec(as));
        }
    }

    // TODO: add spawn state, tech, energy
    void addAlien(int x, int y, String domainName, String alienPackageName, String alienClassName) {

        String speciesName = domainName + ":" + alienPackageName + ":" + alienClassName;

        // add species if necessary
        InternalAlienSpecies as = speciesMap.get(speciesName);
        if (as == null) {
            as = new InternalAlienSpecies(domainName, alienPackageName, alienClassName, speciesCounter);
            speciesMap.put(speciesName, as);
            speciesCounter++;
            vis.registerSpecies(new AlienSpec(as));
        }

        if (as.cns == null) {
            // Load constructor
            try {
                as.cns = Load(engine, alienPackageName, alienClassName);
            } catch (Exception e) {
                vis.debugErr("sg.addAlien: Error loading contructor for " + speciesName);
                //throw (e);
            }
        }

        AlienContainer ac = new AlienContainer(this, this.vis, x, y,
                domainName, alienPackageName, alienClassName, as.cns, as,
                1, 1, // tech and power
                0, // no parent
                null); // no spawn state
        aliens.addAlienAndPlug(ac);
        vis.showSpawn(ac.getFullAlienSpec());
    }

    // Note: Having a seperate method for each SpaceObject seems a little gross,
    // as there is already a large if statement in addElement and the code in
    // addPlanet and addStar is nearly identical
    void addPlanet(GameElementSpec element) {
        Planet p = new Planet(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech, element.parent);
        objects.add(p);
        this.aliens.plugPlanet(p);
        vis.registerPlanet(element.x, element.y, element.className, element.energy, element.tech);
    }

    void addStar(GameElementSpec element) {
        Star st = new Star(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech);
        objects.add(st);
        this.aliens.plugStar(st);
        vis.registerStar(element.x, element.y, element.className, element.energy);
        aliens.distributeStarEnergy(element.x, element.y, element.energy);
    }

    void addResident(GameElementSpec element) {
        Resident r = new Resident(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech, element.parent);
    }

    public void addElement(GameElementSpec element) throws IOException {
        // can't use switch here because switch on enum causes weird error
        // if you doubt that, uncomment this line:
        // switch (element.kind) {}

        if (element.kind == GameElementKind.ALIEN) {
            // position (0,0) leads to random assignment
            addAlien(0, 0, element.domainName, element.packageName, element.className);

        } else if (element.kind == GameElementKind.SPECIES) {
            addSpecies(element);
        } else if (element.kind == GameElementKind.PLANET) {
            addPlanet(element);
        } else if (element.kind == GameElementKind.STAR) {
            addStar(element);
        } else if (element.kind == GameElementKind.RESIDENT) {
            addResident(element);
        }
    }

    //
    // Dynamic class loader (.jar files)
    // stolen from StackOverflow, considered dark voodoo magic
    //
    /**
     * Parameters of the method to add an URL to the System classes.
     */
    private static final Class<?>[] parameters = new Class[]{URL.class};

    /**
     * Adds the content pointed by the file to the class path.
     *
     */
    public static void addClassPathFile(String gamePath, String alienPath, String packageName) throws IOException {
        String fullName;

        if (packageName.equalsIgnoreCase("stockaliens")
                || packageName.equalsIgnoreCase("alieninterfaces")) {
            fullName = gamePath
                    + System.getProperty("file.separator")
                    + packageName
                    + System.getProperty("file.separator")
                    + "dist"
                    + System.getProperty("file.separator")
                    + packageName + ".jar";
        } else {
            fullName = alienPath + packageName + ".jar";
        }

        URL url = new File(fullName).toURI().toURL();
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{url});
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("GameElementThread: Error, could not add URL to system classloader");
        }
        //engine.vis.debugOut("GameElementThread: package " + packageName + " added as " + fullName);

    }

    public static Constructor<?> Load(GameEngineV1 engine, String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> cs = null;

        try {
            addClassPathFile(engine.gameJarPath, engine.alienPath, packageName);
            cs = ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className).getConstructor();
        } catch (Exception e) {
            e.printStackTrace();
            //vis.debugErr("GameElementThread: Error: Could not get constructor");
            throw e;
        }
        return cs;
    }

}
