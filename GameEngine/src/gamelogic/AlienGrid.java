/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.util.ArrayList;

/**
 *
 * @author gmein
 */
public class AlienGrid extends ArrayList<AlienContainer> {

    ArrayList<AlienContainer>[][] acGrid;
    int centerX;
    int centerY;

    public AlienGrid(int width, int height) {
        acGrid = new ArrayList[width][height];
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    public boolean add(AlienContainer ac) {
        // add alien to grid as well as to master list
        acGrid[ac.x+centerX][ac.y+centerY].add(ac);
        return super.add(ac);
    }

    public AlienContainer remove(int i) {
        // remove alien from grid as well as from master list
        AlienContainer ac = super.remove(i);
        acGrid[ac.x + centerX][ac.y + centerY].remove(ac);
        return ac;
    }

    public void move(AlienContainer ac, int oldX, int oldY, int newX, int newY) {
        acGrid[oldX+centerX][oldY+centerY].remove(ac);
        acGrid[newX+centerX][newY+centerY].add(ac);
    }

    public ArrayList<AlienContainer> getAliensAt(int x, int y) {
        return acGrid[x+centerX][y+centerY];
    }
}
