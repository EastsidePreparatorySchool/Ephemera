/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import alieninterfaces.AlienSpecies;
import gameengineinterfaces.AlienSpec;
import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author gunnar
 */
public class ZoomView {

    Cell[][] grid;

    public void render(Group root, int mincol, int minrow, int width, int height) {
        ArrayList<Node> objects = new ArrayList<>();
        for (int i = mincol; i < mincol + width; i++) {
            for (int j = minrow; j < minrow + height; j++) {
                Cell cell = grid[i][j];
                if (cell.alienCount > 0) {
                    int y = 0;
                    for (CellInfo ci : cell.speciesMap.values()) {
                        for (int k = 0; k < ci.count; k++) {
                            Box alien = new Box(0.3, 0.3, 0.3);
                            alien.setMaterial(new PhongMaterial(ci.color));
                            alien.setDrawMode(DrawMode.FILL);
                            alien.setTranslateX((i - mincol) * 1.0 - 10);
                            alien.setTranslateZ((j - minrow) * 1.0 + 10);
                            alien.setTranslateY((10-y) * 1.0 + 10);

                            objects.add(alien);
                            y++;
                        }

                    }
                }
            }
        }
        root.getChildren().setAll(objects);

    }

    public Parent createView(Cell[][] grid) throws Exception {

        this.grid = grid;
        // Box
        Sphere test2 = new Sphere(0.3);
        test2.setMaterial(new PhongMaterial(Color.RED));
        test2.setDrawMode(DrawMode.FILL);
        test2.setTranslateX(2);

        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
                new Rotate(-10, Rotate.Y_AXIS),
                new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -40));

        // Build the Scene Graph
        Group root = new Group();
        root.getChildren().add(camera);
        render(root, 240, 240, 20, 20);

        // Use a SubScene       
        SubScene subScene = new SubScene(root, 300, 300);
        subScene.setFill(Color.DARKBLUE);
        subScene.setCamera(camera);
        Group group = new Group();
        group.getChildren().add(subScene);

        return group;
    }

    public void open(Cell[][] grid) throws Exception {
        Stage primaryStage = new Stage(StageStyle.DECORATED);
        primaryStage.setResizable(false);
        Scene scene = new Scene(createView(grid));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
