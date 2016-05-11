/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author gmein
 */
class Cell {

    int alienCount;
    SpaceCritters gameShell;
    double energy;
    int totalFighters;
    final LinkedList<Alien3D> aliens;

    public Cell(SpaceCritters gameShellInstance, SpeciesSet s, int row, int col) { // Constructor
        gameShell = gameShellInstance;
        alienCount = 0;
        this.aliens = new LinkedList();
    }

    public void addAlien(Alien3D alien) {
        this.aliens.add(alien);
        alien.nextZ = this.alienCount;
        this.alienCount++;

    }

    public void removeAlien(Alien3D alien) {
        Alien3D a;
        int count = 0;

        Iterator<Alien3D> iter = aliens.iterator();
        while (iter.hasNext()) {
            a = iter.next();
            if (a == alien) {
                iter.remove();
                this.alienCount--;
            } else {
                if (a.zPos != count) {
                    a.nextZ = count;
                    gameShell.mainScene.updateQueue.add(a);
                }
                count++;
            }
        }
    }

    public void fight(int num) {
        totalFighters = num;
    }
}
