/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consolealiensgame;

import java.lang.reflect.Constructor;
import alieninterfaces.*;
import stockaliens.*;

/**
 *
 * @author guberti
 */
public class ConsoleAliensGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpaceGrid grid = new SpaceGrid();
        
        try
        {
            Constructor<?> cs = JARLoader.Load("stockaliens", "Dalek");
            grid.addAlien(2, 2, "stockaliens", "Dalek", cs);
        } catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
    
}
