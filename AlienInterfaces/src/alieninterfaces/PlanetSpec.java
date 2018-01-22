/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gunnar
 */
public class PlanetSpec {

    int x;
    int y;
    String name;
    int index;
    double energy;
    int tech;

    public PlanetSpec(int x, int y, String name, int index, double energy, int tech) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.index = index;
        this.energy = energy;
        this.tech = tech;
    }

}
