/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consoleshell;

import gameengineinterfaces.*;
import gameengineV1.*;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gmein
 */
public class ConsoleShell
{

    public static boolean gameOver = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        // args[0] = jar path (optional)
        // args[1] = config file (optional)
        
        //get some objects created (not initialized, nothing important happens here)
        GameEngineV1 engine = new GameEngineV1();
        GameElementSpec[] gameSpec = new GameElementSpec[1];
        ConsoleVisualizer convis = new ConsoleVisualizer();
        Scanner scan = new Scanner(System.in);
        
        // can override the path for class jar files in arguments
        String gameJarPath;
        if (args.length > 0 && args[0] != null && !args[0].isEmpty())
        {
            gameJarPath = args[0];
        } 
        else
        {
            gameJarPath = "c:\\users\\public\\ephemera\\drop\\";
        }
        
        System.out.println("test:"+gameJarPath);
        // get engine up and running
        engine.initFromFile(convis, gameJarPath, "ephemera_initial_setup.csv");
        
        // give it a go
        engine.queueCommand(new GameCommand(GameCommandCode.Resume));
        
        
        // wait for things to happen, mostly sleep, detect when we received game over, shut it down
        do 
        {
            try
            {
                sleep(50);
                
                // check whether anything catatrophic happened on the other thread
                if (!engine.isAlive())
                {
                    convis.debugErr("ConsoleShell: GameEngine died");
                    gameOver = true;
                }
            } catch (Exception e)
            {
                convis.debugOut(e.getMessage());
            }
            
        // gameOver is also set by the ConsoleVisualizer
        } while (!gameOver);

        engine.queueCommand(new GameCommand(GameCommandCode.End));
    }
    
}
