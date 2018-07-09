/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.scgamelog;

import org.eastsideprep.spacecritters.gamelog.GameLogEntry;

/**
 *
 * @author gmein
 */
public class SCGameLogEntry extends GameLogEntry {

    // to record: moves, acceleration, state change (t,e, orbital, secrets), comm, trade, 
    public String type;
    public int newX;
    public int newY;
    public int param1;
    public int param2;
    public String name;
    public int id;
    public double energy;
    public double tech;

    public SCGameLogEntry(String type, int newX, int newY, int param1, int param2, String name, int id, double energy, double tech) {
        this.type = type;
        this.newX = newX;
        this.newY = newY;
        this.param1 = param1;
        this.param2 = param2;
        this.energy = energy;
        this.tech = tech;
        this.name = name;
        this.id = id;
    }

    public SCGameLogEntry(SCGameLogEntry sge) {
        this.type = sge.type;
        this.newX = sge.newX;
        this.newY = sge.newY;
        this.param1 = sge.param1;
        this.param2 = sge.param2;
        this.energy = sge.energy;
        this.tech = sge.tech;
        this.name = sge.name;
        this.id = sge.id;
    }
}
