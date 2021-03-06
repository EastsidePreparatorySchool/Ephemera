/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.alieninterfaces.*;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameVisualizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.eastsideprep.spacecritters.orbit.Orbitable;

/**
 *
 * @author guberti
 */
public class ContextImplementation implements ContextComplex {

    private final AlienContainer ac;
    public GameVisualizer vis;
    public ViewImplementation view;

    ContextImplementation(AlienContainer ac, GameVisualizer vis) {
        this.ac = ac;
        this.vis = vis;
    }

    @Override
    public double getEnergy() {
        return ac.energy;
    }

    @Override
    public int getTech() {
        return (int) ac.tech;
    }

    @Override
    public Position getPosition() {
        return new Position(ac.p);
    }

    @Override
    public IntegerPosition getIntegerPosition() {
        return new IntegerPosition(ac.p);
    }

    @Override
    public double getMass() {
        return ac.getMass();
    }

    @Override
    public double getTime() {
        return ac.grid.getTime();
    }

    @Override
    public View getView(int size) throws NotEnoughEnergyException, NotEnoughTechException {
        // not more than tech
        if (size > 2 && size > (int) ac.tech) {
            throw new NotEnoughTechException();
        }

        //if (!Constants.gameMode.equalsIgnoreCase("sc_play.json")) {
        //    // views are only free in play mode
        //    ac.energy -= size;
        //}
        // Make size at least 2 so people can see where they can move for free
        size = Math.max(size, 2);

        // if we don't have one or they want a bigger one
        this.view = new ViewImplementation(ac.grid.aliens, ac, ac.p.round().x, ac.p.round().y, size); //[kludge]
        return this.view;
    }

    @Override
    public double getPresentEnergy() {
        return ac.grid.aliens.getEnergyAt(ac.p.round().x, ac.p.round().y); //[kludge]
    }

    @Override
    public int getSpawningCost() {
        return Constants.spawningCost;
    }

    @Override
    public int getFightingCost() {
        return Constants.fightingCost;
    }

    @Override
    public HashMap getSecrets() {
        return new HashMap<>(ac.secrets);
    }

    @Override
    public int getSecret(String key) {
        return ac.secrets.get(key);
    }

    // alien chatter is prefixed with full info, and only talks when chatter is on
    @Override
    public void debugOut(String s) {
        if (Constants.chatter) {
            ac.getFullName();
            vis.debugOut(ac.className + "("+ac.hashCode()+"): " + s);
        }
    }

    @Override
    public int getRandomInt(int ceiling) {
        return this.ac.grid.rand.nextInt(ceiling);
    }

    // messsaging API: record that this alien wants to send, and whether to receive
    @Override
    public void broadcastAndListen(String message, int power, boolean listen)
            throws NotEnoughTechException, NotEnoughEnergyException {

        if (power > ac.tech) {
            throw new NotEnoughTechException();
        }

        if (power > ac.energy + 1) {
            throw new NotEnoughEnergyException();
        }

        ac.energy -= power;

        ac.outgoingMessage = message;
        ac.outgoingPower = power;

        if (listen) {
            AlienCell acell = this.ac.grid.aliens.getAliensAt(ac.p.round().x, ac.p.round().y); //[kludge]
            ac.listening = true;
            acell.listening = true;
        }
    }

    // communicate phase 2: deliver messages to gridpoints
    public void routeMessages() {
        // poke around our current position,
        // tracing an imaginary square of increasing size,
        // in 8 line segments, hopefully without overlap
        for (IntegerPosition p : new GridDisk(ac.p, (int) ac.outgoingPower)) {
            depositMessageAt(p, ac.outgoingMessage);
        }
    }

    // put a message at one grid point ONLY if someone is listening
    public void depositMessageAt(IntegerPosition p, String message) {
        int x = p.x;
        int y = p.y;

        if (((x + ac.grid.aliens.centerX) >= ac.grid.width)
                || ((x + ac.grid.aliens.centerX) < 0)
                || ((y + ac.grid.aliens.centerY) >= ac.grid.height)
                || ((y + ac.grid.aliens.centerY) < 0)) {
            return;
        }
        AlienCell acell = this.ac.grid.aliens.getAliensAt(x, y);

        if (acell != null) {
            if (!acell.isEmpty()) {
                // there are aliens there
                if (acell.listening) {
                    // leave a message
                    if (acell.currentMessages == null) {
                        acell.currentMessages = new ArrayList<String>();
                    }
                    acell.currentMessages.add(message);
                }
            }
        }
    }

    @Override
    public int getGameTurn() {
        return ac.grid.currentTurn;
    }

    @Override
    public IntegerPosition getMinPosition() {
        return new IntegerPosition(-ac.grid.width / 2, -ac.grid.height / 2);
    }

    @Override
    public IntegerPosition getMaxPosition() {
        return new IntegerPosition(ac.grid.width / 2 - 1, ac.grid.height / 2 - 1);
    }

    @Override
    public String getStateString() {
        return getPosition().toString()
                + " E:" + Double.toString(Math.round(ac.energy * 100) / 100)
                + " T:" + Integer.toString((int) ac.tech);

    }

    @Override
    public double getDistance(Vector2 p1, Vector2 p2) {
        return p1.subtract(p2).magnitude();
    }

    @Override
    public int getDistance(IntegerPosition p1, IntegerPosition p2) {
        return (int) p1.subtract(p2).magnitude();
    }

    @Override
    public List<IntegerPosition> computeOrbit(IntegerPosition center, int radius) { //Note: DISABLED. No one uses it and it needs to be rewtirren with conics anyway
        //GridCircle gc = new GridCircle(center.x, center.y, radius);
        ArrayList<IntegerPosition> orbit = new ArrayList<>();

        /*for (IntegerPosition p : gc) {
            orbit.add(p);
        }*/
        return orbit;
    }

    @Override
    public AlienSpecies getMyAlienSpecies() {
        IntegerPosition p = ac.p.round();
        return new AlienSpecies(ac.domainName, ac.packageName, ac.className, ac.speciesID, p.x, p.y); //[kludge]
    }

    @Override
    public WorldVector getVelocity() {
        if (ac.trajectory != null) {
            return ac.trajectory.getVelocityAtTime(ac.grid.getTime());
        }
        return (null);
    }

    @Override
    public Orbit getOrbit() {
        Orbit o = null;
        if (ac.trajectory != null) {
            o = new Orbit();
            o.focus = getFocus();
            o.e = ac.trajectory.conic.e;
            o.p = ac.trajectory.conic.p;
            o.a = ac.trajectory.conic.p / (1 - o.e * o.e);
            o.rotation = ac.trajectory.conic.rotation;
            o.signum = ac.trajectory.conic.signum;
            o.mu = ac.trajectory.conic.mu;
            o.h = ac.trajectory.conic.h;
        }

        return o;
    }

    @Override
    public SpaceObject getFocus() {
        if (ac.trajectory != null) {
            Orbitable focus = ac.trajectory.currentFocus;
            if (focus != null) {
                if (focus instanceof Star) {
                    Star s = (Star) focus;
                    return new SpaceObject("STAR", s.className, s.position, s.mass, s.hillRadius);
                } else if (focus instanceof Planet) {
                    Planet p = (Planet) focus;
                    return new SpaceObject("PLANET", p.className, p.position, p.mass, p.hillRadius);
                }
            }
        }
        return (null);
    }

    @Override
    public SpaceObject getSpaceObject(String name) {
        for (InternalSpaceObject iso : this.ac.grid.objects) {
            if (iso.className.equalsIgnoreCase(name)) {
                if (iso instanceof Star) {
                    Star s = (Star) iso;
                    return new SpaceObject("STAR", s.className, new Position(s.position), s.mass, s.hillRadius);
                } else if (iso instanceof Planet) {
                    Planet p = (Planet) iso;
                    return new SpaceObject("PLANET", p.className, new Position(p.position), p.mass, p.hillRadius);
                }
            }
        }
        return null;
    }

    @Override
    public List<SpaceObject> getAllSpaceObjects() {
        List<SpaceObject> result = new LinkedList<>();
        for (InternalSpaceObject iso : this.ac.grid.objects) {
            if (iso instanceof Star) {
                Star s = (Star) iso;
                result.add(new SpaceObject("STAR", s.className, new Position(s.position), s.mass, s.hillRadius));
            } else if (iso instanceof Planet) {
                Planet p = (Planet) iso;
                result.add(new SpaceObject("PLANET", p.className, new Position(p.position), p.mass, p.hillRadius));
            }
        }
        return result;
    }

    @Override
    public WorldVector getWorldPosition() {
        if (ac.trajectory.conic.tCurrent != ac.grid.getTime()) {
            ac.trajectory.conic.updateStateVectors(ac.grid.getTime());
        }
        return new WorldVector(ac.trajectory.conic.rCurrent);
    }

}
