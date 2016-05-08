/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.util.ArrayList;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
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
    final public HBox outer;
    final Group root;

    // master lists
    final private ArrayList<Star3D> stars;
    final private ArrayList<PlanetForDisplay> planets;

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
        this.stars = new ArrayList(50);
        this.planets = new ArrayList(50);

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
        buildAxes(root);
        Box box = new Box(10, 10, 10);
        box.setMaterial(new PhongMaterial(Color.RED));
        box.setDrawMode(DrawMode.FILL);
        root.getChildren().add(box);

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

        outer.setPrefSize(pxX, pxY);
        outer.setMaxSize(pxX, pxY);
        outer.setMinSize(200, 200);
        outer.setAlignment(Pos.TOP_LEFT);
        root.setAutoSizeChildren(true);

    }

    public void createStar(int x, int y, String name, int index, double energy) {

        Star3D star = new Star3D(gameShell, x, y, name, index, energy);

        this.root.getChildren().add(star.s);
        this.stars.add(star);
        assert this.stars.indexOf(star) == star.index;
    }

    public void addPlanet(PlanetForDisplay planet) {
        Sphere s = new Sphere(0.5);
        s.setMaterial(new PhongMaterial(Color.GREEN));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(xFromX(planet.x));
        s.setTranslateY(objectElevation);
        s.setTranslateZ(zFromY(planet.y));
        this.root.getChildren().add(s);

        this.planets.add(planet.index, planet); // add at same index as on engine side
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

    private void buildAxes(Group root) {
        Cylinder axis;
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
            axis.setMaterial(x == 0 ? redMaterial : grayMaterial);
            root.getChildren().add(axis);

        }
        for (int y = -gameShell.field.height / 2; y <= gameShell.field.height / 2; y++) {
            axis = new Cylinder(thickness, gameShell.field.height * spacing);
            axis.setRotationAxis(new Point3D(1.0, 0.0, 0.0));
            axis.setRotate(90);
            axis.setTranslateX(y);
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
        axis.setMaterial(blueMaterial);
        root.getChildren().add(axis);

        axis = new Cylinder(thickness, gameShell.field.width * spacing);
        axis.setRotationAxis(new Point3D(0.0, 0.0, 1.0));
        axis.setRotate(90);
        axis.setTranslateZ(0);
        axis.setMaterial(redMaterial);
        root.getChildren().add(axis);

    }

    public void focus(MouseEvent e) {
        focusX = 0;
        focusY = 0;

        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );

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
                xRot += 5;
                break;
            case UP:
                xRot -= 5;
                break;
            case W:
                if (spacing < 20) {
                    spacing *= 1.1;
                }
                break;
            case N:
                if (spacing > 0.5) {
                    spacing /= 1.1;
                }
                break;
            case PAGE_UP:
                if (objectElevation >= -20) {
                    objectElevation -= 1;
                }
                break;
            case PAGE_DOWN:
                objectElevation = 0;
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

}

/*
    //aliens
    ArrayList<Node> objects = new ArrayList<>();
    for (int i = mincol;
    i< mincol + width ;
    i

    
        ++) {
            for (int j = minrow; j < minrow + height; j++) {
            Cell cell = grid[i][j];
            if (cell.alienCount > 0) {
                int y = 0;
                if (cell.speciesMap != null) {
                    for (CellInfo ci : cell.speciesMap.values()) {
                        for (int k = 0; k < ci.count; k++) {
                            Box alien = new Box(0.5, 0.5, 0.5);
                            alien.setMaterial(new PhongMaterial(ci.color));
                            alien.setDrawMode(DrawMode.FILL);
                            alien.setTranslateX(xFromIndex(i));
                            alien.setTranslateY(yFromIndex(y));
                            alien.setTranslateZ(zFromIndex(j));

                            objects.add(alien);
                            y++;
                        }
                    }
                }
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

                }
            }
        }
    }

    }

    public void open() {
        zStage.setResizable(false);

        javafx.geometry.Rectangle2D sb;
        sb = Screen.getPrimary().getVisualBounds();

        zStage.setX(sb.getMaxX() - pxX);
        zStage.setY(sb.getMaxY() - pxY);

        Scene scene = new Scene(createView());
        zStage.setScene(scene);
        zStage.show();
        //scene.setOnMouseClicked((e) -> close());
        

    }


 */
