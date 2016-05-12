/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;

/**
 *
 * @author gunnar
 */
public class Planet3D {

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

    public Planet3D(SpaceCritters gameShellInstance, int x, int y, String name, int index, double energy) {
        this.gameShell = gameShellInstance;
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.index = index;
        this.nextX = x;
        this.nextY = y;

        this.s = new Sphere(0.7);
        s.setMaterial(new PhongMaterial(Color.GREEN));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));
        this.s.setRotationAxis(new Point3D(0, -1, 0));

        if (name.equalsIgnoreCase("Earth")) {
            decorateEarth(s);
        } else if (name.equalsIgnoreCase("RMack")) {
            decorateRMack(s);
        }

    }

    public void recordMoveTo(int x, int y) {
        this.nextX = x;
        this.nextY = y;
    }

    public void updatePosition() {
        if (nextX != x || nextY != y) {
            forceUpdatePosition();
            this.s.setRotate(rotation);
            rotation += 10;
            if (rotation > 360) {
                rotation -= 360;
            }
        }
    }

    public void forceUpdatePosition() {
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
        dMap = new Image(SpaceCritters.class.getResourceAsStream("dMap_earth.jpg"));
        bMap = new Image(SpaceCritters.class.getResourceAsStream("bMap_earth.jpg"));
        sMap = new Image(SpaceCritters.class.getResourceAsStream("sMap_earth.jpg"));
        dMapMack = new Image(SpaceCritters.class.getResourceAsStream("dmap_rmack.jpg"));
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
