/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import static gamelogic.GridCircle.isValidX;
import static gamelogic.GridCircle.isValidY;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author gmein
 *
 * This replaces "ViewImplementation"
 */
public class RadarImplementation implements Radar {

    private ArrayList<SpaceObject> sOs;
    private AlienGrid ag;
    private int centerX;
    private int centerY;
    private int size;

    public RadarImplementation(AlienGrid ag) {
        this.ag = ag;
        this.sOs = new ArrayList<>();
        this.centerX = 0;
        this.centerY = 0;
        this.size = 0;
    }

    public RadarImplementation(AlienGrid ag, ArrayList<SpaceObject> sOs, int centerX, int centerY, int size) {
        this.ag = ag;
        this.sOs = sOs;
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
    }

    public ArrayList<AlienSpecies> getAliensAtPos(int x, int y) throws CantSeeSquareException {
        x = checkPosX(x);
        y = checkPosY(y);

        ArrayList<AlienSpecies> as = new ArrayList();
        AlienCell acs = ag.getAliensAt(x, y);
        if (ag != null) {
            for (AlienContainer ac : ag.acGrid[x][y]) {
                as.add(ac.getAlienSpecies());
            }
        }
        return as;
    }

    public ArrayList<AlienSpecies> getAliensInView() {
        ArrayList<AlienSpecies> as = new ArrayList();

        for (int d = 1; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        as.add(ac.getAlienSpecies());
                    }
                }
            }
        }

        return as;
    }

    public SpaceObjectSpec getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException {
        return null;
    }

    public ArrayList<SpaceObjectSpec> getSpaceObjectsInView() {
        return null;
    }

    public AlienSpecies getClosestAlienToPos(int x, int y) throws CantSeeSquareException {
        return null;
    }

    public AlienSpecies getClosestSpecificAlienToPos(AlienSpecies as, int x, int y) throws CantSeeSquareException {
        return null;
    }

    public AlienSpecies getClosestXenoToPos(AlienSpecies as, int x, int y) throws CantSeeSquareException {
        return null;
    }

    // helpers
    public int checkPosX(int x) throws CantSeeSquareException {
        if (!isValidX(x)) {
            throw new CantSeeSquareException();
        }
        return x;
    }

    public int checkPosY(int y) throws CantSeeSquareException {
        if (isValidY(y)) {
            throw new CantSeeSquareException();
        }
        return y;
    }

}
