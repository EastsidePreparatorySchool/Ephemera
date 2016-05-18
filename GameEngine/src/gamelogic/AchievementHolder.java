/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */

package gamelogic;
import java.util.HashMap;
import gameengineinterfaces.*;

/**
 *
 * @author guberti
 */
public class AchievementHolder {
    Achievement[] achievements;
    HashMap<String, boolean[]> map;
    
    AchievementHolder() {
        
    }
    
    void initiateSpecies(String fullname) { // Fullname = packagename + classname
        map.put(fullname, newEmptyAchievementArray());
    }
    private boolean[] newEmptyAchievementArray() {
        return new boolean[achievements.length];
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
        boolean[] newAchievements = map.get(fullname);
        newAchievements[index] = true;
        map.put(fullname, newAchievements);
    }
}
