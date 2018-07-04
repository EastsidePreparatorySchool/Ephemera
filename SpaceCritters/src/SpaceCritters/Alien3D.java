/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import alieninterfaces.AlienShapeFactory;
import gameengineinterfaces.AlienSpec;
import gamelogic.Constants;
import java.util.LinkedList;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

/**
 *
 * @author gmein
 */
public class Alien3D extends OrbitGroup{

    int x;
    int y;
    int nextX;
    int nextY;
    int zPos;
    int nextZ;
    boolean isNew;
    boolean killMe;

    final int id;
    final AlienSpec as;
    final SpaceCritters gameShell;
    private Shape3D alien;
    private LinkedList<Transform> intrinsicTransforms;

    public Alien3D(SpaceCritters gameShellInstance, AlienSpec as, int id, int x, int y, AlienShapeFactory asf) { //[Q]
        this.gameShell = gameShellInstance;
        this.nextX = x;
        this.nextY = y;
        this.x = Integer.MAX_VALUE;
        this.y = Integer.MAX_VALUE;
        this.zPos = 0;
        this.nextZ = 0;
        this.id = id;
        this.as = as;
        this.isNew = true;
        this.killMe = false;
        this.alien = null;

        if (asf != null) {
            alien = asf.getShape(Constants.shapeComplexityLimit);
        }

        if (alien == null) {
            alien = new Box(0.5, 0.5, 0.5);
        }

        this.intrinsicTransforms = new LinkedList();
        for (Transform t : alien.getTransforms()) {
            this.intrinsicTransforms.add(t);
        }

        alien.setMaterial(new PhongMaterial(gameShell.fieldGrid.speciesSet.getColor(as.speciesName, as.speciesID)));
        alien.setDrawMode(DrawMode.FILL);
        
        getChildren().add(alien);
    } 

    void updatePosition() { //[Q]
        Cell cell;

        if (nextX != x || nextY != y || nextZ != zPos) {
            if (nextX != x || nextY != y) { // if we are changing cells
                if (!isNew) {
                    cell = gameShell.fieldGrid.getCell(x, y);
                    cell.removeAlien(this);
                }

                x = nextX;
                y = nextY;

                cell = gameShell.fieldGrid.getCell(x, y);
                cell.addAlien(this);

                if (isNew) {
                    gameShell.mainScene.root.getChildren().add(this);
                    isNew = false;
                }
            } else {// we are only changing height
                zPos = nextZ;
            }
            alien.getTransforms().clear();
            alien.getTransforms().add(new Translate(
                    Scene3D.xFromX(x),
                    Scene3D.yFromIndex(zPos),
                    Scene3D.zFromY(y)));
            alien.getTransforms().addAll(intrinsicTransforms);
        }
    }

    public void recordMoveTo(int x, int y) { //[Q]
        this.nextX = x;
        this.nextY = y;

        gameShell.mainScene.updateQueue.add(this);
    }

}
