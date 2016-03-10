/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineV1;

import gamelogic.*;
import gameengineinterfaces.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Scanner;

/**
 *
 * @author gmein
 */
public class GameEngineThread extends Thread
{

    final private GameEngineV1 engine;
    
    public GameEngineThread(GameEngineV1 ge)
    {
        engine = ge;

        setName("GameEngineThread");
    }

    @Override
    public void run()
    {
        GameCommand gc;
        boolean endGame = false;
        Scanner scan = new Scanner(System.in);

        engine.grid = new SpaceGrid(engine.vis);
        
        try
        {
            addClassPathFile ("alieninterfaces");
        } catch (Exception e)
        {
            engine.vis.debugErr("GameEngineThread: run() could not load alieninterfaces.jar");
            return;
        }

        engine.vis.debugOut("GameEngineThread: Started");
        do
        {
            try
            {
                if (engine.gameState == GameState.Running)
                {
                    //
                    // TODO: Execute game turn here
                    //

                    engine.vis.debugOut("GameEngineThread: Executing game turn");
                    if (engine.grid.executeTurn())
                    {
                        //game over
                        engine.gameState = GameState.Paused;
                    }
                }

                //
                // between turns, process commands
                //
                
                if (engine.queue.isEmpty())
                {
                    if (engine.gameState == GameState.Paused)
                    {
                        //engine.vis.debugOut("GameEngineThread: About to wait for msgs");
                        synchronized (engine.queue)
                        {
                            engine.queue.wait();
                        }
                    }
                }
                // do we have work requests?
                while (!engine.queue.isEmpty())
                {
                    //engine.vis.debugOut("GameEngineThread: Dequeueing msg");

                    gc = (GameCommand) engine.queue.remove();

                    // Process the work item
                    //engine.vis.debugOut("GameEngineThread: Processing command");
                    endGame = processCommand(gc);
                    // endGame signifies that an "End" requeat has come through
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                engine.vis.debugErr("GameThread: Unknown exception: " + e.getMessage());
                break;
            }
        } while (!endGame);
        engine.vis.debugOut("GameThread: Exit");
    }

    private boolean processCommand(GameCommand gc) throws Exception
    {
        boolean gameOver = false;
        switch (gc.code)
        {
            case AddElement:
                engine.vis.debugOut("GameEngineThread: Processing GameElement");

                GameElementSpec element = (GameElementSpec) gc.parameters[0];
                //engine.vis.debugOut("GameEngineThread:     package " + element.packageName);
                //engine.vis.debugOut("GameEngineThread:     class   " + element.className);
                //engine.vis.debugOut("GameEngineThread:     state   " + element.state);
                
                if (element.kind != GameElementKind.INVALID) {
                    // temporary copout while I don't know how to handl stars, planets, residents
                    if (element.kind != GameElementKind.STAR &&
                            element.kind != GameElementKind.PLANET &&
                            element.kind != GameElementKind.RESIDENT) {
                        try
                        {
                            element.cns = Load(element.packageName, element.className);
                        } catch (Exception e)
                        {
                            engine.vis.debugErr("GameEngineThread: Error loading game element");
                            throw(e);
                        }
                    }
                }


                engine.grid.addElement(element);
                break;

            case Pause:
                engine.vis.debugOut("GameEngineThread: Pausing");
                engine.gameState = GameState.Paused;
                break;

            case Resume:
                engine.vis.debugOut("GameEngineThread: Resuming");
                engine.gameState = GameState.Running;
                break;
                
            case End:
                engine.vis.debugOut("GameEngineThread: End request, thread closing");
                gameOver = true;
                break;
                

            default:
                break;
        }
        return gameOver;
    }
    
    
    
    //
    // Dynamic class loader (.jar files)
    // stolen from StackOverflow, considered dark voodoo magic
    //
    
    /**
     * Parameters of the method to add an URL to the System classes.
     */
    private static final Class<?>[] parameters = new Class[] { URL.class };

     /**
     * Adds the content pointed by the file to the classpath.
     *
     */
    private void addClassPathFile(String packageName) throws IOException
    {
        String fullName = engine.gameJarPath + packageName + ".jar";
        URL url = new File(fullName).toURI().toURL();
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try
        {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException("GameElementThread: Error, could not add URL to system classloader");
        }
        //engine.vis.debugOut("GameElementThread: package " + packageName + " added as " + fullName);
    }

    public Constructor<?> Load(String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Constructor<?> cs = null;
                
        try 
        {
            addClassPathFile(packageName);
            cs = ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className).getConstructor();
        } catch (Exception e)
        {
            e.printStackTrace();
            engine.vis.debugErr("GameElementThread: Error: Could not get constructor");
            throw e;
        }
        return cs;
    }
    
}
