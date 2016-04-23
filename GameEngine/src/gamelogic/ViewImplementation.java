/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.util.List;
import alieninterfaces.*;
import java.util.ArrayList;

/**
 *
 * @author guberti
 */
public class ViewImplementation implements View {

    private final AlienContainer ac;
    private final List<AlienContainer> aCs;
    private final List<SpaceObject> sOs;
    private final int bottomX;
    private final int bottomY;
    private final int size;
    public boolean fullyInstantiated;

    public ViewImplementation(AlienContainer ac) {
        this.ac = ac;
        this.aCs = null;
        this.sOs = null;
        this.bottomX = 0;
        this.bottomY = 0;
        this.size = (int)ac.tech;
        fullyInstantiated = false;
    }

    public ViewImplementation(AlienContainer ac, List<AlienContainer> aCs, List<SpaceObject> sOs, int bottomX, int bottomY, int size) {
        this.ac = ac;
        this.aCs = aCs;
        this.sOs = sOs;
        this.bottomX = bottomX;
        this.bottomY = bottomY;
        this.size = size;
        this.fullyInstantiated = true;
    }

    @Override
    public int getEnergyAtPos(int x, int y) throws CantSeeSquareException {
        return 0;
    }

    @Override
    public int getAlienCountAtPos(int x, int y) throws CantSeeSquareException {
        if (((x + ac.grid.aliens.centerX) >= ac.grid.width)
                || ((x + ac.grid.aliens.centerX) < 0)
                || ((y + ac.grid.aliens.centerY) >= ac.grid.height)
                || ((y + ac.grid.aliens.centerY) < 0)) {
            return 0;
        }

        if (ac.grid.aliens.isEmptyAt(x, y)) {
            return 0;
        }

        return ac.grid.aliens.acGrid[x + ac.grid.aliens.centerX][y + ac.grid.aliens.centerY].size()
                - ((ac.x == x && ac.y == y) ? 1 : 0);
    }

    @Override
    public int[] getClosestAlienPos(int x, int y) {
        int[] pos = {Integer.MAX_VALUE, Integer.MAX_VALUE};

        if (fullyInstantiated) {
            if (aCs.isEmpty()) { // If there are no visible aliens
                //throw new NoVisibleAliensException();
                int[] invalid_pos = {Integer.MAX_VALUE, Integer.MAX_VALUE};
                return invalid_pos;
            }

            // Returns an array of two numbers corresponding to the x and y of the alien
            int alienX = aCs.get(0).x;
            int alienY = aCs.get(0).y;

            for (AlienContainer aC : aCs) {
                if (getPointDistanceSquare(x, y, aC.x, aC.y)
                        < getPointDistanceSquare(x, y, alienX, alienY)) {
                    alienX = aC.x;
                    alienY = aC.y;
                }
            }

            pos[0] = alienX;
            pos[1] = alienY;

            return pos;
        } else {
            // probe around our current position,
            // tracing an imaginary square of increasing size,
            // starting from the midpoints of the sides
            for (int d = 1; d <= size; d++) {
                for (int dd = 0; dd <= d; dd++) {
                    probe(pos, ac.x - d, ac.y - dd);
                    probe(pos, ac.x - d, ac.y + dd);
                    probe(pos, ac.x + d, ac.y - dd);
                    probe(pos, ac.x + d, ac.y + dd);
                    probe(pos, ac.x - dd, ac.y - d);
                    probe(pos, ac.x + dd, ac.y - d);
                    probe(pos, ac.x - dd, ac.y + d);
                    probe(pos, ac.x + dd, ac.y + d);

                    // if we found one coinciding with our x OR y position, 
                    // there cannot be a closer one in our search pattern
                    if ((pos[0] == ac.x) || (pos[1] == ac.y)) {
                        return pos;
                    }
                }
            }
        }
        return pos;
    }

    void probe(int[] pos, int x, int y) {
        if (((x + ac.grid.aliens.centerX) >= ac.grid.width)
                || ((x + ac.grid.aliens.centerX) < 0)
                || ((y + ac.grid.aliens.centerY) >= ac.grid.height)
                || ((y + ac.grid.aliens.centerY) < 0)) {
            return;
        }

        if (!ac.grid.aliens.isEmptyAt(x, y)) {
            // there are aliens there
            if (getPointDistanceSquare(x, y, ac.x, ac.y) < getPointDistanceSquare(pos[0], pos[1], ac.x, ac.y)) {
                // this is the closest one so far
                pos[0] = x;
                pos[1] = y;
            }
        }
    }

    private int getPointDistanceSquare(int x1, int y1, int x2, int y2) {
        // Square roots are expensive, and we only need to determine
        // which of two distances is the greatest, so we use the distance
        // formula but don't square root it because we can simply compare
        // the squares of distances instead

        return ((y1 - y2) * (y1 - y2)) + (x1 - x2) * (x1 - x2);
    }
}
