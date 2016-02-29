/*
 * 
 */
package stockaliens;

import alieninterfaces.*;

/**
 *
 * @author guberti
 */
public class Venusian implements Alien {
    int currentDirX;
    int currentDirY;
    Context ctx;
    
    public void init(Context ctx) {
        currentDirX = 0;
        currentDirY = 0;
        this.ctx = ctx;
    }
    
    // Move Function
    public MoveDir getMove() {
        
        return new MoveDir(currentDirX, currentDirY);
    }

    /*
    Obsolete:
    
    @Override
    public int getFightPower(AlienAPI api) {
        // Martians should fight with half the maximum amount of fight they
        // could fight with
        return Math.min(api.energy(), api.tech()) / 2;
    }*/
    
    public Action getAction() {
        return new Action(ActionCode.Gain);
    }
    
    public void processResults() {
        return;
    }

}
