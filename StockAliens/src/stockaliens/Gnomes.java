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

		return null;
	}

	@Override
	public Action getAction() {

		return null;
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
