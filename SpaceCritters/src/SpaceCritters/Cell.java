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

    SpaceCritters gameShell;
    int fightCountDown;
    double energy;
    int totalFighters;
    final LinkedList<Alien3D> aliens;

    public Cell(SpaceCritters gameShellInstance, SpeciesSet s, int row, int col) { // Constructor
        gameShell = gameShellInstance;
        fightCountDown = 0;
        this.aliens = new LinkedList();
    }

    public void addAlien(Alien3D alien) {
        this.aliens.add(alien);
        alien.zPos = this.aliens.indexOf(alien);
    }

    public void removeAlien(Alien3D alien) {
        Alien3D a;
        int count = 0;
        int newZPos;

        Iterator<Alien3D> iter = aliens.iterator();
        while (iter.hasNext()) {
            a = iter.next();
            if (a == alien) {
                iter.remove();
            } else {
                newZPos = count++;
                if (a.zPos != newZPos) {
                    a.zPos = newZPos;
                    gameShell.mainScene.updateQueue.add(a);
                }
            }
        }
    }

  
    public void fight(int num) {
        totalFighters = num;
    }
}