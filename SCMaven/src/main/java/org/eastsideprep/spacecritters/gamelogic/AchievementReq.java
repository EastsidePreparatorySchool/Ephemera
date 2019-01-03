/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.gameengineinterfaces.*;

/**
 *
 * @author guberti
 */
public class AchievementReq {

    Requirement numReq;
    double amount;
    AchievementFlag flagReq;

    public AchievementReq(AchievementFlag flagReq) {
        this.flagReq = flagReq;
    }

    public AchievementReq(Requirement req, double amount) {
        this.numReq = req;
        this.amount = amount;
    }
}
