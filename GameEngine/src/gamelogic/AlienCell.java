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
    public boolean listening;
}
