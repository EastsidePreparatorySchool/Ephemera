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

    int startTurn;
    SpaceObject target = null;
    WorldVector targetWorldPosition = null;
    double radiusFrom = 0;

    int phase = 1;

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
            if (phase < 10 || ctx.getEnergy() < SPAWN_ENERGY) {
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
        WorldVector r1;
        WorldVector r2;
        double indicator;
        double vTransfer;
        double radiusBigger = 1000000;
        double radiusTo;

        // first, do we have energy and tech?
        if (ctx.getEnergy() < MIN_ENERGY || ctx.getTech() < MIN_TECH) {
            return null;
        }

        WorldVector v = ctx.getVelocity();
        WorldVector r = ctx.getWorldPosition();
        WorldVector f = ctx.getFocus().worldPosition;
        SpaceObject so = ctx.getFocus();
        double mu = ctx.getOrbit().mu;

        switch (phase) {
            case 1:
                // phase 1 - burn at aphelion make initial orbit cicular
                r1 = ctx.getWorldPosition().subtract(ctx.getFocus().worldPosition);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                radiusFrom = ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                radiusTo = ctx.getOrbit().a * (1 + ctx.getOrbit().e);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator < -0.9999)) {
                    vTransfer = calculateHohmannTransferFrom(mu, radiusFrom, radiusTo);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    System.out.println("--------------acc phase 1 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 2;
                    return deltaV;
                }
                return null;
            case 2:
                // phase 2 - widen orbit - burn at perihelion make orbit wider (1000000)
                r1 = ctx.getWorldPosition().subtract(ctx.getFocus().worldPosition);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                radiusFrom = ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator > 0.9999)) {
                    vTransfer = calculateHohmannTransferFrom(mu, radiusFrom, radiusBigger);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    System.out.println("--------------acc phase 2 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 3;
                    return deltaV;
                }
                return null;

            case 3:
                // phase 3 - burn at aphelion to get into widened orbit
                r1 = ctx.getWorldPosition().subtract(ctx.getFocus().worldPosition);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator < -0.9999)) {
                    vTransfer = calculateHohmannTransferTo(mu, radiusFrom, radiusBigger);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    System.out.println("--------------acc phase 3 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 4;
                    return deltaV;
                }
                return null;

            case 4:
                // accelerate at perihelion to get into elliptical orbit reaching close to target
                if (this.target.name.equalsIgnoreCase(ctx.getFocus().name)) {
                    System.out.println("Already there");
                    return null;
                }
                r1 = r.subtract(f);
                radiusFrom = r1.magnitude();
                r2 = this.targetWorldPosition.subtract(f);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator < -0.99999)) {
                    vTransfer = calculateHohmannTransferFrom(mu, radiusFrom, r2.magnitude() * 1.01);

                    WorldVector deltaV = v.scaleTo(vTransfer);
                    System.out.println("--------------acc phase 4 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 5;
                    return deltaV;
                }
                return null;

            case 5:
                // retrograde burn at perihelion to get into small circular orbit
                r1 = r.subtract(f);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                radiusTo = ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                radiusFrom = ctx.getOrbit().a * (1 + ctx.getOrbit().e);
     
                indicator = r1.unit().dot(r2.unit());
                if ((indicator > 0.9999)) {
                    vTransfer = calculateHohmannTransferFrom(mu, radiusFrom, radiusTo);

                    WorldVector deltaV = v.scaleTo(-vTransfer);
                     deltaV = v.scaleTo(-1.8e-4);
                    System.out.println("--------------dec phase 5 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 6;
                    return deltaV;
                }
                return null;

            default:
                return null;
        }
    }

    double calculateHohmannTransferFrom(double mu, double radiusFrom, double radiusTo) {
        double vTransfer = Math.sqrt(mu / radiusFrom) * (Math.sqrt(2 * radiusTo / (radiusFrom + radiusTo)) - 1);
        return vTransfer;

    }

    double calculateHohmannTransferTo(double mu, double radiusFrom, double radiusTo) {
        double vTransfer = Math.sqrt(mu / radiusTo) * (1 - Math.sqrt(2 * radiusFrom / (radiusFrom + radiusTo)));
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
