/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author gunnar
 */
public class Planet3D extends OrbitGroup {

    final private SpaceCritters gameShell;
    public int x;
    public int y;
    final public String name;
    final public double energy;
    final public int index;
    final public Sphere s;
    int nextX;
    int nextY;
    double rotation = 0;

    public Planet3D(SpaceCritters gameShellInstance, int x, int y, String name, int index, double energy, Trajectory t) { //[Q]
        this.gameShell = gameShellInstance;
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.index = index;
        this.nextX = x;
        this.nextY = y;

        s = new Sphere(0.7);
        s.setMaterial(new PhongMaterial(Color.GREEN));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));
        s.setRotationAxis(new Point3D(0, -1, 0));

        getChildren().add(s);

        if (t != null) {
            buildTrajectory(t, Color.WHITE);
        }
        if (name.equalsIgnoreCase("Earth")) {
            decorateEarth(s);
        } else if (name.equalsIgnoreCase("RMack")) {
            decorateRMack(s);
        }

    }

    public void recordMoveTo(int x, int y) { //[Q]
        this.nextX = x;
        this.nextY = y;
    }

    public void updatePosition() { //[Q]
        if (nextX != x || nextY != y) {
            forceUpdatePosition();
            this.s.setRotate(rotation);
            rotation += 10;
            if (rotation >= 360) {
                rotation -= 360;
            }
        }
    }

    public void forceUpdatePosition() { //[Q]
        x = nextX;
        y = nextY;
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));
    }

    private static Image dMap;
    private static Image bMap;
    private static Image sMap;
    private static Image dMapMack;

    public static void preLoadPlanetImages() {
        try {
            dMap = new Image(SpaceCritters.class.getResourceAsStream("/dMap_earth.jpg"));
            bMap = new Image(SpaceCritters.class.getResourceAsStream("/bMap_earth.jpg"));
            sMap = new Image(SpaceCritters.class.getResourceAsStream("/sMap_earth.jpg"));
            dMapMack = new Image(SpaceCritters.class.getResourceAsStream("/dmap_rmack.jpg"));
        } catch (Exception e) {
            System.err.println("preload planet images" + e.getMessage());
            e.printStackTrace(System.err);

        }
    }

    private void decorateEarth(Sphere s) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(dMap);
        material.setBumpMap(bMap);
        material.setSpecularMap(sMap);
        s.setMaterial(material);
    }

    private void decorateRMack(Sphere s) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(dMapMack);
        s.setMaterial(material);
    }
}