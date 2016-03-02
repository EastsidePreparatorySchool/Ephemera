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
        GameEngineV1 engine = new GameEngineV1();
        GameElementSpec[] gameSpec = new GameElementSpec[1];
        ConsoleVisualizer convis = new ConsoleVisualizer();
        Scanner scan = new Scanner(System.in);
        
        gameSpec[0] = new GameElementSpec("ALIEN", "stockaliens","Dalek", "", null);
        
        engine.init(convis, gameSpec);
        
        engine.queueCommand(new GameCommand(GameCommandCode.Resume));
        
        do 
        {
            try
            {
                sleep(50);
                if (!engine.isAlive())
                {
                    convis.debugErr("ConsoleShell: GameEngine died");
                    gameOver = true;
                }
            } catch (Exception e)
            {
                convis.debugOut(e.getMessage());
            }
            
        } while (!gameOver);

        engine.queueCommand(new GameCommand(GameCommandCode.End));
    }
    
}
