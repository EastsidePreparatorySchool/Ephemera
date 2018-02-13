/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

import alieninterfaces.*;
 
/**
 *
 * @author thaeger
 */
public class Tribbles implements Alien {
    
    int myID = 0;
    Context ctx;
    static int alienCount = 1;
    int distance = 500;
    int x = 0;
    int y = 0;
    int r = 50;

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.myID = id;
        this.ctx = ctx;
     }

    @Override
    public void communicate() {

    }

    @Override
    public void receive(String[] messages) {

    }

    @Override
    public Direction getMove() {
        //find distance between here and corner
        //find nearest star/planet for avoidance
        ctx.debugOut(Integer.toString(alienCount));
        if(alienCount <= 10000) {
        distance = ctx.getDistance(ctx.getPosition(),ctx.getMinPosition());
        if(distance > r) {
            distance = ctx.getDistance(ctx.getPosition(),ctx.getMinPosition());
            ctx.debugOut(Integer.toString(distance));
            return new Direction(-1,-1);
        }
        if(distance <= r) {
            distance = ctx.getDistance(ctx.getPosition(),ctx.getMinPosition());
            x = ctx.getRandomInt(3) - 1;
            y = ctx.getRandomInt(3) - 1;
            return new Direction(x,y);
        }
        return new Direction(0,0);
        } else {
            x = ctx.getRandomInt(2);
            y = ctx.getRandomInt(2);
            return new Direction(x,y);
        }
    }

    @Override
    public Action getAction() {
        if((distance < r + 5 || alienCount >= 5000) && ctx.getEnergy() > ctx.getSpawningCost() + 11){
            ctx.debugOut(Double.toString(ctx.getEnergy()));
            alienCount += 1;
            return new Action(Action.ActionCode.Spawn,10);
        } else  {
            if(ctx.getEnergy() < ctx.getTech() + 3) {
                return new Action(Action.ActionCode.Gain);
            } else {
                return new Action(Action.ActionCode.Research);
            }
        }
    }

    @Override
    public void processResults(){
    }
}
