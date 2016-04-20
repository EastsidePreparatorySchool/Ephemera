/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.AlienSpecies;
import java.lang.reflect.Constructor;

/**
 *
 * @author gunnar
 */
public class InternalAlienSpecies extends AlienSpecies {
    Constructor<?> cns;

    public InternalAlienSpecies(String domainName, String packageName, String className, int id) {
        super(domainName, packageName, className, id);
    }
}
