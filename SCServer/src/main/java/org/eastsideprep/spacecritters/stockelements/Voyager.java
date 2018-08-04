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
    final double MIN_TECH = 10.0;
    final int RESEARCH_PERCENT = 30;

    Context ctx;

    static TriangleMesh vger;
    boolean tooComplex = false;
    int startTurn;
    long tBurn = 0;
    SpaceObject target = null;
    Position targetPosition = null;

    // don't do anything in the contructor, implicitly or explicitly!
    public Voyager() {
    }

    @Override
    public void init(Context ctx, int id, int parentId, String message) {
        // hang on to the context object
        this.ctx = ctx;
        this.startTurn = ctx.getGameTurn();
        this.target = ctx.getSpaceObject("ProximaCentauri");
        this.targetPosition = target.position;
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
            return new Action(Action.ActionCode.Gain);

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
    public Vector2 getAccelerate() {
        Vector3 v = ctx.getVelocity();
        Vector3 p = ctx.getPosition();
        Vector3 f = ctx.getFocus().position;
        double M = ctx.getMeanAnomaly();
        Vector3 d3;
        Vector2 d2;
        double indicator;

        // if we are orbiting anything else than SOL, lay off the gas pedal
        SpaceObject so = ctx.getFocus();
        if (so != null) {
            if (!so.name.equalsIgnoreCase("SOL")) {
                return null;
            }
        }

        // first, do we have energy and tech?
        if (ctx.getEnergy() < MIN_ENERGY || ctx.getTech() < MIN_TECH) {
            return null;
        }

        // accelerate at the right time
        d2 = new Vector2(f.subtract(this.targetPosition));
        if (((d2.angle() - M) < 0.2) && ((d2.angle() - M) > -0.1)) {
            tBurn = System.currentTimeMillis();
            return new Vector2(v.scaleToLength(0.19));
        } else {
            // widen the orbit a bit, but push toward target     
            tBurn = System.currentTimeMillis();
            Vector3 k = new Vector3(0, 0, 1);
            d3 = d2.scale(-1).scaleToLength(0.0005);
            return new Vector2(d3);
        }
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
}
