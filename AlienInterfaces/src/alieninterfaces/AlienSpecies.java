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
public class AlienSpecies {

    public String domainName;
    public String packageName;
    public String className;
    private String speciesName;
    public int speciesID;
    public int x; // for view purposes
    public int y; // for view purposes

    public AlienSpecies(String domainName, String packageName, String className, int id) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.speciesID = id;
    }

    public AlienSpecies(String domainName, String packageName, String className, int id, int x, int y) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.speciesID = id;
        this.x = x;
        this.y = y;
    }
    public String getFullSpeciesName() {
        if (speciesName == null) {
            speciesName = domainName + ":" + packageName + ":" + className;
        }
        return speciesName;
    }

}
