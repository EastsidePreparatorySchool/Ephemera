/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import alieninterfaces.AlienSpecies;

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
    public int tech;
    public int energy;
    public int actionPower;
    public String fullName;
    public String speciesName;

    public AlienSpec(String domainName, String packageName, String className,
            int speciesID, int hashCode, int x, int y, int tech, int energy,
            String fullName, String speciesName) {
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
    }

    public AlienSpec(String domainName, String packageName, String className,
            int speciesID, int hashCode, int x, int y, int tech, int energy,
            String fullName, String speciesName, int actionPower) {
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

    }
    
    public AlienSpec(AlienSpecies as) {
        this.domainName = as.domainName;
        this.packageName = as.packageName;
        this.className = as.className;
        this.speciesID = as.speciesID;
        
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

    public String getXYString() {
        return "(" + x + "," + y + ")";
    }

    public String getTechEnergyString() {
        return "(" + tech + "," + energy + ")";
    }
}
