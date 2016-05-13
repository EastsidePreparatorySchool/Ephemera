package SpaceCritters;

import alieninterfaces.AlienShapeFactory;
import gameengineinterfaces.AlienSpec;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;

/**
 *
 * @author gmein
 */
public class Alien3D {

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
    Shape3D alien;

    public Alien3D(SpaceCritters gameShellInstance, AlienSpec as, int id, int x, int y,
            AlienShapeFactory asf) {
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
            alien = asf.getShape();
        }

        if (alien == null) {
            alien = new Box(0.5, 0.5, 0.5);
        }
        alien.setMaterial(new PhongMaterial(gameShell.field.speciesSet.getColor(as.speciesName, as.speciesID)));
        alien.setDrawMode(DrawMode.FILL);

        alien.setTranslateX(gameShell.mainScene.xFromX(x));
        alien.setTranslateY(gameShell.mainScene.yFromIndex(zPos));
        alien.setTranslateZ(gameShell.mainScene.zFromY(y));
    }

    void updatePosition() {
        Cell cell;

        if (nextX != x || nextY != y || nextZ != zPos) {
            if (nextX != x || nextY != y) { // if we are changing cells
                if (!isNew) {
                    cell = gameShell.field.getCell(x, y);
                    cell.removeAlien(this);
                }

                x = nextX;
                y = nextY;

                cell = gameShell.field.getCell(x, y);
                cell.addAlien(this);

                if (isNew) {
                    gameShell.mainScene.root.getChildren().add(this.alien);
                    isNew = false;
                }
            } else {// we are only changing height
                zPos = nextZ;
            }

            alien.setTranslateX(gameShell.mainScene.xFromX(x));
            alien.setTranslateY(gameShell.mainScene.yFromIndex(zPos));
            alien.setTranslateZ(gameShell.mainScene.zFromY(y));

        }
    }

    public void recordMoveTo(int x, int y) {
        this.nextX = x;
        this.nextY = y;

        gameShell.mainScene.updateQueue.add(this);
    }

}
