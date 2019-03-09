/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import gameengineinterfaces.*;


/**
 *
 * @author guberti
 */


public class AchievementReq {
    public Requirement numReq;
    public double amount;
    public AchievementFlag flagReq;

    public AchievementReq(String flagReq) {
        this.flagReq = AchievementFlag.valueOf(flagReq.trim().toUpperCase());
    }
    
    public AchievementReq(String req, double amount) {
        this.numReq = Requirement.valueOf(req.trim().toUpperCase());
        this.amount = amount;
    }
}
