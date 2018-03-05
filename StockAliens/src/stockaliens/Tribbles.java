/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

import alieninterfaces.*;
import java.util.List;
 
/**
 *
 * @author thaeger
 */
public class Tribbles implements Alien {
    
    int myID = 0;
    Context ctx;
    int x = 0;
    int y = 0;

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.myID = id;
        this.ctx = ctx;
     }

    @Override
    public Direction getMove() {
        
        //move between x(-1:1) and y(-1:1)
        x = ctx.getRandomInt(3) - 1;
        y = ctx.getRandomInt(3) - 1;
        
        //get a view to work with to move towards energy
        View starView = null;
        try {
            starView = ctx.getView(3);
        } catch(Exception e) {
        }
        
        //try to find space objects
        try {
            //find nearsets space objects
            List<SpaceObject> closestSpaceHunk = ctx.getView((int) ctx.getTech()).getSpaceObjectsInView();
        } catch(Exception e) {
        }
        
        //try to avoid space objects
        try {
            //check if space object where we are moving
            if (ctx.getView(2).getSpaceObjectAtPos(ctx.getPosition().add(new Direction(x,y))) != null) {
                //if(x > 0){x -= 1} else{x -= -1}
                x -= x > 0? 1:-1;
            }
        } catch(NotEnoughEnergyException | NotEnoughTechException | View.CantSeeSquareException ex) {
        }

        //one return direction to ensure no ooops
        return new Direction(x,y);
    }

    @Override
    public Action getAction() {
        
        //get a view to be used multiple times
        View view = null;
        try {
            view = ctx.getView(1);
        } catch (Exception e) {
        }

        //try to preform actions if have enough energy
        try {
            
            if(view.getAliensAtPos(ctx.getPosition()).size() > 1) {
                //if so and we have enough energy...
                if(ctx.getFightingCost() + 15 < ctx.getEnergy()) {
                    //fight...
                    return new Action(Action.ActionCode.Fight,(int) ctx.getEnergy() - 10);
                } else {
                    //otherwise get energy to fight
                    return new Action(Action.ActionCode.Gain);
                }
            }
            
            //tech is less important to spawning and fighting
            if(ctx.getEnergy() > (ctx.getTech() + 5)) {
                //if we have enough energy to spawn...
                if(ctx.getEnergy() > ctx.getSpawningCost() + 5) {
                    //than do it
                    return new Action(Action.ActionCode.Spawn,1);
                }
                //other wise check if aliens are at the same position...
                
                //lastly get tech for views
                if(ctx.getTech() < 15) {
                    return new Action(Action.ActionCode.Research);
                }
            }
        
        } catch(View.CantSeeSquareException ex) {
            ctx.debugOut("Oops");
            return new Action(Action.ActionCode.Gain);
        }
        
        ctx.debugOut("Gain with E: " + ctx.getEnergy());
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {

    }

    @Override
    public void receive(String[] messages) {

    }
    
    @Override
    public void processResults(){
    }
}