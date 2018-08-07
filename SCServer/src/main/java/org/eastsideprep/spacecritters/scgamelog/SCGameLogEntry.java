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
    public int type;
    public double newX;
    public double newY;
    public double param1;
    public double param2;
    public String name;
    public String speciesName;
    public int id;
    public int speciesId;
    public double energy;
    public double tech;

    public SCGameLogEntry(int type, double newX, double newY, double param1, double param2, String name, String speciesName, int id, int speciesId, double energy, double tech) {
        this.type = type;
        this.newX = newX;
        this.newY = newY;
        this.param1 = param1;
        this.param2 = param2;
        this.energy = energy;
        this.tech = tech;
        this.name = name;
        this.id = id;
        this.speciesId = speciesId;
        this.speciesName = speciesName;
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
        this.speciesId = sge.speciesId;
        this.speciesName = sge.speciesName;
    }

    public static class Type {

        public static final int ADDSPECIES = 1;
        public static final int ADDSTAR = 2;
        public static final int ADDPLANET = 3;
        public static final int MOVEPLANET = 4;
        public static final int TURN = 5;
        public static final int ADD = 6;
        public static final int MOVE = 7;
        public static final int KILL = 8;
        public static final int STATECHANGE = 9;
        public static final int ORBIT = 10;
        public static final int FIGHT = 11;

    }
}
