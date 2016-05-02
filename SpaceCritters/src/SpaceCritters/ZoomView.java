/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.util.ArrayList;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author gunnar
 */
public class ZoomView {

    Stage zStage;
    Group root;

    Cell[][] grid;
    public ArrayList<StarForDisplay> stars;
    public ArrayList<PlanetForDisplay> planets;

    int mincol;
    int minrow;
    int width;
    int height;
    int cellWidth;
    int cellHeight;
    int heightPX;
    int pxX;
    int pxY;
    PerspectiveCamera camera;
    double focusX;
    double focusY;
    double xRot;
    double yRot;
    double zTrans;
    double spacing;
    double objectElevation;

    public ZoomView(VisualizationGrid field, int mincol, int minrow, int width, int height, int cellWidth, int cellHeight, int heightPX) {
        this.grid = field.grid;
        this.stars = field.stars;
        this.planets = field.planets;
        this.mincol = mincol;
        this.minrow = minrow;
        this.width = width;
        this.height = height;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.xRot = -20;
        this.yRot = -10;
        this.zTrans = -(Math.min(width, 100));
        this.spacing = 1.0;
        this.heightPX = heightPX;
        pxX = Math.min(width * 6, 600);
        pxY = Math.min(height * 6, 600);
        focusX = 0.0;
        focusY = 0.0;
        zStage = new Stage(StageStyle.DECORATED);
       

    }

    public void render() {
        if (zStage == null || !zStage.isShowing()) {
            return;
        }

        Group group = new Group();

        // stars
        for (StarForDisplay star : stars) {
            Sphere s = new Sphere(1.0);
            s.setMaterial(new PhongMaterial(Color.ANTIQUEWHITE));
            s.setDrawMode(DrawMode.FILL);
            s.setTranslateX(xFromIndex(star.x));
            s.setTranslateY(objectElevation);
            s.setTranslateZ(zFromIndex(star.y));
            s.setOpacity(0.5);
            group.getChildren().add(s);

            PointLight light = new PointLight(Color.ANTIQUEWHITE);
            light.setTranslateX(xFromIndex(star.x));
            light.setTranslateY(objectElevation);
            light.setTranslateZ(zFromIndex(star.y));
            group.getChildren().add(light);

        }

        // planets
        for (PlanetForDisplay planet : planets) {
            Sphere s = new Sphere(0.5);
            s.setMaterial(new PhongMaterial(Color.GREEN));
            s.setDrawMode(DrawMode.FILL);
            s.setTranslateX(xFromIndex(planet.x));
            s.setTranslateY(objectElevation);
            s.setTranslateZ(zFromIndex(planet.y));

            group.getChildren().add(s);

        }

        //aliens
        ArrayList<Node> objects = new ArrayList<>();
        for (int i = mincol; i < mincol + width; i++) {
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
        group.getChildren().addAll(objects);

        AmbientLight a = new AmbientLight(Color.ANTIQUEWHITE);
        group.getChildren().add(a);

        root.getChildren().clear();
        buildAxes(root);
        root.getChildren().addAll(group);

       
    }

    private double xFromIndex(int i) {
        return (i - mincol - width / 2) * spacing;
    }

    private double yFromIndex(int i) {
        return -i;
    }

    private double zFromIndex(int i) {
        return (i - minrow - height / 2) * spacing;
    }

    public Parent createView() {

     
        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, Math.min(zTrans, 100))
        );
        camera.setNearClip(5);
        camera.setFarClip(1500);

        // Build the Scene Graph
        root = new Group();
        root.getChildren().add(camera);
        render();

        // Use a SubScene       
        SubScene subScene = new SubScene(root, pxX, pxY);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);
        Group group = new Group();
        group.getChildren().add(subScene);

        return group;
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
        scene.setOnKeyPressed((e) -> controlCamera(e));
        
    }

    public void close() {
        zStage.close();
    }

    private void buildAxes(Group root) {
        Box axis;
        final PhongMaterial grayMaterial = new PhongMaterial(Color.rgb(24, 24, 24, 1.0));
        final PhongMaterial greenMaterial = new PhongMaterial(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial(Color.BLUE);
        final PhongMaterial redMaterial = new PhongMaterial(Color.RED);

        for (int x = -width / 2; x <= width / 2; x++) {
            axis = new Box(width*spacing, 0.05, 0.05);
            axis.setTranslateZ(x);
            axis.setMaterial(grayMaterial);
            root.getChildren().add(axis);

        }
        for (int y = -height / 2; y <= height / 2; y++) {
            axis = new Box(0.05, 0.05, height*spacing);
            axis.setTranslateX(y);
            axis.setMaterial(grayMaterial);
            root.getChildren().add(axis);
        }
        
        axis = new Box(0.06, 100*spacing, 0.06);
        axis.setMaterial(greenMaterial);
        root.getChildren().addAll(axis);

        axis = new Box(height * spacing,0.06, 0.06);
        axis.setMaterial(redMaterial);
        root.getChildren().addAll(axis);

        axis = new Box(0.06, 0.06, height*spacing);
        axis.setMaterial(blueMaterial);
        root.getChildren().addAll(axis);
    }

    //public void focusZoomOn (int col, int row);
    public void focus(MouseEvent e) {
        if (!this.zStage.isShowing()) {
            open();
        }
        
        zStage.toFront();

        focusX = ((e.getX() - 1) / cellWidth) - (width / 2);
        focusY = (((heightPX - e.getY() - 1) / cellHeight) - (height / 2));
        //this.mincol = (int) ((e.getX() - 1) / cellWidth) - (width / 2);
        //this.minrow = (int) (((heightPX - e.getY() - 1) / cellHeight) - (height / 2));

        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );

        e.consume();
    }

    public void controlCamera(KeyEvent e) {

        if (e.getCode() == KeyCode.ESCAPE) {
            this.xRot = -20;
            this.yRot = -10;
            this.zTrans = -(Math.min(width, 100));
            this.spacing = 1.0;
            this.objectElevation = 0;

        } else {
            switch (e.getText()) {
                case "f":
                    if (zTrans < 1000) {
                        zTrans += 10;
                    }
                    break;
                case "b":
                    if (zTrans > -1000) {
                        zTrans -= 10;
                    }
                    break;
                case "r":
                    yRot -= 5;
                    break;
                case "l":
                    yRot += 5;
                    break;
                case "d":
                    xRot += 5;
                    break;
                case "u":
                    xRot -= 5;
                    break;
                case "w":
                    if (spacing < 20) {
                        spacing *= 1.1;
                    }
                    break;
                case "n":
                    if (spacing > 0.5) {
                        spacing /= 1.1;
                    }
                    break;
                case "e":
                    if (objectElevation >= -20) {
                        objectElevation -= 1;
                    }
                    break;
                case "0":
                    objectElevation = 0;
                    break;
                default:
                    break;
            }
        }

        camera.getTransforms().setAll(
                new Translate(focusX, 0, focusY),
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans)
        );

        e.consume();
    }

}
