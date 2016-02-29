/*
 * 
 */
package stockaliens;

import alieninterfaces.*;

/**
 *
 * @author sfreisem-kirov
 */
public class Martian implements Alien {
    int HorizontalMove; 
    int VerticalMove; 
    Context ctx;
    
    public void init(Context ctx) {
        HorizontalMove = 0; 
        VerticalMove = 0; 
        this.ctx = ctx;
    }
    
    // Martians move left, right, left, right
    public MoveDir getMove() {
        return new MoveDir(ctx.getEnergy(), 0);
    }

    
    public Action getAction() {
        try
        {
            if(ctx.getView().isAlienAtPos(ctx.getX(), ctx.getY()))
            {
                return new Action(ActionCode.Fight, Math.max(ctx.getEnergy() - 2, 2));
            }
        }catch (Exception e)
        {
            
        }
        
        if(ctx.getEnergy()< 2)
        {
            return new Action(ActionCode.Gain);
        }
        else if(ctx.getEnergy() < 4)
        {
            return new Action(ActionCode.Research);
        }
        else
        {
            return new Action(ActionCode.Spawn);
        }
    }
    
    public void processResults() {
        ctx.debugOut("Martian: processRsult not supported yet.");
        return;
        // GM: Sam, you don't really want to do this throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }
}

/* SFK code as checked into branch


package consolealiensgame;


public class Martian extends Alien {
    int HorizontalMove;
    int VerticalMove;
    
    public Martian() {
        HorizontalMove = 0;
        VerticalMove = 0;
    }

    // Martians move left, right, left, right
    public MoveDir getMove(AlienAPI api) {
        return new MoveDir(api.energy(), 0);
    }

    public Action getAction(AlienAPI api) {
        try
        {
            if(api.view.isAlienAtPos(api.x(), api.y()))
            {
                return new Action(ActionCode.Fight, Math.max(api.energy() - 2, 2));
            }
        }catch (Exception e)
        {
            
        }
        
        if(api.energy()< 2)
        {
            return new Action(ActionCode.Gain);
        }
        else if(api.energy() < 4)
        {
            return new Action(ActionCode.Research);
        }
        else
        {
            return new Action(ActionCode.Spawn);
        }
        
    }

    @Override
    public void processResults(AlienAPI api) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

*/
