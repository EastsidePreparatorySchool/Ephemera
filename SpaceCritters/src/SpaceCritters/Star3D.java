/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;

/**
 *
 * @author gunnar
 */
public class Star3D {

    final private SpaceCritters gameShell;
    final public int x;
    final public int y;
    final public String name;
    final public double energy;
    final public int index; // index in ArrayList of stars in both engine and visualizer
    final public Sphere s;
 

    public Star3D(SpaceCritters gameShellInstance, int x, int y, String name, int index, double energy) {
        this.gameShell = gameShellInstance;
        this.x = x;
        this.y = y;
        this.name = name;
        this.energy = energy;
        this.index = index;

        this.s = new Sphere(1.0);
        s.setMaterial(new PhongMaterial(Color.rgb(255, 255, 255, 0.9)));
        s.setDrawMode(DrawMode.FILL);
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));

    

    }

    public void forceUpdatePosition() {
        s.setTranslateX(gameShell.mainScene.xFromX(x));
        s.setTranslateY(gameShell.mainScene.objectElevation);
        s.setTranslateZ(gameShell.mainScene.zFromY(y));

      

    }

}
