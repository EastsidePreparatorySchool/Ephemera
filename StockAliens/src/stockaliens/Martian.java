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
    Context ctx;
    int HorizontalMove;
    int VerticalMove;
    int Remainder;
    private static Random rnd = new Random();
    public static boolean getRandomBoolean() {
        return rnd.nextBoolean();
    }

    public Martian() {
    }

    @Override
    public void init(Context ctx) {
        ctx = ctx;

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
        ctx.debugOut("Martian initialized");
    }
    
    public MoveDir getMove() {
        return new MoveDir(HorizontalMove, VerticalMove);
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
        return new Action(ActionCode.Fight, ctx.getEnergy());
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

    @Override
    public void processResults() {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
