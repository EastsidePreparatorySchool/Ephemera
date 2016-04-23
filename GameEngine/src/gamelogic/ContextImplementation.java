/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;
import java.util.ArrayList;

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

    public double getEnergy() {
        return ac.energy;
    }

    public double getTech() {
        return ac.tech;
    }

    public int getX() {
        return ac.x;
    }

    public int getY() {
        return ac.y;
    }

    public View getView() {
        if (this.view == null) {
            this.view = new ViewImplementation(ac);
            // full object: this.view = this.aC.getFullView();
        }
        return this.view;
    }

    public int getSpawningCost() {
        return Constants.spawningCost;
    }

    public int getFightingCost() {
        return Constants.fightingCost;
    }

    // alien chatter is prefixed with full info, and only talks when chatter is on
    public void debugOut(String s) {
        if (Constants.chatter) {
            vis.debugOut(ac.getFullName() + ": " + s);
        }
    }

    @Override
    public Radar getRadar() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public int getRandomInt(int ceiling) {
        return SpaceGrid.rand.nextInt(ceiling);
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
            for (int[] point : c) {
                if (point != null) {
                    depositMessageAt(point[0], point[1], ac.outgoingMessage);
                }
            }
        }
    }

    // put a message at one grid point ONLY if someone is listening
    public void depositMessageAt(int x, int y, String message) {
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
    public int getMinX() {
        return -ac.grid.width / 2;
    }

    @Override
    public int getMinY() {
        return -ac.grid.height / 2;
    }

    @Override
    public int getMaxX() {
        return ac.grid.width / 2 - 1;
    }

    @Override
    public int getMaxY() {
        return ac.grid.height / 2 - 1;
    }
}
