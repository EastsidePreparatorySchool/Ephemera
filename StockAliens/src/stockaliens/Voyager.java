/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockaliens;

/**
 *
 * @author gmein@eastsideprep.org
 */
import alieninterfaces.*;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.geometry.Point3D;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import static stockaliens.AggressiveAlien.dalekMesh;

public class Voyager implements Alien, AlienComplex, AlienShapeFactory {

    final double MIN_ENERGY = 20.0;
    final double MIN_TECH = 10.0;
    final int RESEARCH_PERCENT = 30;

    Context ctx;

    static TriangleMesh vger;
    boolean tooComplex = false;

    // don't do anything in the contructor, implicitly or explicitly!
    public Voyager() {
    }

    @Override
    public void init(Context ctx, int id, int parentId, String message) {
        // hang on to the context object
        this.ctx = ctx;
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

        // first, do we have energy and tech?
        if (ctx.getEnergy() < MIN_ENERGY || ctx.getTech() < MIN_TECH) {
            return new Vector2(0, 0);
        }

        // if we are all good, move us out a little from the center (Earth/Sun)
        // but only every tenth turn or so
            Position p = ctx.getPosition();
            return new Vector2(p.scaleToLength(0.3));
    }

    @Override
    public Vector2 getMove() {
        throw new UnsupportedOperationException("Complex alien does not support simple moves");
    }

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
}
