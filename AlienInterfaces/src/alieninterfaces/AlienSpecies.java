/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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
    public Position position; // for view purposes

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
        this.position  = new Position (x, y);
    }
    public String getFullSpeciesName() {
        if (speciesName == null) {
            speciesName = domainName + ":" + packageName + ":" + className;
        }
        return speciesName;
    }

}
