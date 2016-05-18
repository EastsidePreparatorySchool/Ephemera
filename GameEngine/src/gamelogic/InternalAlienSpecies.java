/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.AlienShapeFactory;
import alieninterfaces.AlienSpecies;
import java.lang.reflect.Constructor;

/**
 *
 * @author gunnar
 */
public class InternalAlienSpecies extends AlienSpecies {
    public Constructor<?> cns;
    public AlienShapeFactory shapeFactory;
    public long counter;
    public long spawns;

    public InternalAlienSpecies(String domainName, String packageName, String className, int id) {
        super(domainName, packageName, className, id);
        counter = 0;
        spawns = 0;
    }
}
