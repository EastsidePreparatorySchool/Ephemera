/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import gameengineinterfaces.AlienSpec;
import java.util.HashMap;
import java.util.HashSet;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

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
    final HashSet<Alien3D> updateSet;

    // camera
    final private PerspectiveCamera camera;
    final private PointLight pLight;
    private double focusX;
    private double focusY;
    private double xRot;
    private double yRot;
    private double zTrans;

    // object positioning
    double spacing;
    double objectElevation;

    public Scene3D(SpaceCritters gameShellInstance, int widthPX, int heightPX) {
        this.gameShell = gameShellInstance;
        this.stars = new HashMap(50);
        this.planets = new HashMap(50);
        this.aliens = new HashMap(100000);
        this.updateSet = new HashSet(100000);

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

        AmbientLight aLight = new AmbientLight(Color.rgb(50, 50, 50));
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
        camera.setNearClip(5);
        camera.setFarClip(1500);
        root.getChildren().add(camera);

        // Use a SubScene       
        SubScene subScene = new SubScene(root, pxX, pxY, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        outer = new HBox(subScene);
        subScene.widthProperty().bind(outer.widthProperty());
        subScene.heightProperty().bind(outer.heightProperty());
        subScene.setOnMouseClicked((e) -> focus(e));

        outer.setPrefSize(pxX, pxY);
        outer.setMaxSize(pxX, pxY);
        outer.setMinSize(200, 200);
        outer.setAlignment(Pos.TOP_LEFT);
        root.setAutoSizeChildren(true);

    }

    public void createStar(int x, int y, String name, int index, double energy) {

        Star3D star = new Star3D(gameShell, x, y, name, index, energy);

        this.root.getChildren().add(star.s);
        this.stars.put(index, star);
    }

    public void createPlanet(int x, int y, String name, int index, double energy, int tech) {

        Planet3D planet = new Planet3D(gameShell, x, y, name, index, energy);

        this.root.getChildren().add(planet.s);
        this.planets.put(index, planet);
    }

    public void createAlien(AlienSpec as, int id, int x, int y) {
        Alien3D alien = new Alien3D(gameShell, as, id, x, y);
        this.aliens.put(as.hashCode, alien);

        // alien is added with isNew == true, this leads to update() adding it to the visuals
        this.updateSet.add(alien);
    }

    public void destroyAlien(AlienSpec as, int id, int x, int y) {
        Alien3D alien = this.aliens.get(as.hashCode);
        assert (alien != null);

        alien.killMe = true;

        // remove it later on the UI thread
        this.updateSet.add(alien);
    }

    double xFromX(int x) {
        return (x) * spacing;
    }

    double yFromIndex(int i) {
        return -i;
    }

    double zFromY(int y) {
        return (y) * spacing;
    }

    private Group buildAxes() {
        Cylinder axis;
        Group root = new Group();

        double thickness = 0.07;

        final PhongMaterial grayMaterial = new PhongMaterial(Color.rgb(24, 24, 24, 1.0));
        final PhongMaterial greenMaterial = new PhongMaterial(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial(Color.BLUE);
        final PhongMaterial redMaterial = new PhongMaterial(Color.RED);

        for (int x = -gameShell.field.width / 2; x <= gameShell.field.width / 2; x++) {
            axis = new Cylinder(thickness, gameShell.field.width * spacing);
            axis.setRotationAxis(new Point3D(0.0, 0.0, 1.0));
            axis.setRotate(90);
            axis.setTranslateZ(x);
            axis.setTranslateY(1);
            axis.setMaterial(x == 0 ? redMaterial : grayMaterial);
            root.getChildren().add(axis);

        }
        for (int y = -gameShell.field.height / 2; y <= gameShell.field.height / 2; y++) {
            axis = new Cylinder(thickness, gameShell.field.height * spacing);
            axis.setRotationAxis(new Point3D(1.0, 0.0, 0.0));
            axis.setRotate(90);
            axis.setTranslateX(y);
            axis.setTranslateY(1);
            axis.setMaterial(y == 0 ? blueMaterial : grayMaterial);
            root.getChildren().add(axis);
        }

        axis = new Cylinder(thickness, 100 * spacing);
        axis.setMaterial(greenMaterial);
        root.getChildren().addAll(axis);

        axis = new Cylinder(thickness, gameShell.field.height * spacing);
        axis.setRotationAxis(new Point3D(1.0, 0.0, 0.0));
        axis.setRotate(90);
        axis.setTranslateX(0);
        axis.setTranslateY(1);
        axis.setMaterial(blueMaterial);
        root.getChildren().add(axis);

        axis = new Cylinder(thickness, gameShell.field.width * spacing);
        axis.setRotationAxis(new Point3D(0.0, 0.0, 1.0));
        axis.setRotate(90);
        axis.setTranslateZ(0);
        axis.setTranslateY(1);
        axis.setMaterial(redMaterial);
        root.getChildren().add(axis);

        // plate to alwasy have someting to click on
        Box plate = new Box(gameShell.field.width * spacing, 0, gameShell.field.height * spacing);
        plate.setMaterial(new PhongMaterial(Color.DARKRED));
        plate.setTranslateY(1 + 2 * thickness);
        root.getChildren().add(plate);

        return root;
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

            camera.getTransforms().setAll(
                    new Translate(focusX, 0, focusY),
                    new Rotate(yRot, Rotate.Y_AXIS),
                    new Rotate(xRot, Rotate.X_AXIS),
                    new Translate(0, 0, zTrans)
            );
        }
        e.consume();
    }

    public void controlCamera(KeyEvent e) {

        switch (e.getCode()) {
            case ESCAPE:
                this.xRot = -20;
                this.yRot = -10;
                this.zTrans = -530;
                this.spacing = 1.0;
                this.objectElevation = 0;
                this.focusX = 0.0;
                this.focusY = -80.0;
                break;
            case PLUS:
            case EQUALS:
                if (zTrans < 1000) {
                    zTrans += 10;
                }
                break;
            case MINUS:
                if (zTrans > -1000) {
                    zTrans -= 10;
                }
                break;
            case RIGHT:
                yRot -= 5;
                break;
            case LEFT:
                yRot += 5;
                break;
            case DOWN:
                xRot += 2;
                break;
            case UP:
                xRot -= 2;
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

            case G:
                if (gridVisible) {
                    root.getChildren().removeAll(grid);
                } else {
                    root.getChildren().addAll(grid);
                }
                gridVisible = !gridVisible;
                break;
            default:
                return; // do not consumer this event if you can't handle it
        }

        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );
        pLight.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );

        e.consume();
    }

    void update() {
        for (Planet3D p : planets.values()) {
            p.updatePosition();
        }

        for (Alien3D a : updateSet) {
            if (a.killMe) {
                this.aliens.remove(a);
                this.gameShell.field.getCell(a.x, a.y).removeAlien(a);
                this.root.getChildren().remove(a.alien);
            } else {
                a.updatePosition();
            }
        }

        updateSet.clear();
    }
    
    void updatePlanetsAndStars() {
        for(Star3D s : stars.values()) {
            s.forceUpdatePosition();
        }
        for (Planet3D p : planets.values()) {
            p.forceUpdatePosition();
        }
    }

}

/*
                int fighting = cell.fightCountNoDecrement();
                if (fighting > 0) {
                    y = cell.totalFighters;

                    Sphere s = new Sphere(fighting / (double) 2);
                    s.setMaterial(new PhongMaterial(Color.RED));
                    s.setDrawMode(DrawMode.FILL);
                    s.setTranslateX(xFromIndex(i));
                    s.setTranslateY(yFromIndex(y) - fighting);
                    s.setTranslateZ(zFromIndex(j));
                    s.setOpacity(fighting / (double) 10);
                    objects.add(s);

                    Box box = new Box(0.7, y, 0.7);
                    box.setMaterial(new PhongMaterial(Color.RED));
                    box.setDrawMode(DrawMode.FILL);
                    box.setTranslateX(xFromIndex(i));
                    box.setTranslateY(yFromIndex(y) - (yFromIndex(y) / 2) + 0.5);
                    box.setTranslateZ(zFromIndex(j));
                    box.setOpacity(0.05);
                    objects.add(box);

  
 */
