/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.gameengineimplementation;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author gunnar
 */
public class Compilers {

    public static void compileJava(String folder, String className) {
        String fileName = folder + System.getProperty("file.separator") + className + ".java";

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                System.out.println("Compiler:compileJava: could not load compiler");
//                try {
//                    compiler.getClass();
//                }catch (Exception e) {
//                    System.out.println("Compiler folder:" + folder+", class "+className);
//                    System.out.println("Compiler was invoiked through: ");
//                    e.printStackTrace();
//                }
                return;
            }
            
//            final JavaCompiler compiler = new EclipseCompiler();
            final DiagnosticCollector< JavaFileObject> diagnostics = new DiagnosticCollector<>();
            final StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null);

            File file = null;
            try {
                file = new File(fileName);
            } catch (Exception e) {
                System.out.println("Compiler:compileJava: could not find file to compile: " + fileName);
            }

            final Iterable< ? extends JavaFileObject> sources = manager.getJavaFileObjectsFromFiles(Arrays.asList(file));
            final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, null, null, sources);
            task.call();
        } catch (Exception e) {
            System.out.println("Compiler:compileJava: " + e.toString());
        }
    }

}
