/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class AlienSpec {
    public String domainName;
    public String packageName;
    public String className;
    public int hashCode;
    public int x;
    public int y;
    public int tech;
    public int energy;
    public int actionPower;
    
    
    public AlienSpec(String domainName, String packageName, String className, int hashCode, int x, int y, int tech, int energy) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
        this.tech = tech;
        this.energy = energy;
    }

    public AlienSpec(String domainName, String packageName, String className, int hashCode, int x, int y, int tech, int energy, int actionPower) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
        this.tech = tech;
        this.energy = energy;
        this.actionPower = actionPower;
    }

    // for purposes of describing a species of alien
    public AlienSpec(String domainName, String packageName, String className) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
    }
 
    
    // helpers
    public String getFullSpeciesName() {
        return domainName + ":" + packageName + ":" + className;
    }

    public String getFullName() {
        return getFullSpeciesName() + "(" + Integer.toHexString(hashCode).toUpperCase() + ")";
    }

    public String getXYString() {
        return "(" + x + "," + y + ")";
    }
    public String getTechEnergyString() {
        return "(" + tech + "," + energy + ")";
    }
}
