/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.spacecritters;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author gmein
 */
public class SplashScreenLoader extends Preloader {

    private Stage splashScreen;

    @Override
    public void start(Stage stage) throws Exception {
        splashScreen = stage;
        
        StackPane root = new StackPane();
        Image splash = new Image(SpaceCritters.class.getResourceAsStream("/BigSplash.jpg"));
        root.getChildren().add(new ImageView(splash));
        Scene scene = new Scene(root, splash.getWidth(), splash.getHeight());

        splashScreen.setScene(scene);
        splashScreen.show();
    }

  
    @Override
    public void handleApplicationNotification(PreloaderNotification notification) {
        if (notification instanceof StateChangeNotification) {
            splashScreen.hide();
        }
    }

}