package SpaceCritters;

import gameengineinterfaces.AlienSpec;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;

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
    boolean isNew;
    boolean killMe;

    final int id;
    final AlienSpec as;
    final SpaceCritters gameShell;
    final Box alien;

    public Alien3D(SpaceCritters gameShellInstance, AlienSpec as, int id, int x, int y) {
        this.gameShell = gameShellInstance;
        this.nextX = x;
        this.nextY = y;
        this.x = Integer.MAX_VALUE;
        this.y = Integer.MAX_VALUE;
        this.zPos = 0;
        this.id = id;
        this.as = as;
        this.isNew = true;
        this.killMe = false;

        alien = new Box(0.5, 0.5, 0.5);
        alien.setMaterial(new PhongMaterial(gameShell.field.speciesSet.getColor(as.speciesName, as.speciesID)));
        alien.setDrawMode(DrawMode.FILL);
        alien.setTranslateX(gameShell.mainScene.xFromX(x));
        alien.setTranslateY(gameShell.mainScene.yFromIndex(zPos));
        alien.setTranslateZ(gameShell.mainScene.zFromY(y));
    }

    void updatePosition() {
        Cell cell;

        if (nextX != x || nextY != y) {
            if (!isNew) {
                cell = gameShell.field.getCell(x, y);
                cell.aliens.remove(this);
            }

            x = nextX;
            y = nextY;

            alien.setTranslateX(gameShell.mainScene.xFromX(x));
            alien.setTranslateY(gameShell.mainScene.yFromIndex(zPos));
            alien.setTranslateZ(gameShell.mainScene.zFromY(y));

            cell = gameShell.field.getCell(x, y);
            cell.aliens.add(this);

            if (isNew) {
                this.zPos = cell.aliens.size() - 1;

                gameShell.mainScene.root.getChildren().add(this.alien);
                isNew = false;
            }
        }
    }

    public void recordMoveTo(int x, int y) {
        this.nextX = x;
        this.nextY = y;
        
        gameShell.mainScene.updateSet.add(this);
    }

}
