/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineinterfaces;

import org.eastsideprep.spacecritters.alieninterfaces.AlienSpecies;
import org.eastsideprep.spacecritters.gamelogic.AlienContainer;
import org.eastsideprep.spacecritters.orbit.Conic;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author gmein
 */
public class AlienSpec {

    public String domainName;
    public String packageName;
    public String className;
    public int speciesID;
    public int hashCode;
    public int x;
    public int y;
    public double tech;
    public double energy;
    public double actionPower;
    public String fullName;
    public String speciesName;
    public String msg;
    public boolean isResident;

    public AlienSpec(String domainName, String packageName, String className,
            int speciesID, int hashCode, int x, int y, double tech, double energy,
            String fullName, String speciesName, String msg) { //[Q]
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
        this.tech = tech;
        this.energy = energy;
        this.fullName = fullName;
        this.speciesName = speciesName;
        this.speciesID = speciesID;
        this.msg = msg;
        this.isResident = this.className.toLowerCase().endsWith("resident");
    }

    public AlienSpec(String domainName, String packageName, String className,
            int speciesID, int hashCode, int x, int y, double tech, double energy,
            String fullName, String speciesName, String msg, AlienContainer parent) { //[Q]
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
        this.tech = tech;
        this.energy = energy;
        this.fullName = fullName;
        this.speciesName = speciesName;
        this.speciesID = speciesID;
        this.msg = msg;
        this.isResident = this.className.toLowerCase().endsWith("resident");
        //this.parent = parent;
    }

    public AlienSpec(String domainName, String packageName, String className,
            int speciesID, int hashCode, int x, int y, double tech, double energy,
            String fullName, String speciesName, double actionPower) { //[Q]
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
        this.tech = tech;
        this.energy = energy;
        this.fullName = fullName;
        this.speciesName = speciesName;
        this.speciesID = speciesID;
        this.actionPower = actionPower;
        this.isResident = this.className.toLowerCase().endsWith("resident");
    }

    public AlienSpec(AlienSpecies as) {
        this.domainName = as.domainName;
        this.packageName = as.packageName;
        this.className = as.className;
        this.speciesID = as.speciesID;
        this.isResident = as.isResident;
    }

    // for purposes of describing a species of alien
    public AlienSpec(String domainName, String packageName, String className, int speciesID,
            String fullName, String speciesName) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.fullName = fullName;
        this.speciesName = speciesName;
        this.speciesID = speciesID;
    }

    // helpers
    public String getFullSpeciesName() {
        if (speciesName == null) {
            speciesName = domainName + ":" + packageName + ":" + className;
        }
        return speciesName;
    }

    public String getFullName() {
        if (fullName == null) {
            fullName = getFullSpeciesName() + "(" + Integer.toHexString(hashCode).toUpperCase() + ")";
        }
        return fullName;
    }

    public String getXYString() { //[Q]
        return "(" + x + "," + y + ")";
    }

    public String getTechEnergyString() {
        return "(" + Math.round(tech * 10) / 10 + "," + Math.round(energy * 10) / 10 + ")";
    }
}
