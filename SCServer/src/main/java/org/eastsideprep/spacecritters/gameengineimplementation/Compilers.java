/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.gameengineimplementation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author gunnar
 */
public class Compilers {

 public static Constructor<?> compileAndLoadJava(String folder, String className) {
        String fileName = folder + System.getProperty("file.separator") + className + ".java";

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector< JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null);

        File file = null;
        try {
            file = new File(fileName);
        } catch (Exception e) {
            System.out.println("GE:compileAndLoadJava: could not find file to compile: " + fileName);
        }

        final Iterable< ? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(Arrays.asList(file));
        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sources);
        task.call();

        Constructor<?> cs = null;

        try {
            Class<?> compiled = new URLClassLoader(new URL[]{Paths.get(folder).toUri().toURL()}).loadClass(className);
            cs = compiled.getConstructor();
//            Object o = cs.newInstance();
//            Method m = compiled.getDeclaredMethod("test");
//            m.invoke(o);
        } catch (Exception e) {
            System.out.println("GE:compileAndLoadJava: " + e.toString());
        }

        return cs;
    }
 
  public static void compileJava(String folder, String className) {
        String fileName = folder + System.getProperty("file.separator") + className + ".java";

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector< JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null);

        File file = null;
        try {
            file = new File(fileName);
        } catch (Exception e) {
            System.out.println("GE:compileAndLoadJava: could not find file to compile: " + fileName);
        }

        final Iterable< ? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(Arrays.asList(file));
        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sources);
        task.call();
    }
 
}
