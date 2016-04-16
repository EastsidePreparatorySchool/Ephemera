/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import java.util.ArrayList;

/**
 *
 * @author gmein
 *
 * This replaces "ViewImplementation"
 */
public class RadarImplementation implements Radar {

    private ArrayList<SpaceObject> sOs;
    private AlienGrid ag;
    private int x;
    private int y;
    private int size;

    public RadarImplementation(AlienGrid ag) {
        this.ag = ag;
        this.sOs = new ArrayList<>();
        this.x = 0;
        this.y = 0;
        this.size = 0;
    }

    public RadarImplementation(AlienGrid ag, ArrayList<SpaceObject> sOs, int x, int y, int size) {
        this.ag = ag;
        this.sOs = sOs;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public int checkPosX(int x) throws CantSeeSquareException {
        if (x < this.x - size
                || x > this.x + size) {
            throw new CantSeeSquareException();
        }
        if (x < 0) {
            return 0;
        }
        if (x > ag.acGrid.length - 1) {
            return ag.acGrid.length - 1;
        }
        return x;
    }

    public int checkPosY(int y) throws CantSeeSquareException {
        if (y < this.y - size
                || y > this.y + size) {
            throw new CantSeeSquareException();
        }
        if (y < 0) {
            return 0;
        }
        if (y > ag.acGrid[0].length - 1) {
            return ag.acGrid[0].length - 1;
        }
        return y;
    }

    private int getPointDistanceProxy(int x1, int y1, int x2, int y2) {
        // Square roots are expensive, and we only need to determine
        // which of two distances is the greatest, so we use the distance
        // formula but don't square root it because we can simply compare
        // the sum of squares of distances instead

        int distance = (int) (x1 - x2) * (x1 - x2);
        return distance + (int) (y1 - y2) * (y1 - y2) ;
    }

    public ArrayList<AlienSpec> getAliensAtPos(int x, int y) throws CantSeeSquareException {
        ArrayList<AlienSpec> as = new ArrayList();
        for (AlienContainer ac : ag.acGrid[x][y]) {
            as.add(new AlienSpec(null, ac.alienPackageName, ac.alienClassName));
        }
        return as;
    }

    public ArrayList<AlienSpec> getAliensInView() throws CantSeeSquareException {
        ArrayList<AlienSpec> as = new ArrayList();
        int x1 = checkPosX(this.x - size);
        int x2 = checkPosX(this.x + size);
        int y1 = checkPosY(this.y - size);
        int y2 = checkPosY(this.y + size);

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (AlienContainer ac : ag.acGrid[x][y]) {
                    as.add(new AlienSpec(null, ac.alienPackageName, ac.alienClassName, ac.alien.hashCode(), ac.x, ac.y));
                }
            }
        }
        return as;
    }

    public SpaceObjectSpec getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException {
        return null;
    }

    public ArrayList<SpaceObjectSpec> getSpaceObjectsInView() throws CantSeeSquareException {
        return null;
    }

    public AlienSpec getClosestAlienToPos(int x, int y) {
        return null;
    }

    public AlienSpec getClosestSpecificAlienToPos(AlienSpec as, int x, int y) {
        return null;
    }

    public AlienSpec getClosestXenoToPos(AlienSpec as, int x, int y) {
        return null;
    }

}
