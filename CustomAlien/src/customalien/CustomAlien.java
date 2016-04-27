/*
 * Example empty custom alien for free distribution and sharing
 */

package customalien;

import alieninterfaces.*;

public class CustomAlien implements Alien{

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        throw new UnsupportedOperationException("Not supported yet."); 
    } 
    
    @Override
    public void communicate() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void receive(String[] messages) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public MoveDir getMove() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Action getAction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beThoughtful() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
