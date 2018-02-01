/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

import alieninterfaces.*;

/**
 *
 * @author ditto2642
 */

public class Gnomes implements Alien{
	Context ctx;
	final boolean debug = true;
	@Override
	public void init(Context game_ctx, int id, int parent, String message) {
		ctx = game_ctx;

	}

	@Override
	public Direction getMove() {
		double techLevel = ctx.getTech();
		int y = ctx.getRandomInt(3);
		int x = ctx.getRandomInt(3);
		//but don't move into star
        try {
            if (ctx.getView(2).getSpaceObjectAtPos(ctx.getPosition().add(new Direction((int) x, (int) y))) != null) {
                y -= y > 0 ? 1 : -1;
            }
        } catch (NotEnoughEnergyException | NotEnoughTechException | View.CantSeeSquareException ex) {
        }
		return new Direction(x,y);
	}

	@Override
	public Action getAction() {
		double techLevel = ctx.getTech();
		  if (ctx.getEnergy() < 10) {
              return new Action(Action.ActionCode.Gain);
          }
		  if(ctx.getTech()<5) {
			  return new Action(Action.ActionCode.Research);
		  }
		  if(ctx.getTech()>=5&&ctx.getEnergy()>=14) {
			  return new Action(Action.ActionCode.Spawn, 4);
		  }
		  return new Action(ctx.getRandomInt(1) == 1?Action.ActionCode.Gain:Action.ActionCode.Research);
	}


	@Override
	public void communicate() {
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void receive(String[] messages) {
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void processResults() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}
