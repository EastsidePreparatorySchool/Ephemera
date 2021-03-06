/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.gameengineinterfaces.*;
import org.eastsideprep.spacecritters.alieninterfaces.Action.ActionCode;

/**
 *
 * @author guberti
 */
public class Achievement {

    AchievementReq[] reqs;

    public Achievement(int reqNum) {
        reqs = new AchievementReq[reqNum];
    }

    public void addReq(AchievementReq req, int position) {
        reqs[position] = req;
    }

    public boolean checkFlag(AchievementFlag flag) {
        return checkFullAlien(null, flag, -1, -1);
    }

    public boolean checkAlien(AlienContainer ac) {
        return checkFullAlien(ac, null, -1, -1);
    }

    public boolean checkBroadcastPower(double bP) {
        return checkFullAlien(null, null, bP, -1);
    }

    public boolean checkTotalAliens(double tA) {
        return checkFullAlien(null, null, -1, tA);
    }

    public boolean checkFullAlien(AlienContainer ac, AchievementFlag flag, double broadcastPower, double totalAliens) {
        for (AchievementReq req : reqs) {
            if (req.flagReq != null) { // If that specific requirement needs a flag
                if (flag != req.flagReq) { // If it is the incorrect flag
                    return false; // The alien does not get the achievement
                }
            } else { // If the achievement requires a numeric stat

                double alienAmount = -1; // The amount the alien has of the stat

                switch (req.numReq) {
                    case Energy:
                        alienAmount = ac.energy;
                        break;

                    case Tech:
                        alienAmount = ac.tech;
                        break;

                    case FightEnergy:
                        if (ac.currentActionCode == ActionCode.Fight) {
                            alienAmount = ac.currentActionPower;
                        } else {
                            alienAmount = 0;
                        }
                        break;

                    case Broadcast:
                        if (broadcastPower > 0) {
                            alienAmount = broadcastPower;
                        }
                        break;

                    case Aliens:
                        if (totalAliens > 0) {
                            alienAmount = totalAliens;
                        }

                }

                if (alienAmount < req.amount) { // If the alien does not have enough of the stat
                    return false;
                }
            }
        }
        // If the alien passes all required conditions
        return true; // It gets the achievement
    }
}
