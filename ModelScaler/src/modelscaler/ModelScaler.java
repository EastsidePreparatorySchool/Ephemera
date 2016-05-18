/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelscaler;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;

/**
 *
 * @author gunnar
 */
public class ModelScaler extends Application {

    Group root = new Group();
    Group controls = new Group();
    Group display = new Group();

    @Override
    public void start(Stage primaryStage) {
        
        display = createDisplay();

        HBox hb = new HBox();
        hb.getChildren().addAll(display, controls);

        root.getChildren().add(hb);
        
        Scene scene = prepareScene();
        

        primaryStage.setTitle("SpaceCritters Model Scaler 1.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    Group createDisplay() {
        StlMeshImporter imp = new StlMeshImporter();

        try {
            imp.read(new File("test.stl"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        TriangleMesh mesh = imp.getImport();
        imp.close();
        MeshView meshView = new MeshView(mesh);
        Group group = new Group(meshView);

        return group;
    }

    Scene prepareScene() {

        Scene scene = new Scene(root, 1024, 800, true);
        Camera camera = new PerspectiveCamera();
        scene.setCamera(camera);
        return scene;
    }

    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
