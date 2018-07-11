
import alieninterfaces.Action;
import alieninterfaces.Alien;
import alieninterfaces.Context;
import alieninterfaces.Vector2;



/**
 *
 * @author gunnar
 */
public class TestAlien implements Alien{
    Context ctx;

    @Override
    public void init(Context ctx, int id, int parent, String message) {
        this.ctx = ctx;
        ctx.debugOut("TestAlien is alive!");
    }

    @Override
    public void communicate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void receive(String[] messages) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Action getAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processResults() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
