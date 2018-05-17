/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import gameengineinterfaces.AlienSpec;
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
import orbit.DummyTrajectory;
import orbit.Trajectory;

/**
 *
 * @author guberti
 */
public class SpaceGrid {

    GameEngineV1 engine;
    GameVisualizer vis;
    AlienGrid aliens;    // Aliens are born and die, so our list needs to be able to grow and shrink
    List<InternalSpaceObject> objects;  // Stars, planets, space stations, etc.
    public SpeciesMap<String, InternalAlienSpecies> speciesMap; // Maps alienSpeciesNames to indexes
    int width;
    int height;
    int planetCount = 0;
    int starCount = 0;
    int residentCount = 0;
    int alienCount = 0;
    double avgTech;

    int currentTurn = 1;
    private static double time = 0;
    int speciesCounter = 1; // starting with ID 1

    // this is the only approved random generator for the game. Leave it alone!
    public Random rand;
    
    public SpaceGrid() {}

    public SpaceGrid(GameEngineV1 eng, GameVisualizer vis, int width, int height, Achievement[] achievements) {
        this.vis = vis;
        this.width = width;
        this.height = height;
        this.engine = eng;
        aliens = new AlienGrid(width, height); // AlienContainer type is inferred
        objects = new ArrayList<>();
        speciesMap = new SpeciesMap<>(achievements);

        rand = new Random(0);
    }

    public int getNumAliens() {
        return alienCount;
    }

    public double getTech() {
        return avgTech;
    }

    public double getTime() {
        return time;
    }

    public void ready() {
        // this is also a good spot to
        // prepare the random generator seed number
        // if not set from config file
        if (Constants.randSeed == 0) {
            Constants.randSeed = (int) System.nanoTime();
        }

        rand.setSeed(Constants.randSeed);
        // output randSeed into logfile
        vis.debugOut("RandSeed: " + Constants.randSeed);

        vis.setFilter(Constants.filters);
        vis.setChatter(Constants.chatter);

        // send the whole energyMap to the display
        for (int x = -aliens.centerX; x < aliens.centerX; x++) {
            for (int y = -(aliens.centerY); y < (aliens.centerY); y++) {
                vis.mapEnergy(x, y, aliens.getEnergyAt(new IntegerPosition(x, y)));
            }
        }

        vis.showReady();
    }

    public boolean executeGameTurn() {
        time += Constants.deltaT;

        performCommunications();
        removeDeadAliens();
        performReceives();
        removeDeadAliens();

        requestAlienMoves();
        movePlanets();
        removeDeadAliens();
        reviewInhabitants();

        recordAlienMoves();
        removeDeadAliens();

        requestAlienActions();
        reviewInhabitantActions();

        vis.showUpdateAfterRequestedActions();

        performAlienActions();
        removeDeadAliens();
        resetAliens();

        processResults();
        removeDeadAliens();

        currentTurn++;
        if (currentTurn > Constants.maxTurns) {
            return true;
        }

        if (Constants.lastAlienWins && false) {
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

        return false;
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

    public void performCommunications() { //[Q]
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
            //for (AlienContainer ac : aliens) {
            if (ac.listening) {
                // get rid of stale views from prior phases
                ac.ctx.view = null;

                // call the alien to receive messages
                AlienCell acell = aliens.getAliensAt(ac.p);
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

    public void requestAlienMoves() { //[Q]
        //vis.debugOut("Requesting moves for " + (aliens.size()) + " aliens");
        for (AlienContainer ac : aliens) {
            // get rid of stale views from prior moves
            ac.ctx.view = null;

            // call the alien to move
            try {
                ac.move();
            } catch (UnsupportedOperationException e) {
                // we'll let that go
            } catch (Exception ex) {
                displayException("Unhandled exception in getMove(): ", ex);
                ac.kill("Death for unhandled exception in getMove()/getAccelerate(): " + ex.toString());
            }

        }
    }

    public void movePlanets() {
        for (InternalSpaceObject so : this.objects) {
            if (so.isPlanet) {
                Planet p = (Planet) so;

                // unplug planet from grid
                aliens.unplugPlanet(p);

                Position pOld = p.position;
                p.move();
                Position pNew = p.position;

                // plug planet back into grid
                aliens.plugPlanet(p);



                // now worry about the aliens at the old and new positions
                AlienCell acsFrom = aliens.getAliensAt(pOld.round());
                AlienCell acsTo = aliens.getAliensAt(pNew.round());

                // if aliens are where the planet is moving to, and not landed, they die.
                if (acsTo != null) {
                // we have a cell, let's look at aliens
                    for (AlienContainer ac : acsTo) {
                    // if not landed, and not freshly moved here, you die.
                        if (ac.planet != p && ac.nextP.equals(ac.p)) {
                            ac.kill("Death by being in path of planet " + p.className);
                        }
                    }
                }

                // aliens that were on the planet, move with the planet
                // do this on a cloned list to avoid comodification
                LinkedList<AlienContainer> acsClone = (LinkedList<AlienContainer>) acsFrom.clone();
                for (AlienContainer ac : acsClone) {
                    if (ac.planet == p) {
                        // they didn't intend to move away, move with planet
                        ac.nextP = p.position;
                    }
                }


                // visualize
                IntegerPosition pOldInt = pOld.round();
                IntegerPosition pNewInt = pNew.round();

                vis.showPlanetMove(pOldInt.x, pOldInt.y, pNewInt.x, pNewInt.y, p.className, p.index, p.energy, (int) p.tech);
            }
        }
    }

    private void reviewInhabitants() {
        for (InternalSpaceObject so : this.objects) {
            if (so.isPlanet) {
                Planet p = (Planet) so;
                p.reviewInhabitants();
            }
        }

    }

    // now that moving is done and views don't matter anymore,
    // record the moves in AlienContainers and the grid
    public void recordAlienMoves() { //[Q]
        //vis.debugOut("Recording moves for " + (aliens.size()) + " aliens");

        // now for all the aliens
        for (AlienContainer ac : aliens) {
            if (ac.energy > 0) {// not accidentally killed by moving planet in this phase
                int oldX = ac.p.round().x; //[kludge]
                int oldY = ac.p.round().y;

                if (oldX != ac.nextP.x || oldY != ac.nextP.y) {
                    ac.p.set(ac.nextP);
                    aliens.move(ac, oldX, oldY, ac.p.round().x, ac.p.round().y);
                }

                // need to go through all the rest to mark cell fresh for display,
                // TODO: Fix this in visualizer instead, go away from fillRect and to painting individual cells.
                AlienCell acs = aliens.getAliensAt(ac.p);

                // call shell visualizer
                // just make sure we don't blow up the alien beacuse of an exception in the shell
                try {
                    vis.showMove(ac.getFullAlienSpec(),
                            oldX,
                            oldY,
                            0,
                            0);
                } catch (Exception e) {
                    displayException("Unhandled exception in showMove(): ", e);
                }

                if (acs.star != null) {
                    ac.kill("Death for moving into star " + acs.star.className);
                }
            }
        }
    }

    public boolean isInSafeZone(AlienContainer ac) { //[Q]
        return ac.p.magnitude() <= Constants.safeZoneRadius;
    }

    public void removeDeadAliens() {
        for (Iterator<AlienContainer> iterator = aliens.iterator(); iterator.hasNext();) {
            AlienContainer ac = iterator.next();
            if (ac.energy <= 0) {
                if (!ac.packageName.equalsIgnoreCase("stockelements")) {
                    vis.showDeath(ac.getFullAlienSpec(), 0);
                }
                aliens.unplug(ac);
                iterator.remove();
                this.alienCount--;

                InternalAlienSpecies as = speciesMap.get(ac.getFullSpeciesName());
                if (as != null) {
                    as.counter--;
                }

            }
        }
    }

    public void resetAliens() {
        for (AlienContainer alien : aliens) {
            alien.participatedInAction = false;
            alien.ctx.view = null;
            alien.outgoingMessage = null;
            alien.listening = false;

            AlienCell acell = alien.grid.aliens.getAliensAt(alien.p);
            acell.listening = false;
            acell.currentMessages = null;
            acell.energyPerAlien = 0;
        }
    }

    public void processResults() {
        for (AlienContainer ac : aliens) {
            //for (AlienContainer ac : aliens) {
            // get rid of stale views from prior moves
            ac.ctx.view = null;

            // call the alien to think on the state of the universe
            try {
                ac.processResults();
            } catch (Exception ex) {
                // any exceptions except NotSupported get the alien killed here
                displayException("Unhandled exception in processResults(): ", ex);

                ac.kill("Death for unhandled exception in processResults(): " + ex.toString());
            }
        }
    }

    public void requestAlienActions() {

        this.avgTech = 0;

        // first request all the actions from the aliens
        for (AlienContainer thisAlien : aliens) {
            // get rid of stale views from prior phases
            thisAlien.ctx.view = null;

            // assess avg tech
            avgTech += thisAlien.tech;

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
                thisAlien.kill("Death for unhandled exception in getAction(): " + ex.toString());
            }
        }
    }

    private void reviewInhabitantActions() {
        for (InternalSpaceObject so : this.objects) {
            if (so.isPlanet) {
                Planet p = (Planet) so;
                p.reviewInhabitantActions();
            }
        }
    }

    public void performAlienActions() { //[Q]
        LinkedList<AlienSpec> newAliens = new LinkedList<>();
        HashMap<AlienSpec, Trajectory> trajectories = new HashMap<>();
        // now process all actions
        for (AlienContainer thisAlien : aliens) {
            //thisAlien.debugOut(thisAlien.currentActionCode.toString());

            // if this guy was part of a fight, don't bother with more actions
            if (thisAlien.participatedInAction) continue;


            switch (thisAlien.currentActionCode) {
                case Land:
                    Planet p = aliens.getAliensAt(thisAlien.p).planet;
                    if (p != null) {
                        thisAlien.planet = p;
                    } else {
                        thisAlien.kill("Died by landing on a planet that wasn't there.");
                    }
                    break;

                case Launch:
                    if (thisAlien.planet != null) {
                        thisAlien.planet = null;
                    } else {
                        thisAlien.kill("Died launching from a planet it hadn't landed on.");
                    }
                    break;

                case Fight:
                    //vis.debugOut("SpaceGrid: Processing Fight");

                    if (Constants.iSaidNoFighting) {
                        break;
                    }

                    // If the alien is in the safe zone
                    if (isInSafeZone(thisAlien)) {
                        // Make them pay the energy they spent
                        thisAlien.energy -= thisAlien.currentActionPower + Constants.fightingCost;
                        // but don't let them fight
                        break;
                    }

                    List<AlienSpec> fightSpecs = new ArrayList<>();
                    HashMap<String, Double> fightSpecies = new HashMap<>();

                    LinkedList<AlienContainer> fightingAliens = aliens.getAliensAt(thisAlien.p);

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

                    vis.showFight(thisAlien.p.round().x, thisAlien.p.round().y);

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
                    List<AlienContainer> aliensAtPos;
                    aliensAtPos = (List<AlienContainer>) aliens.getAliensAt(thisAlien.p).clone();
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

                                // The two individual trading aliens exchange secrets
                                buyingAlien.secrets.putAll(sellingAlien.secrets);
                                sellingAlien.secrets.putAll(buyingAlien.secrets);

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
                    // todo: share power only among those gaining
                    if (thisAlien.energy < Constants.energyCap) {
                        AlienCell acs = aliens.getAliensAt(thisAlien.p);
                        // if this cell was not initialized in this turn, initialize it
                        // todo: how does this get reset? should it?
                        if (acs.energyPerAlien == 0) {
                            acs.energyPerAlien = acs.energy / acs.size(); // aliens have to share harvest in one spot
                        }

                        thisAlien.energy += acs.energyPerAlien * thisAlien.tech / Constants.energyGainReducer;

                        // cap it
                        if (thisAlien.energy > Constants.energyCap) {
                            thisAlien.energy = Constants.energyCap;
                        }
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
                        break;
                    }

                    if (thisAlien.currentActionPower <= 0) {
                        thisAlien.kill("Death stillborn spawning - no initial power for child.");
                        break;
                    }

                    // no spawning in safezone. makes sure squatters can't win the games.
                    if (isInSafeZone(thisAlien)) break;
                    

                    // construct a random move for the new alien depending on power and send that move through drift correction
                    // spend thisAction.power randomly on x move, y move and initital power
                    double power = thisAlien.currentActionPower;

                    int x = rand.nextInt(3) - 1;
                    int y = rand.nextInt(3) - 1;

                    thisAlien.energy -= power;
                    if (thisAlien.energy <= 0) {
                        thisAlien.kill("Death by spawning exhaustion - died in childbirth.");
                    }

                    Vector2 dir = new IntegerVector2(x, y).v2();
                    dir = thisAlien.containMove(thisAlien.p.x, thisAlien.p.y, dir);

                    x = thisAlien.p.round().x + dir.round().x; //[kludge]
                    y = thisAlien.p.round().y + dir.round().y;

                    // Add in the alien spec to a new arraylist
                    // to be processed later so the master list is not disrupted
                    AlienSpec spec = new AlienSpec(
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
                            thisAlien.currentActionMessage,
                            thisAlien
                    );
                    newAliens.add(spec);
                    trajectories.put(spec,thisAlien.trajectory);

                    break;
            }
            // this ends our big switch statement
        }

        // add all new aliens
        for (AlienSpec as : newAliens) {
            addAlienWithParams(as.x, as.y, as.domainName, as.packageName, as.className,
                    as.hashCode, as.tech, as.energy, as.msg, trajectories.get(as));
        }
    }

    void addSpecies(GameElementSpec element) {
        String speciesName = element.domainName + ":" + element.packageName + ":" + element.className;

        // add species if necessary
        InternalAlienSpecies as = speciesMap.get(speciesName);
        if (as == null) {

            as = new InternalAlienSpecies(element.domainName, element.packageName, element.className, speciesCounter, speciesMap.getAchievementCount());
            speciesMap.put(speciesName, as);
            speciesCounter++;

            if (as.cns == null) {
                try {
                    as.cns = loadConstructor(engine, element.domainName, element.packageName, element.className);
                } catch (Exception e) {
                    gridDebugErr("sg.addSpecies: Error loading contructor for " + speciesName);
                    //throw (e);
                }
                as.shapeFactory = null;
                try {
                    Alien a = (Alien) as.cns.newInstance();
                    if (a instanceof AlienShapeFactory) {
                        as.shapeFactory = (AlienShapeFactory) a;
                    }
                } catch (Exception e) {
                    gridDebugErr("sg.addSpecies: Error constructing class reference alien for " + speciesName);
                    //throw (e);
                }

            }

            if (!element.packageName.equalsIgnoreCase("stockelements")) {
                vis.registerSpecies(new AlienSpec(as), as.shapeFactory);
            }
        }
    }



    AlienContainer addAlienWithParams(int x, int y, String domainName, String alienPackageName, String alienClassName,
            int parent, double tech, double power, String spawnMsg, Trajectory trajectory) {
        AlienContainer ac = null;

        String speciesName = domainName + ":" + alienPackageName + ":" + alienClassName;

        // add species if necessary
        InternalAlienSpecies as = speciesMap.get(speciesName);
        if (as == null) {
            as = new InternalAlienSpecies(domainName, alienPackageName, alienClassName, speciesCounter, speciesMap.getAchievementCount());
            speciesMap.put(speciesName, as);
            speciesCounter++;
            if (!alienPackageName.equalsIgnoreCase("stockelements")) {
                vis.registerSpecies(new AlienSpec(as), as.shapeFactory);
            }
        }

        if (as.counter < Constants.perSpeciesCap && as.spawns < Constants.perSpeciesSpawnCap) {
            try {
                ac = new AlienContainer(this, this.vis, x, y,
                        domainName, alienPackageName, alienClassName, as.cns, as,
                        tech, power, // tech and power
                        parent, // no parent
                        spawnMsg,  // no spawn state
                        trajectory); //no trajectory

                aliens.addAlienAndPlug(ac);
                if (!ac.packageName.equalsIgnoreCase("stockelements")) {
                    vis.showSpawn(ac.getFullAlienSpec(), 0);
                }
                as.counter++;
                as.spawns++;
                this.alienCount++;
            } catch (InstantiationException e) {
                gridDebugErr("sg: could not instantiate new " + speciesName);
            }
        }

        return ac;
    }

    // Note: Having a seperate method for each InternalSpaceObject seems a little gross,
    // as there is already a large if statement in addElement and the code in
    // addPlanet and addStar is nearly identical
    void addPlanet(GameElementSpec element) { //[Q]
        InternalSpaceObject soParent = null;
        PlanetBehavior pb = null;
        for (InternalSpaceObject o : this.objects) {
            if (element.parent.equalsIgnoreCase(o.getFullName())) {
                soParent = o;
                break;
            }
        }
        if (soParent != null) {
            try {
                Constructor<?> cs = this.loadConstructor(engine, element.domainName, element.packageName, element.className);
                pb = (PlanetBehavior) cs.newInstance();
            } catch (Exception e) {
                vis.debugOut("sg.addPlanet: behavior not found: " + element.className);
            }

            Planet p = new Planet(this, soParent,
                    new Vector2(element.x, element.y).magnitude(),
                    planetCount++,
                    element.domainName, element.packageName, element.className,
                    element.energy, element.tech, element.parent, pb, element.mass);
            p.init();
            objects.add(p);
            this.aliens.plugPlanet(p);
            vis.registerPlanet(p.position.round().x, p.position.round().y, element.className, p.index, element.energy, (int) element.tech, element.mass);
        }
    }

    void addStar(GameElementSpec element) { //[Q]
        Star st = new Star(this, element.x, element.y, starCount++, element.domainName, element.packageName, element.className, element.energy, element.tech, element.mass);
        objects.add(st);
        this.aliens.plugStar(st);
        vis.registerStar(element.x, element.y, element.className, objects.indexOf(st), element.energy, element.mass);
        aliens.distributeStarEnergy(element.x, element.y, element.energy);
    }

    void addResident(GameElementSpec element) {

        Planet p = null;

        AlienContainer ac;
        for (InternalSpaceObject o : this.objects) {
            if ((element.parent.equalsIgnoreCase(o.getFullName())) && (o instanceof Planet)) {
                p = (Planet) o;
                break;
            }
        }

        if (p == null) {
            return;
        }

        addSpecies(element);

        ac = addAlienWithParams(p.position.round().x, p.position.round().y, element.domainName, element.packageName, element.className,
                0, 1, 1, element.state, null);

        if (ac == null) {
            return;
        }

        this.alienCount--; // don't count this one, it's a resident

        if (ac.alien instanceof ResidentAlien) {
            ResidentContextImplementation rc = new ResidentContextImplementation(ac);
            ((ResidentAlien) ac.alien).init(rc);
        }

        // TODO add code to seed Residents with secrets loaded from config file
        // ac gets created with empty secrets, spacegrid looks up resident by race from list
        // and adds secrets one by one
    }

    public void addElement(GameElementSpec element) throws IOException {

        if (null != element.kind) {
            switch (element.kind) {
                case ALIEN:
                    // position (0,0) leads to random assignment
                    InternalSpaceObject soParent = null;
                    element.parent = "ephemera.eastsideprep.org:stockelements:Sol"; // BIG FAT KLUDGE
                    for (InternalSpaceObject o : this.objects) {
                        if (element.parent != null && element.parent.equalsIgnoreCase(o.getFullName())) {
                            soParent = o;
                            break;
                        }
                    }

                    addAlienWithParams(0, 0, element.domainName, element.packageName, element.className, 0, 1, 1, null,
                            (soParent != null)? new DummyTrajectory(soParent):null);
                    break;
                case SPECIES:
                    addSpecies(element);
                    break;
                case PLANET:
                    addPlanet(element);
                    break;
                case STAR:
                    addStar(element);
                    break;
                case RESIDENT:
                    addResident(element);
                    break;
                default:
                    break;
            }
        }
    }

    //
    // Dynamic class loader (.jar files)
    // stolen from StackOverflow, considered dark voodoo magic
    //
    public Constructor<?> loadConstructor(GameEngineV1 engine, String domainName, String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> cs = null;
        String fullName;
        File file;
        URL url;
        Object[] parameters;

        if (packageName.equalsIgnoreCase("stockaliens")
                || packageName.equalsIgnoreCase("alieninterfaces")) {
            fullName = engine.gamePath
                    + packageName
                    + System.getProperty("file.separator")
                    + "dist"
                    + System.getProperty("file.separator")
                    + packageName + ".jar";
        } else {
            fullName = engine.alienPath + domainName;
        }

        try {
            file = new File(fullName);
            url = file.toURI().toURL();
            parameters = new Object[1];
            parameters[0] = url;

            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class
                    .getDeclaredMethod("addURL", URL.class
                    );
            method.setAccessible(true);
            Object result = method.invoke(classLoader, parameters);

            String fullClassName = packageName.equals("") ? className : (packageName + "." + className);

            cs = ClassLoader.getSystemClassLoader().loadClass(fullClassName).getConstructor();
        } catch (Exception e) {
            //e.printStackTrace();
            vis.debugErr("sg: Could not get constructor: " + className);
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

    public void killAll(GameElementSpec element) {
        String speciesName = element.domainName + ":" + element.packageName + ":" + element.className;

        InternalAlienSpecies as = speciesMap.get(speciesName);
        if (as != null) {
            for (AlienContainer ac : aliens) {
                if (as.speciesID == ac.speciesID) {
                    ac.kill("Death by shell selection");
                }
            }
        }

        removeDeadAliens();

    }

    public void addAllCustomAliens() {
        addCustomAliens(engine.alienPath, "");
        if (Constants.searchParentForAliens) {
            String parent = engine.gamePath;
            parent = parent.substring(0, parent.lastIndexOf(System.getProperty("file.separator"))); // take of trailing separator
            parent = parent.substring(0, parent.lastIndexOf(System.getProperty("file.separator")) + 1); // take off "SpaceCritters" or such

            addCustomAliens(parent, "");
        }
    }

    public void addCustomAliens(String folder, String domain) {
        String packageName;
        String className;

        if (folder.toLowerCase().endsWith("ephemera" + System.getProperty("file.separator"))
                || folder.toLowerCase().endsWith("spacecritters" + System.getProperty("file.separator"))) {
            return;
        }

        File folderFile = new File(folder);

        if (folderFile != null) {
            File[] files = folderFile.listFiles();

            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        // recurse
                        addCustomAliens(folder + f.getName() + System.getProperty("file.separator"), domain + f.getName() + System.getProperty("file.separator"));
                    } else if ((f.getName().toLowerCase().endsWith(".jar"))
                            && (!f.getName().toLowerCase().equals("stockaliens.jar"))
                            && (!f.getName().toLowerCase().equals("stockelements.jar"))
                            && (!f.getName().toLowerCase().equals("alieninterfaces.jar"))) {
                        try {
                            // look for jar files and process
                            List<String> classNames = new ArrayList<String>();
                            ZipInputStream zip = new ZipInputStream(new FileInputStream(f));
                            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                                if (!entry.isDirectory() && (entry.getName().toLowerCase().endsWith("alien.class")
                                        || entry.getName().toLowerCase().endsWith("spacecritter.class"))) {

                                    // handle classes in the default package differently
                                    if (entry.getName().lastIndexOf('/') != -1) {
                                        // named package
                                        packageName = entry.getName().substring(0, entry.getName().lastIndexOf('/'));
                                        className = entry.getName().substring(entry.getName().lastIndexOf('/') + 1).replace(".class", "");
                                    } else {
                                        //default package
                                        packageName = "";
                                        className = entry.getName().replace(".class", "");
                                    }

                                    addSpecies(new GameElementSpec("SPECIES",
                                            domain + f.getName(),
                                            packageName,
                                            className,
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
    }
}
