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
    double xRot;
    double yRot;
    double zTrans;
    double spacing;

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
        this.zTrans = -width;
        this.spacing = 1.0;
        this.heightPX = heightPX;
        pxX = width * 6;
        pxY = height * 6;

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
            s.setTranslateY(1);
            s.setTranslateZ(zFromIndex(star.y));
            s.setOpacity(0.5);
            group.getChildren().add(s);

            PointLight light = new PointLight(Color.ANTIQUEWHITE);
            light.setTranslateX(xFromIndex(star.x));
            light.setTranslateY(1);
            light.setTranslateZ(zFromIndex(star.y));
            group.getChildren().add(light);

        }

        // planets
        for (PlanetForDisplay planet : planets) {
            Sphere s = new Sphere(0.5);
            s.setMaterial(new PhongMaterial(Color.GREEN));
            s.setDrawMode(DrawMode.FILL);
            s.setTranslateX(xFromIndex(planet.x));
            s.setTranslateY(1);
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
                }
            }
        }
        group.getChildren().addAll(objects);

        AmbientLight a = new AmbientLight(Color.ANTIQUEWHITE);
        group.getChildren().add(a);

        root.getChildren().clear();
        buildAxes(root);
        root.getChildren().addAll(group);

        /*
        Box alien = new Box(1.0, 1.0, 1.0);
        alien.setMaterial(new PhongMaterial(Color.WHITE));
        alien.setDrawMode(DrawMode.FILL);
        alien.setTranslateX(0);
        alien.setTranslateY(0);
        alien.setTranslateZ(0);

        root.getChildren().add(alien);
         */
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

        // Box
        Sphere test2 = new Sphere(0.3);
        test2.setMaterial(new PhongMaterial(Color.RED));
        test2.setDrawMode(DrawMode.FILL);
        test2.setTranslateX(2);

        // Create and position camera
        camera = new PerspectiveCamera(true);
        camera.getTransforms().setAll(
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans));
        camera.setNearClip(5);
        camera.setFarClip(200);

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
        zStage = new Stage(StageStyle.DECORATED);
        zStage.setResizable(false);
        zStage.setAlwaysOnTop(true);

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
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(240.0, 0.05, 0.05);
        final Box yAxis = new Box(0.05, 240.0, 0.05);
        final Box zAxis = new Box(0.05, 0.05, 240.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        root.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    public void focus(MouseEvent e) {
        if (!this.zStage.isShowing()) {
            open();
        }

        this.mincol = (int) ((e.getX() - 1) / cellWidth) - (width / 2);
        this.minrow = (int) (((heightPX - e.getY() - 1) / cellHeight) - (height / 2));
    }

    public void controlCamera(KeyEvent e) {

        if (e.getCode() == KeyCode.ESCAPE) {
            this.xRot = -20;
            this.yRot = -10;
            this.zTrans = -width;
            this.spacing = 1.0;

        } else {
            switch (e.getText()) {
                case "f":
                    zTrans += 10;
                    break;
                case "b":
                    zTrans -= 10;
                    break;
                case "l":
                    yRot -= 5;
                    break;
                case "r":
                    yRot += 5;
                    break;
                case "u":
                    xRot += 5;
                    break;
                case "d":
                    xRot -= 5;
                    break;
                 case "w":
                    spacing *= 1.1;
                    break;
                case "n":
                    spacing /= 1.1;
                    break;
                default:
                    break;
            }
        }

        camera.getTransforms().setAll(
                new Rotate(yRot, Rotate.Y_AXIS),
                new Rotate(xRot, Rotate.X_AXIS),
                new Translate(0, 0, zTrans));

        e.consume();
    }

}
