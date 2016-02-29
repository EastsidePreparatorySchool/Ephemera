/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consolealiensgame;

import java.util.List;
import alieninterfaces.*;

/**
 *
 * @author guberti
 */
public class ViewImplementation implements View {
    
    private final List<AlienContainer> aCs;
    private final int bottomX;
    private final int bottomY;
    private final int size;
    
    
    public ViewImplementation (List<AlienContainer> aCs, int bottomX, int bottomY, int size) {
        this.aCs = aCs;
        this.bottomX = bottomX;
        this.bottomY = bottomY;
        this.size = size;
    }
    
    @Override
    public int getEnergyAtPos(int x, int y) throws CantSeeSquareException {
        if (bottomX + 2 * size < x ||
                bottomX > x ||
                bottomY + 2 * size < y ||
                bottomY > y) {
            throw new CantSeeSquareException();
        }
        for (AlienContainer aC : aCs) {
            if (aC.x == x && aC.y == y) {
                return aC.energy;
            }
        }
        return -1;
    }
    
    @Override
    public boolean isAlienAtPos(int x, int y) throws CantSeeSquareException {
        return getEnergyAtPos(x, y) > -1;
    }
    
    @Override
    public int[] getClosestAlienPos(int x, int y) {
        if (aCs.isEmpty()) { // If there are no visible aliens
            //throw new NoVisibleAliensException();
            int [] invalid_pos = {Integer.MAX_VALUE, Integer.MAX_VALUE};
            return invalid_pos;
        }
        
        // Returns an array of two numbers corresponding to the x and y of the alien
        int alienX = aCs.get(0).x;
        int alienY = aCs.get(0).y;
        
        for (AlienContainer aC : aCs) {
            if (getPointDistance(x, y, aC.x, aC.y) < 
                    getPointDistance(x, y, alienX, alienY)) {
                alienX = aC.x;
                alienY = aC.y;
            }
        }
        
        int[] pos = {alienX, alienY};
        
        return pos;
    }
    
    private int getPointDistance(int x1, int y1, int x2, int y2) {
        // Square roots are expensive, and we only need to determine
        // which of two distances is the greatest, so we use the distance
        // formula but don't square root it because we can simply compare
        // the squares of distances instead
        
        int distance = (int) Math.pow(Math.abs(x1 - x2), 2);
        return distance + (int) Math.pow(Math.abs(y1 - y2), 2);
    }
}
