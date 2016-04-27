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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author guberti
 */
public class SpaceGrid {

    GameEngineV1 engine;
    GameVisualizer vis;
    public AlienGrid aliens;    // Aliens are born and die, so our list needs to be able to grow and shrink
    List<InternalSpaceObject> objects;  // Stars, planets, space stations, etc.
    public HashMap<String, InternalAlienSpecies> speciesMap; // Maps alienSpeciesNames to indexes
    int width;
    int height;

    int currentTurn = 1;
    int speciesCounter = 1; // starting with ID 1

    // this is the only approved random generator for the game. Leave it alone!
    public static Random rand;

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
        // this is also a good spot to 
        // prepare the random generator seed number
        // if not set from config file
        if (Constants.randSeed == 0) {
            Constants.randSeed = (int) System.nanoTime();
        }

        SpaceGrid.rand.setSeed(Constants.randSeed);
        // output randSeed into logfile
        vis.debugOut("RandSeed: " + Constants.randSeed);

        vis.setFilter(Constants.filters);
        vis.setChatter(Constants.chatter);

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

        beThoughtful();

        currentTurn++;

        AlienContainer aUniqueAlien = null;
        for (AlienContainer a : aliens) {
            if (a != null) {
                if (aUniqueAlien != null) {
                    if (!aUniqueAlien.speciesName.equalsIgnoreCase(a.speciesName)) {
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

        for (AlienContainer ac : aliens) {
            if (ac != null) {
                vis.debugOut("Alien " + ac.toString());
            }
        }
        vis.debugOut("");
    }

    public void performCommunications() {
        // phase 1: take outgoing messages and store in ac
        for (AlienContainer ac : aliens) {
            // get rid of stale views from prior phases
            ac.ctx.view = null;

            // call the alien to communicate
            try {
                ac.alien.communicate();
            } catch (UnsupportedOperationException e) {
                // we'll let that go
            } catch (Exception ex) {
                displayException("communicate()", ex);
                ac.kill("Death for unhandled exception in communicate(): " + ex.toString());
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
                        displayException("Unhandled exception in receive(): ", ex);
                        ac.kill("Death for unhandled exception in receive(): " + ex.toString());
                    }
                }
            }
        }
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
            } catch (UnsupportedOperationException e) {
                // we'll let that go
            } catch (Exception ex) {
                displayException("Unhandled exception in getMove(): ", ex);
                ac.kill("Death for unhandled exception in getMove(): " + ex.toString());
            }
            
            // charge only for moves > 1 in either direction
            int moveLength = GridCircle.distance(ac.x, ac.y, oldX, oldY - oldX);
            if (Math.abs(ac.x - oldX) > 1 || Math.abs(ac.y - oldY) >1) {
                ac.energy -= moveLength;
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

            // need to go through all the rest to mark cell fresh for display, 
            // TODO: Fix this in visualizer instead, go away from fillRect and to painting individual cells.
            double oldEnergy = 0;
            AlienCell acs = aliens.getAliensAt(oldX, oldY);
            if (acs != null) {
                oldEnergy = acs.stream().map((alien) -> alien.energy).reduce(oldEnergy, (accumulator, _item) -> accumulator + _item);
            }
            oldEnergy += aliens.getEnergyAt(oldX, oldY);

            double newEnergy = 0;
            acs = aliens.getAliensAt(ac.x, ac.y);
            newEnergy = acs.stream().map((alien) -> alien.energy).reduce(newEnergy, (accumulator, _item) -> accumulator + _item);
            newEnergy += aliens.getEnergyAt(ac.x, ac.y);

            // call shell visualizer
            // just make sure we don't blow up the alien beacuse of an exception in the shell
            try {
                vis.showMove(ac.getFullAlienSpec(),
                        oldX,
                        oldY,
                        newEnergy,
                        oldEnergy);
            } catch (Exception e) {
                displayException("Unhandled exception in showMove(): ", e);

            }

            if (acs.star != null) {
                ac.kill("Death for moving into a star");

            }
        }

    }

    public void removeDeadAliens() {
        for (Iterator<AlienContainer> iterator = aliens.iterator(); iterator.hasNext();) {
            AlienContainer ac = iterator.next();
            if (ac.energy <= 0) {

                double newEnergy = 0;
                AlienCell acs = aliens.getAliensAt(ac.x, ac.y);
                if (acs != null) {
                    newEnergy = acs.stream().map((alien) -> alien.energy).reduce(newEnergy, (accumulator, _item) -> accumulator + _item);
                }
                newEnergy += aliens.getEnergyAt(ac.x, ac.y);

                vis.showDeath(ac.getFullAlienSpec(), newEnergy);
                aliens.unplug(ac);
                iterator.remove();
            }
        }
    }

    public void resetAliens() {
        aliens.stream().forEach((alien) -> {
            alien.participatedInAction = false;
            alien.ctx.view = null;
            alien.outgoingMessage = null;
            alien.listening = false;

            AlienCell acell = alien.grid.aliens.getAliensAt(alien.x, alien.y);
            acell.listening = false;
            acell.currentMessages = null;
            acell.energyPerAlien = 0;
        });
    }

    public void beThoughtful() {
        for (AlienContainer ac : aliens) {
            // get rid of stale views from prior moves
            ac.ctx.view = null;

            // call the alien to think on the state of the universe
            try {
                ac.beThoughtful();
            } catch (Exception ex) {
                // any exceptions except NotSupported get the alien killed here
                displayException("Unhandled exception in beThoughtful(): ", ex);

                ac.kill("Death for unhandled exception in beThoughtful(): " + ex.toString());
            }
        }
    }

    public void performAlienActions() {
        LinkedList<AlienSpec> newAliens = new LinkedList();

        //vis.debugOut("Processing actions for " + (aliens.size()) + " aliens");
        // first request all the actions from the aliens
        for (AlienContainer thisAlien : aliens) {
            // get rid of stale views from prior phases
            thisAlien.ctx.view = null;

            try {
                // Note: getAction() checks validity
                thisAlien.getAction();
                //thisAlien.api.debugOut(thisAlien.currentAction.name());
            } catch (UnsupportedOperationException e) {
                // we'll let that go, but it sits idle
                thisAlien.currentActionCode = Action.ActionCode.None;
                thisAlien.currentActionPower = 0;
            } catch (Exception ex) {
                // for all other exceptions, we'll kill it. 
                thisAlien.currentActionCode = Action.ActionCode.None;
                thisAlien.currentActionPower = 0;
                displayException("Unhandled exception in getAction(): ", ex);
                thisAlien.kill("Death for unhandled exception in getAction(): " + ex.toString());
            }
        }

        // now process all actions
        for (AlienContainer thisAlien : aliens) {
            //thisAlien.debugOut(thisAlien.currentActionCode.toString());

            // if this guy was part of a fight, don't bother with more actions
            if (thisAlien.participatedInAction) {
                continue;
            }

            switch (thisAlien.currentActionCode) {
                case Fight:
                    //vis.debugOut("SpaceGrid: Processing Fight");

                    // If the alien is in the safe zone
                    if (Math.abs(thisAlien.x) <= Constants.safeZoneRadius
                            && Math.abs(thisAlien.y) <= Constants.safeZoneRadius) {
                        // Make them pay the energy they spent
                        thisAlien.energy -= thisAlien.currentActionPower + Constants.fightingCost;
                        // but don't let them fight
                        break;
                    }
                    List<AlienSpec> fightSpecs = new ArrayList<>();
                    HashMap<String, Double> fightSpecies = new HashMap<>();

                    LinkedList<AlienContainer> fightingAliens = aliens.getAliensAt(thisAlien.x, thisAlien.y);

                    for (AlienContainer fightingAlien : fightingAliens) {

                        if (fightingAlien.currentActionCode != Action.ActionCode.Fight
                                && fightingAlien.currentActionCode != Action.ActionCode.TradeOrDefend) {
                            fightingAlien.currentActionPower = 0;
                        }

                        Double speciesCombinedPower = fightSpecies.get(fightingAlien.speciesName);
                        if (speciesCombinedPower == null) {
                            speciesCombinedPower = 0.0;
                        }

                        speciesCombinedPower += fightingAlien.currentActionPower;
                        fightSpecies.put(
                                fightingAlien.speciesName,
                                fightingAlien.currentActionPower);

                        fightSpecs.add(fightingAlien.getFullAlienSpec());
                    }

                    if (fightSpecies.size() < 2) { // If only one species is fighting
                        break; // No fight will take place
                        // Aliens involved will lose their turns
                    }

                    // Deduct energy from all aliens and set their participatedInAction status
                    for (AlienContainer fightingAlien : fightingAliens) {
                        fightingAlien.participatedInAction = true;
                        fightingAlien.energy -= Constants.fightingCost;
                        fightingAlien.energy -= fightingAlien.currentActionPower;
                        if (fightingAlien.energy <= 0) {
                            fightingAlien.kill("Death for investing too much in fighting");
                        }
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
                            if (winningPower > fightSpecies.get(ac.speciesName) + Constants.deathThreshold) {

                                // The alien will then be killed
                                ac.kill("Death in fight by losing by more than " + Constants.deathThreshold + " energy points");

                            } else { // If the alien was beaten by less than 5 points
                                // They go down to two technology
                                ac.tech = 2;
                            }
                        }
                        vis.showFightAfter(thisAlien.x, thisAlien.y, fightSpecs);
                    }
                    break;
                case TradeOrDefend:
                    // If a trade or fight has already occurred
                    if (thisAlien.participatedInAction) {
                        break;
                    }
                    // Make a SHALLOW copy of the aliens at the given position
                    // As long as we don't mutilate the aliens, we can mutilate
                    // the list as much as we want
                    List<AlienContainer> aliensAtPos = (List<AlienContainer>) aliens.getAliensAt(thisAlien.x, thisAlien.y).clone();
                    for (AlienContainer ac : aliensAtPos) {
                        if (ac.currentActionCode == Action.ActionCode.Fight) {
                            break; // No trades can take place if there is war
                        }
                    }
                    // A trade will now take place
                    // Everyone who activated defenses now pays to keep them active
                    for (AlienContainer ac : aliensAtPos) {
                        ac.energy -= 2;
                    }
                    // Trading will now commence
                    // First sort aliens by tech
                    Comparator c = new Comparator<AlienContainer>() {
                        @Override
                        public int compare(AlienContainer ac1, AlienContainer ac2) {
                            return (int) (ac1.tech - ac2.tech);
                        }
                    };
                    Collections.sort(aliensAtPos, c);
                    // aliensAtPos is now sorted by tech
                    while (aliensAtPos.size() > 0) {
                        AlienContainer buyingAlien = aliensAtPos.get(0);
                        // Lowest tech alien is at position 0
                        for (int i = aliensAtPos.size() - 1; i >= 1; i--) {
                            AlienContainer sellingAlien = aliensAtPos.get(i);
                            // Iterate through the aliens with highest tech first
                            if (buyingAlien.tech == sellingAlien.tech) {
                                // No trading with people of same/lower tech
                                aliensAtPos.remove(0);
                                break;
                            }

                            // If the trade and buy prices are compatible
                            if (buyingAlien.currentAction.buyPrice
                                    >= sellingAlien.currentAction.sellPrice
                                    && buyingAlien.energy
                                    >= sellingAlien.currentAction.sellPrice) {

                                // Perform the trade
                                buyingAlien.energy -= sellingAlien.currentAction.sellPrice;
                                sellingAlien.energy += sellingAlien.currentAction.sellPrice;
                                buyingAlien.tech++;

                                // Remove both aliens from aliensAtPos
                                aliensAtPos.remove(0);
                                aliensAtPos.remove(i);
                            }
                        }
                    }

                    break;
                case Gain:
                    // todo: dshare power only among those gaining
                    if (thisAlien.energy < Constants.energyCap) {
                        AlienCell acs = aliens.getAliensAt(thisAlien.x, thisAlien.y);
                        if (acs.energyPerAlien == 0) {
                            acs.energyPerAlien = acs.energy / acs.size(); // aliens have to share harvest in one spot
                        }

                        thisAlien.energy += acs.energyPerAlien * thisAlien.tech / Constants.energyGainReducer;
                    }
                    break;

                case Research:
                    // aliens can research up to Constants.researchTechCap, must buy tech beyond that from residents
                    if (thisAlien.tech < Constants.researchTechCap) {
                        // TODO: rewrite this to be able to save fractional energy towards technology
                        thisAlien.energy -= thisAlien.tech;
                        thisAlien.tech++; // you only gain 1 tech regardless of energy invested
                    }
                    break;

                case Spawn:
                    thisAlien.energy -= Constants.spawningCost;
                    if (thisAlien.energy - thisAlien.currentActionPower < 0) {
                        thisAlien.kill("Death by spawning exhaustion - not enough energy to complete.");
                    }

                    // construct a random move for the new alien depending on power and send that move through drift correction
                    // spend thisAction.power randomly on x move, y move and initital power
                    double power = Math.max(Math.min(rand.nextInt((int) thisAlien.currentActionPower), thisAlien.currentActionPower / 3), 1);
                    double powerForX = rand.nextInt((int) (thisAlien.currentActionPower - power));
                    double powerForY = rand.nextInt((int) (thisAlien.currentActionPower - power - powerForX));

                    int x = (int) powerForX * (rand.nextInt(2) == 0 ? 1 : -1);
                    int y = (int) powerForY * (rand.nextInt(2) == 0 ? 1 : -1);

                    thisAlien.energy -= power + powerForX + powerForY;
                    if (thisAlien.energy <= 0) {
                        thisAlien.kill("Death by spawning exhaustion - died in childbirth.");
                    }

                    MoveDir dir = new MoveDir(x, y);
                    dir = thisAlien.containMove(thisAlien.x, thisAlien.y, dir);

                    x = thisAlien.x + dir.x;
                    y = thisAlien.y + dir.y;

                    // Add in the alien spec to a new arraylist
                    // to be processed later so the master list is not disrupted
                    newAliens.add(new AlienSpec(
                            thisAlien.domainName,
                            thisAlien.packageName,
                            thisAlien.className,
                            thisAlien.species.speciesID,
                            thisAlien.alienHashCode, // ugly, overloading the use to communicate parentage
                            x,
                            y,
                            power,
                            thisAlien.tech, // tech inherited undiminished from parent, TODO: This is questionable
                            null,
                            null,
                            thisAlien.currentActionMessage
                    ));

                    break;
            }
            // this ends our big switch statement
        }

        // add all new aliens
        for (AlienSpec as : newAliens) {
            addAlienWithParams(as.x, as.y, as.domainName, as.packageName, as.className,
                    as.hashCode, as.tech, as.energy, as.msg);
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

    void addAlien(int x, int y, String domainName, String alienPackageName, String alienClassName) {
        addAlienWithParams(x, y, domainName, alienPackageName, alienClassName, 0, 1, 1, null);
    }

    void addAlienWithParams(int x, int y, String domainName, String alienPackageName, String alienClassName,
            int parent, double tech, double power, String spawnMsg) {

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
            try {
                as.cns = LoadConstructor(engine, domainName, alienPackageName, alienClassName);
            } catch (Exception e) {
                gridDebugErr("sg.addAlien: Error loading contructor for " + speciesName);
                //throw (e);
            }

        }

        if (as.counter < Constants.perSpeciesCap) {
            try {
                AlienContainer ac = new AlienContainer(this, this.vis, x, y,
                        domainName, alienPackageName, alienClassName, as.cns, as,
                        tech, power, // tech and power
                        parent, // no parent
                        spawnMsg); // no spawn state

                aliens.addAlienAndPlug(ac);

                double newEnergy = 0;
                AlienCell acs = aliens.getAliensAt(ac.x, ac.y);
                if (acs != null) {
                    newEnergy = acs.stream().map((alien) -> alien.energy).reduce(newEnergy, (accumulator, _item) -> accumulator + _item);
                }

                newEnergy += aliens.getEnergyAt(ac.x, ac.y);

                vis.showSpawn(ac.getFullAlienSpec(), newEnergy);
                as.counter++;
            } catch (InstantiationException e) {
                gridDebugErr("sg: could not instantiate new " + domainName + ":" + alienPackageName + ":" + alienClassName);
            }
        }
    }

    // Note: Having a seperate method for each InternalSpaceObject seems a little gross,
    // as there is already a large if statement in addElement and the code in
    // addPlanet and addStar is nearly identical
    void addPlanet(GameElementSpec element) {
        // Todo: adjust position by parent star
        //Planet p = new Planet(this.vis, element.x, element.y, element.packageName, element.className, element.energy, element.tech, element.parent);
        //objects.add(p);
        //this.aliens.plugPlanet(p);
        //vis.registerPlanet(element.x, element.y, element.className, element.energy, element.tech);
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

    public Constructor<?> LoadConstructor(GameEngineV1 engine, String domainName, String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> cs = null;
        String fullName;
        File file;
        URL url;
        Object[] parameters;

        if (packageName.equalsIgnoreCase("stockaliens")
                || packageName.equalsIgnoreCase("alieninterfaces")) {
            fullName = engine.gameJarPath
                    + packageName
                    + System.getProperty("file.separator")
                    + "dist"
                    + System.getProperty("file.separator")
                    + packageName + ".jar";
        } else {
            fullName = engine.alienPath + domainName + System.getProperty("file.separator") + packageName + ".jar";
        }

        try {
            file = new File(fullName);
            url = file.toURI().toURL();
            parameters = new Object[1];
            parameters[0] = url;

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            Object result = method.invoke(classLoader, parameters);

           cs = ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className).getConstructor();
        } catch (Exception e) {
            e.printStackTrace();
            //vis.debugErr("GameElementThread: Error: Could not get constructor");
            throw e;
        }
        return cs;
    }

    // this debugOut is not sensitive to chatter control
    public void gridDebugOut(String s) {
        vis.debugOut(s);
    }

    public void gridDebugErr(String s) {
        vis.debugErr(s);
        if (Constants.pauseOnError) {
            engine.queueCommand(new GameCommand(GameCommandCode.Pause));
        }
    }

    public void displayException(String msg, Exception ex) {
        gridDebugErr(msg + ex.toString());
        //    for (StackTraceElement s : ex.getStackTrace()) {
        //        vis.debugErr(s.toString());
        //    }
    }

    public void addAllCustomAliens() {
        addCustomAliens(engine.alienPath, "<unknown>");
    }

    public void addCustomAliens(String folder, String domain) {
        File[] files = new File(folder).listFiles();

        for (File f : files) {
            if (f.isDirectory()) {
                // recurse
                addCustomAliens(folder + f.getName() + System.getProperty("file.separator"), f.getName());
            } else if (f.getName().toLowerCase().endsWith(".jar")) {
                try {
                    // look for jar files and process
                    List<String> classNames = new ArrayList<String>();
                    ZipInputStream zip = new ZipInputStream(new FileInputStream(f));
                    for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                        if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith("alien.class")) {
                            addSpecies(new GameElementSpec("SPECIES",
                                    domain,
                                    f.getName().replace(".jar", "").toLowerCase(),
                                    entry.getName().substring(entry.getName().indexOf('/') + 1).replace(".class", ""),
                                    "")
                            );
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}
