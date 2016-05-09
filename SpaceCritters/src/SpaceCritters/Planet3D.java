/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import javafx.scene.effect.Glow;
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

    public Planet3D(SpaceCritters gameShellInstance, int x, int y, String name, int index, double energy) {
        this.gameShell = gameShellInstance;
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.index = index;
        this.nextX = 0;
        this.nextY = 0;

        this.s = new Sphere(0.7);
        s.setMaterial(new PhongMaterial(Color.GREEN));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));
    }

    public void recordMoveTo(int x, int y) {
        this.nextX = x;
        this.nextY = y;
    }

    public void updatePosition() {
        if (nextX != x || nextY != y) {
            forceUpdatePosition();
        }
    }

    public void forceUpdatePosition() {
        x = nextX;
        y = nextY;
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));
    }
}
