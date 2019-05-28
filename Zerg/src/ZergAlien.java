
/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
/**
 *
 * @author gmein@eastsideprep.org
 */
import java.util.List;
import java.util.stream.Collectors;
import org.eastsideprep.spacecritters.alieninterfaces.*;
//import com.interactivemesh.jfx.importer.stl.StlMeshImporter;

public class ZergAlien implements Alien, AlienComplex /*, AlienShapeFactory*/ {

    final double MIN_ENERGY = 50.0;
    final double SPAWN_ENERGY = 200.0;
    final double MIN_TECH = 10.0;
    final int RESEARCH_PERCENT = 30;
    final int COAST_TIME = 100000;
    final boolean SPAWN = true;

    final int PHASE_INVALID = -1;

    final int PHASE_RECTIFY = 1;
    final int PHASE_WIDEN_ACC_1 = 2;
    final int PHASE_WIDEN_ACC_2 = 3;
    final int PHASE_TARGET_ACC = 4;
    final int PHASE_TARGET_TRIM = 5;
    final int PHASE_TARGET_DEC = 6;
    final int PHASE_SPAWN = 7;
    final int PHASE_COAST = 8;

    ContextComplex ctx;

    int startCoastTurn;
    SpaceObject origin;
    SpaceObject target = null;
    WorldVector targetWorldPosition = null;
    double radiusFrom = 0;
    String targetList;
    boolean acquired;
    int beenHere;

    int phase = 1;

    // don't do anything in the contructor, implicitly or explicitly!
    public ZergAlien() {
    }

    @Override
    public void initComplex(ContextComplex ctx, int id, int parentId, String message) {
        // hang on to the context object
        this.ctx = ctx;
        this.targetList = null;

        if (parentId == 0) {
            ctx.debugOut("Zerg " + id + " initial alien initializing ");

            // get all objects
            List<SpaceObject> lso = ctx.getAllSpaceObjects();
            // stars only please
            lso.removeIf((e) -> e.kind.equalsIgnoreCase("planet"));
            // sort by who is closer to what we are orbiting right now
            lso.sort((a, b) -> (int) (a.worldPosition.subtract(ctx.getFocus().worldPosition).magnitude()
                    - b.worldPosition.subtract(ctx.getFocus().worldPosition).magnitude()));
            // remove earth  -- isnt earth a planet? should that not be removed in line 73
            lso.remove(0);
            // make target list
            this.targetList = lso.stream().map((e) -> e.name).collect(Collectors.joining(","));
        } else {
            ctx.debugOut("Zerg " + id + " child initializing with target list " + message);
            this.targetList = message;
        }

        this.target = getNextTarget();
        this.targetWorldPosition = target.worldPosition;

        if (this.target == null) {
            ctx.debugOut("init: no target, floating");
            phase = -1;
            return;
        }
        ctx.debugOut("init: target set to " + this.target.name);
        phase = PHASE_RECTIFY;
    }

    SpaceObject getNextTarget() {
        if (this.targetList == null || this.targetList.length() == 0) {
            return null;
        }
        String result = this.targetList.substring(0, this.targetList.indexOf(","));
        if (result.length() == 0) {
            return null;
        }

        this.targetList = targetList.substring(result.length() + 1);
        return ctx.getSpaceObject(result);

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
            if (phase != PHASE_SPAWN || ctx.getEnergy() < SPAWN_ENERGY) {
                return new Action(Action.ActionCode.Gain);
            }
            WorldVector deltaV = new WorldVector(ctx.getVelocity()
                    .scale(0.001 * ctx.getRandomInt(10))
                    .rotate(Math.PI / (ctx.getRandomInt(10) + 1) - Math.PI / 2));
            //System.out.println("  spawn v "+ctx.getVelocity());
            //System.out.println("  spawn acc " + deltaV);
            ctx.debugOut("phase spawn, with list " + this.targetList);
            Action a = new Action(Action.ActionCode.Spawn, deltaV, 5, this.targetList);
            ctx.debugOut("phase coast");
            this.startCoastTurn = ctx.getGameTurn();
            phase = PHASE_COAST;
            return SPAWN ? a : null;
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
        Orbit orbit = ctx.getOrbit();
        WorldVector r1;
        WorldVector r2;
        double indicator;
        double vTransfer;
        double radiusBigger = 700000;
        double radiusTo;
        double normalAccuracy = 0.99;
        double highAccuracy = 0.99999;

        // first, do we have energy and tech?
        if (ctx.getEnergy() < MIN_ENERGY || ctx.getTech() < MIN_TECH) {
            return null;
        }

        WorldVector v = ctx.getVelocity();
        WorldVector r = ctx.getWorldPosition();
        SpaceObject so = ctx.getFocus();
        WorldVector f = so.worldPosition;

        double mu = ctx.getOrbit().mu;

        switch (phase) {
            case PHASE_RECTIFY:
                origin = so;
                if (ctx.getOrbit().e == 1.0) {
                    // already circular, no action needed
                    phase = PHASE_WIDEN_ACC_1;
                    return null;
                }
                // phase 1 - burn at aphelion make initial orbit cicular
                r1 = r.subtract(f); //vector from focus to alien
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);         //unit vector, points towards periapse
                radiusFrom = orbit.p / (1 + orbit.e);//ctx.getOrbit().a * (1 - ctx.getOrbit().e);   -- periapse now calculated with semi-latus rectum, instead semi major axis
                radiusTo = orbit.p / (1 - orbit.e);  //ctx.getOrbit().a * (1 + ctx.getOrbit().e);   -- ditto for apoapse
                indicator = r1.unit().dot(r2.unit()); //how far away are the vectors are -- am I at apoapse
                if ((indicator < -normalAccuracy)) {
                    vTransfer = calculateHohmannTransferAtPerigee(mu, radiusFrom, radiusTo);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    ctx.debugOut("phase rectify " + deltaV + ", mag " + deltaV.magnitude());
                    phase = 2;
                    return deltaV;
                }
                return null;
            case PHASE_WIDEN_ACC_1:
                // phase 2 - widen orbit - burn at perihelion make orbit wider (1000000)
                r1 = ctx.getWorldPosition().subtract(ctx.getFocus().worldPosition);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                radiusFrom = orbit.p / (1 + orbit.e); //ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator > normalAccuracy)) {
                    vTransfer = calculateHohmannTransferAtPerigee(mu, radiusFrom, radiusBigger);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    ctx.debugOut("phase widen acc 1 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = PHASE_WIDEN_ACC_2;
                    return deltaV;
                }
                return null;

            case PHASE_WIDEN_ACC_2:
                // phase 3 - burn at aphelion to get into widened orbit
                r1 = ctx.getWorldPosition().subtract(ctx.getFocus().worldPosition);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator < -normalAccuracy)) {
                    vTransfer = calculateHohmannTransferAtApogee(mu, radiusFrom, radiusBigger);
                    WorldVector deltaV = v.scaleTo(vTransfer);
                    ctx.debugOut("phase widen acc 2 " + deltaV + ", mag " + deltaV.magnitude());
                    phase = PHASE_TARGET_ACC;
                    return deltaV;
                }
                return null;

            case PHASE_TARGET_ACC:
                // accelerate at perihelion to get into elliptical orbit reaching close to target
                if (this.target.name.equalsIgnoreCase(ctx.getFocus().name)) {
                    ctx.debugOut("phase target acc: already there");
                    phase = PHASE_TARGET_TRIM;
                    return null;
                }

                r1 = r.subtract(f);
                radiusFrom = r1.magnitude();
                r2 = this.targetWorldPosition.subtract(f);
                indicator = r1.unit().dot(r2.unit());
                if ((indicator < -normalAccuracy)) {
                    vTransfer = calculateHohmannTransferAtPerigee(mu, radiusFrom, r2.magnitude() * 1.2);

                    WorldVector deltaV = v.scaleTo(vTransfer);
                    ctx.debugOut("phase target acc" + deltaV + ", mag " + deltaV.magnitude());
                    phase = PHASE_TARGET_TRIM;
                    acquired = false;
                    this.beenHere = 0;
                    return deltaV;
                } else {
                    //ctx.debugOut("indicator: "+indicator);
                }
                return null;

            case PHASE_TARGET_TRIM:
                if (this.origin.name.equalsIgnoreCase(so.name)) {
                    // we did not leave the origin star's orbit, continue floating
                    this.beenHere++;
                    if (this.beenHere < 2000) {
                        //keep coasting and hoping
                        return null;
                    }
                    // ok accept it, we won't leave this orbit to get back into circular orbit and try again
                }
                // at this point, we might have arrived in some orbit, but perhaps not that of our target star
                if (!this.acquired) {
                    this.acquired = true;
                    ctx.debugOut("acquired by " + so.name);
                }
                // check the perigee, if it is not tight enough, perform a 1% retro burn to fix that
                // do that as often as necessary

                double perihelion = orbit.p / (1 + orbit.e); //ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                if (perihelion > radiusBigger / 2) {
                    WorldVector deltaV = v.scaleTo(-v.magnitude() * 0.01);
                    ctx.debugOut("phase trim dec " + deltaV + ", mag " + deltaV.magnitude());
                    return deltaV;
                }
                phase = PHASE_TARGET_DEC;
                return null;

            case PHASE_TARGET_DEC:
                // retrograde burn at perihelion to get into small circular orbit
                r1 = r.subtract(f);
                r2 = new WorldVector(1, 0).rotate(ctx.getOrbit().rotation);
                double radiusSmall = orbit.p / (1 + orbit.e); //ctx.getOrbit().a * (1 - ctx.getOrbit().e);
                double radiusBig = orbit.p / (1 - orbit.e); //ctx.getOrbit().a * (1 + ctx.getOrbit().e);

                indicator = r1.unit().dot(r2.unit());
                if ((indicator > normalAccuracy)) {
                    // calculate transfer as if we wanted to grow orbit, then use negative
                    vTransfer = calculateHohmannTransferAtPerigee(mu, radiusSmall, radiusBig);
                    WorldVector deltaV = v.scaleTo(-vTransfer);
                    ctx.debugOut("phase target dec " + deltaV + ", mag " + deltaV.magnitude());

                    if (this.target.name.equalsIgnoreCase(so.name)) {
                        phase = PHASE_SPAWN;

                    } else {
                        // we did not arrive at target, go back to phase 1
                        phase = PHASE_RECTIFY;

                    }
                    return deltaV;
                }
                return null;

            case PHASE_SPAWN:
                // will be advanced by spawning
                return null;

            case PHASE_COAST:
                // will stay in this phase for a while

                if (ctx.getGameTurn() - this.startCoastTurn > COAST_TIME) {
                    // a lot of turns have gone by. Go to next target, do it all again
                    if (so.name.equals(this.target.name)) {
                        this.target = getNextTarget();
                    }
                    this.targetWorldPosition = this.target.worldPosition;
                    ctx.debugOut("continue: target set to " + this.target.name);
                    phase = PHASE_RECTIFY;
                }
                return null;

            default:
                return null;
        }
    }

    double calculateHohmannTransferAtPerigee(double mu, double radiusFrom, double radiusTo) {
        double vTransfer = Math.sqrt(mu / radiusFrom) * (Math.sqrt(2 * radiusTo / (radiusFrom + radiusTo)) - 1);
        return vTransfer;
    }

    double calculateHohmannTransferAtApogee(double mu, double radiusFrom, double radiusTo) {
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

