/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author gmein
 */
public class AlienCell extends LinkedList<AlienContainer> {
    public ArrayList<String> currentMessages;
    public SpaceObject object;
    int energy; // energy gain at this point in space
    int tech; // for planets, this is technology gain rate, otherwise 0
    public boolean listening;

    public AlienCell() {
        currentMessages = null;
        object = null;
        energy = 1;
        tech = 0;
        listening = false;
    }

}

