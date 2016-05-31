/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author guberti
 */
public class ContextImplementation implements Context {

    private AlienContainer ac;
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
        return new Position(ac.x, ac.y);
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
        this.view = new ViewImplementation(ac.grid.aliens, ac, ac.x, ac.y, size);
        return this.view;
    }

    @Override
    public double getPresentEnergy() {
        return ac.grid.aliens.getEnergyAt(ac.x, ac.y);
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
            vis.debugOut(ac.getFullName() + ": " + s);
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
            AlienCell acell = this.ac.grid.aliens.getAliensAt(ac.x, ac.y);
            ac.listening = true;
            acell.listening = true;
        }
    }

    // communicate phase 2: deliver messages to gridpoints
    public void routeMessages() {
        // poke around our current position,
        // tracing an imaginary square of increasing size,
        // in 8 line segments, hopefully without overlap
        for (int d = 1; d <= ac.outgoingPower; d++) {
            GridCircle c = new GridCircle(ac.x, ac.y, d);
            for (Position point : c) {
                if (point != null) {
                    depositMessageAt(point, ac.outgoingMessage);
                }
            }
        }
    }

    // put a message at one grid point ONLY if someone is listening
    public void depositMessageAt(Position p, String message) {
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
                        acell.currentMessages = new ArrayList();
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
    public Position getMinPosition() {
        return new Position(-ac.grid.width / 2, -ac.grid.height / 2);
    }

    @Override
    public Position getMaxPosition() {
        return new Position(ac.grid.width / 2 - 1, ac.grid.height / 2 - 1);
    }

    @Override
    public String getStateString() {
        return getPosition().toString()
                + " E:" + Double.toString(Math.round(ac.energy * 100) / 100)
                + " T:" + Integer.toString((int) ac.tech);

    }

    @Override
    public int getDistance(Position p1, Position p2) {
        return GridCircle.distance(p1, p2);
    }

    @Override
    public List<Position> computeOrbit(Position center, int radius) {
        GridCircle gc = new GridCircle(center.x, center.y, radius);
        ArrayList<Position> orbit = new ArrayList<>();
        
        for (Position p:gc) {
            orbit.add(p);
        }
        
        return orbit;
    }

    @Override
    public AlienSpecies getMyAlienSpecies() {
        return new AlienSpecies(ac.domainName, ac.packageName, ac.className, ac.speciesID, ac.x, ac.y);
    }

}
