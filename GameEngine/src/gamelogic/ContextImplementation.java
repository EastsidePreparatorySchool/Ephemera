/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import gameengineinterfaces.GameVisualizer;

/**
 *
 * @author guberti
 */
public class ContextImplementation implements Context {

    private AlienContainer ac;
    public GameVisualizer vis;
    public boolean chatter;
    public ViewImplementation view;

    ContextImplementation(AlienContainer ac, GameVisualizer vis) {
        this.ac = ac;
        this.vis = vis;
        chatter = false;
    }

    public int getEnergy() {
        return ac.energy;
    }

    public int getTech() {
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
        return 100;
    }

    public int getFightingCost() {
        return 0;
    }

    // alien chatter is prefixed with full info, and only talks when chatter is on
    public void debugOut(String s) {
        if (ac.chatter) {
            vis.debugOut(ac.getFullName() + ": " + s);
        }
    }
}
