/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.stockelements;

/**
 *
 * @author gmein@eastsideprep.org
 */
import org.eastsideprep.spacecritters.alieninterfaces.*;
//import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.geometry.Point3D;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import static org.eastsideprep.spacecritters.stockelements.AggressiveAlien.dalekMesh;

public class Voyager implements Alien, AlienComplex /*, AlienShapeFactory*/ {

    final double MIN_ENERGY = 50.0;
    final double SPAWN_ENERGY = 200.0;
    final double MIN_TECH = 10.0;
    final int RESEARCH_PERCENT = 30;

    ContextComplex ctx;

    static TriangleMesh vger;
    boolean tooComplex = false;
    int startTurn;
    long tBurn = 0;
    SpaceObject target = null;
    WorldVector targetWorldPosition = null;
    boolean accelerate;

    // don't do anything in the contructor, implicitly or explicitly!
    public Voyager() {
    }

    @Override
    public void initComplex(ContextComplex ctx, int id, int parentId, String message) {
        // hang on to the context object
        this.ctx = ctx;
        this.startTurn = ctx.getGameTurn();
        this.target = ctx.getSpaceObject("ProximaCentauri");
        this.targetWorldPosition = target.worldPosition;
        this.accelerate = true;
    }

    @Override
    public Action getAction() {

        try {
            // gain first
            if (ctx.getEnergy() < MIN_ENERGY) {
                return new Action(Action.ActionCode.Gain);
            }

            // Adopted from Trajan: research a portion of the time, otherwise gain
            if (ctx.getRandomInt(100) > (100 - RESEARCH_PERCENT)) {
                return new Action(Action.ActionCode.Research);
            }
            if (accelerate || ctx.getEnergy() < SPAWN_ENERGY) {
                return new Action(Action.ActionCode.Gain);
            }
            WorldVector deltaV = new WorldVector(ctx.getVelocity()
                    .scale(0.001 * ctx.getRandomInt(10))
                    .rotate(Math.PI / (ctx.getRandomInt(10) + 1) - Math.PI / 2));
            //System.out.println("  spawn v "+ctx.getVelocity());
            //System.out.println("  spawn acc " + deltaV);
            return new Action(Action.ActionCode.Spawn, deltaV, 5);

        } catch (Exception e) {
            ctx.debugOut("Something went wrong in getAction, " + ctx.getStateString());
        }

        return new Action(Action.ActionCode.Gain);
    }

    @Override
    public void processResults() {
    }

    @Override
    public void communicate() {
    }

    @Override
    public void receive(String[] strings) {
    }

    @Override
    public WorldVector getAccelerate() {
        WorldVector v = ctx.getVelocity();
        WorldVector p = ctx.getWorldPosition();
        WorldVector f = ctx.getFocus().worldPosition;

        // if we are orbiting anything else than SOL, lay off the gas pedal
        // even if we later go back
        SpaceObject so = ctx.getFocus();
        if (so != null) {
            if (!so.name.equalsIgnoreCase("SOL")) {
                accelerate = false;
            }
        }

        if (accelerate) {
            // first, do we have energy and tech?
            if (ctx.getEnergy() < MIN_ENERGY || ctx.getTech() < MIN_TECH) {
                return null;
            }

            // accelerate at the right time
            Vector3 d1 = p.subtract(f);
            Vector3 d2 = this.targetWorldPosition.subtract(f);
            double indicator = d1.unit().dot(d2.unit());
            if ((indicator < -0.999999) && tBurn == 0) {
                tBurn = System.currentTimeMillis();
                double mu = ctx.getOrbit().mu;
                double vTransfer = calculateHohmannTranferDeltaV(mu, p, v, targetWorldPosition);

                WorldVector deltaV = v.scaleTo(vTransfer);
                System.out.println("--------------acc " + deltaV + ", mag " + deltaV.magnitude());
                return deltaV;
            }
        }

        return null;
    }

    double calculateHohmannTranferDeltaV(double mu, WorldVector r, WorldVector v, WorldVector target) {
        double vm = v.magnitude();
        double rm = r.magnitude();
        double d = target.magnitude();

        double vTransfer = Math.sqrt(mu / rm) * (Math.sqrt(2 * d / (rm + d)) - 1);
        
        vTransfer *= 1.00; // because life is not perfect
        
        return vTransfer;
    }

    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Complex alien does not support simple moves");
    }

    /*
    @Override
    public Shape3D getShape(int complexityLimit) {
        MeshView v;
        if (vger == null && !tooComplex) {
            try {
                StlMeshImporter importer = new StlMeshImporter();
                importer.read(this.getClass().getResource("/Resources/voyager.stl"));
                vger = importer.getImport();
                tooComplex = (vger.getFaceElementSize() > complexityLimit);
            } catch (Exception e) {
                vger = null;
            }
        }

        if (vger != null && !tooComplex) {
            v = new MeshView(vger);
            v.getTransforms().setAll(new Scale(0.03, 0.03, 0.03));
//            v.getTransforms().add(new Translate(0, 20, 0));
//            v.getTransforms().add(new Rotate(-90, new Point3D(1, 0, 0)));
            return v;
        }
        return null;
    }
     */
    @Override
    public void init(Context ctx, int id, int parent, String message) {
    }
}
