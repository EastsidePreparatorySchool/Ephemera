/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.alieninterfaces.AlienShapeFactory;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.effect.Glow;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author gunnar
 */
public class Scene3D {

    // window
    final private int pxX;
    final private int pxY;
    final SpaceCritters gameShell;
    final HBox outer;
    final Group root;
    final Group grid;
    boolean gridVisible;

    // master lists
    final HashMap<Integer, Star3D> stars;
    final HashMap<Integer, Planet3D> planets;
    final HashMap<Integer, Alien3D> aliens;
    final ConcurrentLinkedQueue<Alien3D> updateQueue;

    // camera
    final private PerspectiveCamera camera;
    final private PointLight pLight;
    private double focusX;
    private double focusY;
    private double xRot;
    private double yRot;
    private double zTrans;

    // object positioning
    static double spacing;
    double objectElevation;

    public Scene3D(SpaceCritters gameShellInstance, int widthPX, int heightPX) {
        this.gameShell = gameShellInstance;
        this.stars = new HashMap<>(50);
        this.planets = new HashMap<>(50);
        this.aliens = new HashMap<>(100000);
        this.updateQueue = new ConcurrentLinkedQueue<>();

        // initial camera values
        this.xRot = -20;
        this.yRot = -10;
        this.zTrans = -530;
        this.spacing = 1.0;
        this.pxX = widthPX;
        this.pxY = heightPX;
        this.focusX = 0.0;
        this.focusY = -80.0;

        this.root = new Group();

        // axes
        this.grid = buildAxes();
        this.gridVisible = true;
        this.root.getChildren().addAll(this.grid);

        AmbientLight aLight = new AmbientLight(Color.rgb(10, 10, 10));
        pLight = new PointLight(Color.ANTIQUEWHITE);
        pLight.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
        root.getChildren().addAll(aLight, pLight);

        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
        camera.setNearClip(0.1);
        camera.setFarClip(2000);

        root.getChildren().add(camera);

        // Use a SubScene       
        SubScene subScene = new SubScene(root, pxX, pxY, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        outer = new HBox(subScene);
        subScene.widthProperty().bind(outer.widthProperty());
        subScene.heightProperty().bind(outer.heightProperty());
        //subScene.setOnMouseClicked((e) -> focus(e));

        outer.setPrefSize(pxX, pxY);
        outer.setMaxSize(pxX, pxY);
        outer.setMinSize(200, 200);
        outer.setAlignment(Pos.TOP_LEFT);
        root.setAutoSizeChildren(true);

    }

    double cameraDistance = 450;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    public void updateCamera() {
        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
    }

    public void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                this.xRot = -20;
                this.yRot = -10;
                this.zTrans = -530;
                this.spacing = 1.0;
                this.objectElevation = 0;
                this.focusX = 0.0;
                this.focusY = -80.0;
                this.updatePlanetsAndStars();
                break;
            case PLUS:
            case EQUALS:
                if (zTrans < 1000) {
                    zTrans += 5;
                }
                break;
            case MINUS:
                if (zTrans > -1000) {
                    zTrans -= 5;
                }
                break;
            case RIGHT:
                yRot -= 5;
                break;
            case LEFT:
                yRot += 5;
                break;
            case DOWN:
                if (e.isShiftDown()) {
                    objectElevation = 0;
                    this.updatePlanetsAndStars();
                } else {
                    xRot += 2;
                }
                break;
            case UP:
                if (e.isShiftDown()) {
                    if (objectElevation >= -20) {
                        objectElevation -= 1;
                        this.updatePlanetsAndStars();
                    }
                } else {
                    xRot -= 2;

                }
                break;

            case PAGE_UP:
                if (objectElevation >= -20) {
                    objectElevation -= 1;
                    this.updatePlanetsAndStars();
                }
                break;
            case PAGE_DOWN:
                objectElevation = 0;
                this.updatePlanetsAndStars();
                break;

            default:
                return; // do not consume this event if you can't handle it
        }

        updateCamera();
        /*pLight.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );*/

        e.consume();
    }

    public void handleDrag(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifierFactor = 0.1;

        if (me.isPrimaryButtonDown()) {
            yRot += mouseDeltaX * modifierFactor * 2.0;
            xRot -= mouseDeltaY * modifierFactor * 2.0;
        } else if (me.isSecondaryButtonDown()) {
            focusX -= mouseDeltaX * modifierFactor;
            focusY += mouseDeltaY * modifierFactor;
        }

        updateCamera();
    }

    public void handleClick(MouseEvent me) {
        mousePosX = mouseOldX = me.getSceneX();
        mousePosY = mouseOldY = me.getSceneY();

        focus(me);
        //mouseOldX = me.getSceneX();
        //mouseOldY = me.getSceneY();

    }

    public void handleScroll(ScrollEvent se) {
        zTrans += se.getDeltaY() * 0.2;

        se.consume();
        updateCamera();
    }

    public void focus(MouseEvent e) {
        if (e.getClickCount() > 1) {
            PickResult res = e.getPickResult();
            if (res.getIntersectedNode() instanceof Shape3D) {
                Shape3D shape = (Shape3D) res.getIntersectedNode();
                focusX = shape.getTranslateX() + res.getIntersectedPoint().getX();
                focusY = shape.getTranslateZ() + res.getIntersectedPoint().getZ();
                gameShell.field.debugOut("Focus: " + focusX + ", " + focusY);
            } else {
                focusX = 0;
                focusY = 0;
            }

            zTrans *= 0.7;

            updateCamera();
        }
        //e.consume();
    }

    public void createStar(int x, int y, String name, int index, double energy) { //[Q]

        Star3D star = new Star3D(gameShell, x, y, name, index, energy);

        this.root.getChildren().add(star.s);

        this.stars.put(index, star);
    }

    public void createPlanet(int x, int y, String name, int index, double energy, int tech, Trajectory t) { //[Q]

        Planet3D planet = new Planet3D(gameShell, x, y, name, index, energy, t);

        //this.root.getChildren().add(planet.s);
        root.getChildren().add(planet);
        this.planets.put(index, planet);
    }

    public void createAlien(AlienSpec as, int id, int x, int y, AlienShapeFactory asf, Trajectory t) { //[Q]
        Alien3D alien = new Alien3D(gameShell, as, id, x, y, asf);
        if (t != null) {
            alien.buildTrajectory(t, Color.RED);
        }
        this.aliens.put(as.hashCode, alien);

        // alien is added with isNew == true, this leads to update() adding it to the visuals
        this.updateQueue.add(alien);
    }

    public void destroyAlien(AlienSpec as, int id, int x, int y) { //[Q]
        Alien3D alien = this.aliens.get(as.hashCode);
        assert (alien != null);

        alien.killMe = true;

        // remove it later on the UI thread
        this.updateQueue.add(alien);
    }

    public static double xFromX(double x) { //[Q]
        return (x) * spacing;
    }

    public static double yFromIndex(int i) { //[Q]
        return -i;
    }

    public static double zFromY(double y) { //[Q]
        return (y) * spacing;
    }

    private Group buildAxes() {
        Group g = new Group();

        int scale = 10;
        int w = gameShell.fieldGrid.width / 10 * scale + 1;
        int h = gameShell.fieldGrid.height / 10 * scale + 1;

        Box b = new Box(gameShell.fieldGrid.width, 0.01, 0.01);
        PhongMaterial plateMaterial = new PhongMaterial(Color.BLUE);
        b.setMaterial(plateMaterial);
        g.getChildren().add(b);
        return g;
    }

    void update() { //[Q]
        for (Planet3D p : planets.values()) {
            p.updatePosition();
        }

        for (Alien3D a : updateQueue) {
            if (a.killMe) {
                aliens.remove(a.as.hashCode);
                gameShell.fieldGrid.getCell(a.x, a.y).removeAlien(a);
                root.getChildren().remove(a);
            } else {
                a.updatePosition();

                Cell cell = gameShell.fieldGrid.getCell(a.x, a.y);
                if (cell.totalFighters > 0) {
                    displayFight(a.x, a.y);
                    cell.totalFighters = 0;
                }
            }
        }

        updateQueue.clear();
    }

    void updatePlanetsAndStars() { //[Q]
        for (Star3D s : stars.values()) {
            s.forceUpdatePosition();
        }
        for (Planet3D p : planets.values()) {
            p.forceUpdatePosition();
        }
    }

    public void displayFight(int x, int y) {
        Cell cell = gameShell.fieldGrid.getCell(x, y);
        int fighting = cell.totalFighters;

        Sphere s = new Sphere(fighting / (double) 2);
        s.setMaterial(new PhongMaterial(Color.rgb(255, 0, 0, 0.2)));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(xFromX(x));
        s.setTranslateY(-fighting + 1);
        s.setTranslateZ(zFromY(y));
        s.setOpacity(0.1);

        // make it glow
        Glow glow = new Glow(1.0);
        glow.setLevel(1.0);
        s.setEffect(glow);

        // make it shrink over 2 seconds
        ScaleTransition t = new ScaleTransition(Duration.millis(500), s);
        t.setFromX(1);
        t.setFromY(1);
        t.setFromZ(1);

        t.setToX(0);
        t.setToY(0);
        t.setToZ(0);

        // kill it when done
        t.setOnFinished((e) -> this.root.getChildren().remove(s));

        // add to scene and play
        t.play();
        this.root.getChildren().add(s);
    }

    private Box buildPlate() {

        int scale = 5;
        int w = gameShell.fieldGrid.width / 10 * scale + 1;
        int h = gameShell.fieldGrid.height / 10 * scale + 1;
        WritableImage gridImg = new WritableImage(w, h);
        PixelWriter writer = gridImg.getPixelWriter();

        // mostly DARKRED
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                writer.setColor(x, y, Color.DARKRED);
            }
        }

        for (int x = 0; x <= w; x += scale) {
            for (int y = 0; y < h; y++) {
                writer.setColor(x, y / 10, x == w / 2 ? Color.RED : Color.BLACK);
            }
        }

        for (int y = 0; y <= h; y += scale) {
            for (int x = 0; x < w; x++) {
                writer.setColor(x, y, y == h / 2 ? Color.BLUE : Color.BLACK);
            }
        }

        Box b = new Box(gameShell.fieldGrid.width, 0.01, gameShell.fieldGrid.height);
        PhongMaterial plateMaterial = new PhongMaterial();
        plateMaterial.setDiffuseMap(gridImg);
        b.setMaterial(plateMaterial);
//        b.setTranslateY(1.1);

        return b;
    }
}
