/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.alieninterfaces.AlienSpecies;
import java.lang.reflect.Constructor;

/**
 *
 * @author gunnar
 */
public class InternalAlienSpecies extends AlienSpecies {

    public Constructor<?> cns;
    public long counter;
    public long spawns;
    public boolean[] achievements;
    public boolean instantiate;

    public InternalAlienSpecies(String domainName, String packageName, String className, int id, int achievementCount, boolean instantiate) {
        super(domainName, packageName, className, id);
        counter = 0;
        spawns = 0;
        achievements = new boolean[achievementCount];
        this.instantiate = instantiate;
        resetAchievements();
    }

    public void resetAchievements() {
        for (int i = 0; i < achievements.length; i++) {
            achievements[i] = false;
        }
    }
}
