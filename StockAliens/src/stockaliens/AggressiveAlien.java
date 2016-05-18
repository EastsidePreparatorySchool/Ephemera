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
public class AggressiveAlien implements Alien, AlienShapeFactory {

    Context ctx;
    static TriangleMesh dalekMesh;
    boolean tooComplex = false;

    final boolean debug = true;

    public AggressiveAlien() {
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
    public Direction getMove() {

        ctx.debugOut("Move requested,"
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

        Direction dir = new Direction(x, y);

        // don't park, a planet might hit you
        if (dir.x == 0 & dir.y == 0) {
            dir = new Direction(ctx.getRandomInt(2) == 0 ? -1 : 1, ctx.getRandomInt(2) == 0 ? -1 : 1);
        }

        try {
            if (ctx.getView(move_energy).getSpaceObjectAtPos(ctx.getPosition().add(dir)) != null) {
                // don't be a dumbass, don't move into a star
                ctx.debugOut("Avoiding Star at " + ctx.getPosition().add(dir).toString());
                dir = new Direction(-dir.x, -dir.y);
            }
        } catch (Exception e) {
            // do something here to deal with errors
            ctx.debugOut("EXPLAIN?????? " + e.toString());
            dir = new Direction(1, 1);
        }

        ctx.debugOut("Moving to " + ctx.getPosition().add(dir).toString() + ctx.getStateString());
        return dir;
    }

    @Override
    public Action getAction() {

        ctx.debugOut("Action requested," + ctx.getStateString());

        // catch any shenanigans
        if (ctx.getEnergy() > 100) {
            try {
                View view = ctx.getView((int) ctx.getTech());
                if (view.getAliensAtPos(ctx.getPosition()).size() > 1) {
                    ctx.debugOut("Uh-oh.There is someone else here."
                            + ctx.getStateString());
                }

                // do we have enough energy?
                if (ctx.getEnergy() < 10) {
                    // no, charge
                    ctx.debugOut("Choosing to gain energy,"
                            + ctx.getStateString());
                    return new Action(Action.ActionCode.Gain);
                }

                // do we have enough tech?
                if (ctx.getTech() < 30 && ctx.getEnergy() > ctx.getTech()) {
                    // no, research
                    ctx.debugOut("Choosing to research"
                            + ctx.getStateString());
                    return new Action(Action.ActionCode.Research);
                }

                // is there another alien on our position?
                if (view.getAliensAtPos(ctx.getPosition()).size() > 1
                        && ctx.getEnergy() > ctx.getFightingCost() + 2) {
                    ctx.debugOut("EXTERMINATE!!!!!"
                            + ctx.getStateString());

                    return new Action(Action.ActionCode.Fight, (int) ctx.getEnergy() - 2 - ctx.getFightingCost());
                }

                if (ctx.getEnergy() > (ctx.getSpawningCost() + 10)) {
                    // no other aliens here, have enough stuff, spawn!
                    ctx.debugOut("AAs RULE SUPREME! SPAWNING!"
                            + ctx.getStateString());

                    return new Action(Action.ActionCode.Spawn, 5);
                }
            } catch (Exception e) {
                // do something here to deal with errors
                ctx.debugOut("EXPLAIN?????? " + e.toString());
            }
        }
        ctx.debugOut("Gaining energy"
                + ctx.getStateString());
        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void communicate() {
        if (ctx.getEnergy() > 100) {
            try {
                if (ctx.getGameTurn() % 20 == 0) {
                    ctx.broadcastAndListen("I say: AAs rule supreme!!!!!", 1, true);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void receive(String[] messages) {
        for (String s : messages) {
            ctx.debugOut(s);
        }
    }

    @Override
    public void processResults() {
    }

    @Override
    public Shape3D getShape(int complexityLimit) {
        MeshView dalek;
        if (dalekMesh == null) {
            try {
                StlMeshImporter importer = new StlMeshImporter();
                importer.read(this.getClass().getResource("/Resources/DalekFull.stl"));
                dalekMesh = importer.getImport();
                tooComplex = (dalekMesh.getFaceElementSize() > complexityLimit);
            } catch (Exception e) {
                dalekMesh = null;
            }
        }

        if (dalekMesh != null && !tooComplex) {
            dalek = new MeshView(dalekMesh);
            dalek.getTransforms().setAll(new Scale(0.01, 0.01, 0.01));
            dalek.getTransforms().add(new Translate(0, 20, 0));
            dalek.getTransforms().add(new Rotate(-90, new Point3D(1, 0, 0)));
            return dalek;
        }
        return null;
    }
}
