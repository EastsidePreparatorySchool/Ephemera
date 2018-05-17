/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

import alieninterfaces.*;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.geometry.Point3D;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 *
 * @author gmein
 */
public class Caelusite implements Alien,AlienComplex {

    Context ctx;
    boolean tooComplex = false;

    final boolean debug = true;

    public Caelusite() {
    }

    @Override
    public void init(Context game_ctx, int id, int parent, String message) {
        ctx = game_ctx;
        ctx.debugOut("Initialized at "
                + ctx.getPosition().toString()
                + " E: " + Double.toString(ctx.getEnergy())
                + " T: " + Double.toString(ctx.getTech()));
    }

    // Martians move left, right, left, right
    @Override
    public Vector2 getMove() {

        /*ctx.debugOut("Move requested,"
                + ctx.getStateString());
        int move_energy;

        // don't move more than you have tech
        move_energy = (int) Math.min(ctx.getTech(), ctx.getEnergy());
        // don't move more than 5, leave energy for other stuff
        move_energy = Math.min(move_energy, 5);

        int powerX;
        int powerY;

        if (move_energy <= 2) {
            powerX = ctx.getRandomInt(3) - 1;
            powerY = ctx.getRandomInt(3) - 1;
        } else {
            // spend a random amount of that moving into x direction
            powerX = ctx.getRandomInt(move_energy / 2);
            powerY = ctx.getRandomInt(move_energy / 2);
        }

        int x = powerX * (ctx.getRandomInt(2) == 0 ? -1 : 1);
        int y = powerY * (ctx.getRandomInt(2) == 0 ? -1 : 1);

        IntegerDirection dir = new IntegerDirection(x, y);

        // don't park, a planet might hit you
        if (dir.x == 0 & dir.y == 0) {
            dir = new IntegerDirection(ctx.getRandomInt(2) == 0 ? -1 : 1, ctx.getRandomInt(2) == 0 ? -1 : 1);
        }

        try {
            if (ctx.getView(move_energy).getSpaceObjectAtPos(ctx.getPosition().add(dir).round()) != null) {
                // don't be a dumbass, don't move into a star
                ctx.debugOut("Avoiding Star at " + ctx.getPosition().add(dir).toString());
                dir = new IntegerDirection(-dir.x, -dir.y);
            }
        } catch (Exception e) {
            // do something here to deal with errors
            ctx.debugOut("EXPLAIN?????? " + e.toString());
            dir = new IntegerDirection(1, 1);
        }

        ctx.debugOut("Moving to " + ctx.getPosition().add(dir).toString() + ctx.getStateString());
        return dir.v2();*/
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action getAction() {

        //if(ctx.getEnergy() > 100) return new Action(Action.ActionCode.Spawn, 5);
        
        ctx.debugOut("Gaining energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {
        
    }

    @Override
    public void receive(String[] messages) {
        
    }

    @Override
    public void processResults() {
    }

    @Override
    public Vector2 getAccelarate() {
        if (ctx.getGameTurn() % 15 == 0) return new Vector2(1,0);
        
        return new Vector2(0,0); 
    }
}
