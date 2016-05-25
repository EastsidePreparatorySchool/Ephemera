/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import gameengineinterfaces.AchievementFlag;
import java.util.HashMap;

/**
 *
 * @author guberti
 */

/**
 *
 * @author guberti
 * @param <K>
 * @param <V>
 */
public class SpeciesMap<K extends String, V extends InternalAlienSpecies> extends HashMap<K,V> {
    Achievement[] achievements;
    public SpeciesMap(Achievement[] achievements) {
        this.achievements = achievements;
    }
    
    void recieveFlag(String fullname, AchievementFlag flag) {
        for (int i = 0; i < achievements.length; i++) {
            // TODO add code to see if achievement has already been achieved
            if (achievements[i].checkFlag(flag)) {
                setAchievementForAlien(fullname, i);
            }
        }
    }
    
    void checkAchievementsForAlien(AlienContainer ac) {
        for (int i = 0; i < achievements.length; i++) {
            // TODO add code to see if achievement has already been achieved
            if (achievements[i].checkFullAlien(ac, null, -1, -1)) {
                
                setAchievementForAlien(ac.getFullName(), i);
            }
        }
    }
    
    private void setAchievementForAlien(String fullname, int index) {
        boolean[] newAchievements = get(fullname).achievements;
        newAchievements[index] = true;
    }
    
    public int getAchievementCount() {
        return achievements.length;
    }
}
