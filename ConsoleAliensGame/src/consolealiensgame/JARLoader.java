/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consolealiensgame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import alieninterfaces.*;

/**
 *
 * @author gmein
 */
public class JARLoader
{

    /**
     * Parameters of the method to add an URL to the System classes.
     */
    private static final Class<?>[] parameters = new Class[]
    {
        URL.class
    };

    /**
     * Adds a file to the classpath.
     *
     * @param s a String pointing to the file
     * @throws IOException
     */
    public static void addFile(String s) throws IOException
    {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Adds a file to the classpath
     *
     * @param f the file to be added
     * @throws IOException
     */
    public static void addFile(File f) throws IOException
    {
        addURL(f.toURI().toURL());
    }

    /**
     * Adds the content pointed by the URL to the classpath.
     *
     * @param u the URL pointing to the content to be added
     * @throws IOException
     */
    public static void addURL(URL u) throws IOException
    {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try
        {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]
            {
                u
            });
        } catch (Throwable t)
        {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    public static Constructor<?> Load(String packageName, String className) throws IOException, SecurityException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        addFile(packageName+".jar");
        Constructor<?> cs = ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className).getConstructor();
        return cs;
    }

    
    
    /* alternative code
     public static void test2()
     {
     try
     {
     System.out.println("Loading from C:\\");
     File file  = new File("c:\\stockaliens.jar");

     URL url = file.toURL();  
     URL[] urls = new URL[]{url};

     ClassLoader cl = new URLClassLoader(urls);
     Class c = cl.loadClass("stockaliens.Dalek");
     Constructor cn = c.getConstructor();
     Alien a = (Alien) cn.newInstance();
     a.getMove(null);
     } catch (Throwable t)
     {
     t.printStackTrace();
     }
     }
     */
}
