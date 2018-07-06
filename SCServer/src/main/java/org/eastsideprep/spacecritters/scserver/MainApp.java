package org.eastsideprep.spacecritters.scserver;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import org.eastsideprep.spacecritters.spacecritters.SpaceCritters;

public class MainApp extends Application {
    
    SpaceCritters sc;
    
    @Override
    public void start(Stage stage) throws Exception {
        sc = new SpaceCritters();
        
        try {
            sc.start(stage);
        } catch (Exception e) {
            System.err.println("scserver init: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", "org.eastsideprep.spacecritters.spacecritters.SplashScreenLoader");

        launch(args);
    }
    
}
