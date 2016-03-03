/*
 * 
 */
package stockaliens;
import java.util.Random;
import alieninterfaces.*;
/**
 *
 * @author guberti
 */
public class Martian implements Alien {
    //declaring everything that will need to be used later
    Context ctx;
    int HorizontalMove;
    int VerticalMove;
    int Remainder;
    int fightStrength;
    //gets a random boolean to determine positive or negative move function.
    private static Random rnd = new Random();
    public static boolean getRandomBoolean() {
        return rnd.nextBoolean();
    }
//empty constructor
    public Martian() 
    {
        System.out.println("I like waffles");
    }
    
    @Override
    public void init(Context ctx) {
        System.out.println("Pancakes taste like styrofoam");
        this.ctx = ctx;                
    }
    
    public MoveDir getMove() {
         //splits in two whole numbers
        Remainder = ctx.getTech() % 2;
        HorizontalMove = (ctx.getTech()-Remainder)/2;
        VerticalMove = ctx.getTech()- HorizontalMove;
        if(getRandomBoolean() == true)
        {
            HorizontalMove *= -1;
        }
        if(getRandomBoolean() == true)
        {
            VerticalMove *= -1;
        }
        int ClosestAlienXCoordinate;
        int ClosestAlienYCoordinate;
        ClosestAlienXCoordinate = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY())[0];
        ClosestAlienYCoordinate = ctx.getView().getClosestAlienPos(ctx.getX(), ctx.getY())[1];        
        
        
        //Checks to see if the closest alien is withen moving capability.
        if(Math.abs(ClosestAlienXCoordinate - ctx.getX()) + Math.abs(ClosestAlienYCoordinate - ctx.getY()) <= ctx.getEnergy())
        {

            HorizontalMove = ClosestAlienXCoordinate - ctx.getX();
            VerticalMove = ClosestAlienYCoordinate - ctx.getY();
        }
        return new MoveDir(HorizontalMove, VerticalMove);
    }

    public Action getAction() {
        //checks if alien is on the same position, if so, then fights with the priorly designated amount of energy
        try
        {
            if(ctx.getView().isAlienAtPos(ctx.getX(), ctx.getY()))
            {
                return new Action(ActionCode.Fight, (fightStrength));
            }
        }catch (Exception e)
        {
        return new Action(ActionCode.Fight, ctx.getEnergy());
        }
        //if it doesnt fight, it chooses a item to do depending on how much energy it has.
        if(ctx.getEnergy()< 2)
        {
            return new Action(ActionCode.Gain);
        }
        else if(ctx.getEnergy() < 3)
        {
            return new Action(ActionCode.Research);
        }
        else
        {
            return new Action(ActionCode.Spawn);
        }
        
    }

    @Override
    public void processResults() 
    {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
